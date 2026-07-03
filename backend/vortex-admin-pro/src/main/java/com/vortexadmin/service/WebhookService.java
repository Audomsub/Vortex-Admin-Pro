package com.vortexadmin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexadmin.dto.request.WebhookEndpointRequest;
import com.vortexadmin.dto.response.WebhookDeliveryResponse;
import com.vortexadmin.dto.response.WebhookEndpointResponse;
import com.vortexadmin.entity.WebhookDelivery;
import com.vortexadmin.entity.WebhookEndpoint;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.WebhookDeliveryRepository;
import com.vortexadmin.repository.WebhookEndpointRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    // BUG-025: injected bean has connect/read timeouts configured in AppConfig
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- CRUD ----------

    private WebhookEndpointResponse mapToResponse(WebhookEndpoint endpoint) {
        List<String> events = endpoint.getEventsSubscribed() != null && !endpoint.getEventsSubscribed().isBlank()
                ? Arrays.asList(endpoint.getEventsSubscribed().split(","))
                : List.of();
        return WebhookEndpointResponse.builder()
                .id(endpoint.getId())
                .name(endpoint.getName())
                .url(endpoint.getUrl())
                .events(events)
                .active(endpoint.isActive())
                .createdAt(endpoint.getCreatedAt())
                .build();
    }

    public List<WebhookEndpointResponse> getAllEndpoints() {
        return endpointRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WebhookEndpointResponse createEndpoint(WebhookEndpointRequest request) {
        String secret = "whsec_" + UUID.randomUUID().toString().replace("-", "");

        WebhookEndpoint endpoint = WebhookEndpoint.builder()
                .name(request.getName())
                .url(request.getUrl())
                .secret(secret)
                .eventsSubscribed(String.join(",", request.getEvents()))
                .active(request.isActive())
                .build();
        endpointRepository.save(endpoint);

        WebhookEndpointResponse response = mapToResponse(endpoint);
        response.setSecret(secret); // shown to the user only once
        return response;
    }

    @Transactional
    public WebhookEndpointResponse updateEndpoint(Long id, WebhookEndpointRequest request) {
        WebhookEndpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Webhook endpoint not found"));

        endpoint.setName(request.getName());
        endpoint.setUrl(request.getUrl());
        endpoint.setEventsSubscribed(String.join(",", request.getEvents()));
        endpoint.setActive(request.isActive());
        endpointRepository.save(endpoint);

        return mapToResponse(endpoint);
    }

    @Transactional
    public void deleteEndpoint(Long id) {
        WebhookEndpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Webhook endpoint not found"));
        endpointRepository.delete(endpoint);
    }

    public List<WebhookDeliveryResponse> getDeliveries(Long endpointId) {
        if (!endpointRepository.existsById(endpointId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Webhook endpoint not found");
        }
        return deliveryRepository.findTop20ByWebhookEndpointIdOrderByDeliveredAtDesc(endpointId).stream()
                .map(d -> WebhookDeliveryResponse.builder()
                        .id(d.getId())
                        .eventType(d.getEventType())
                        .statusCode(d.getStatusCode())
                        .success(d.isSuccess())
                        .responseBody(d.getResponseBody())
                        .deliveredAt(d.getDeliveredAt())
                        .build())
                .collect(Collectors.toList());
    }

    public void sendTestEvent(Long endpointId) {
        WebhookEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Webhook endpoint not found"));
        deliverWebhook(endpoint, "test.ping", toJson(Map.of(
                "event", "test.ping",
                "message", "This is a test event from Vortex Admin Pro",
                "timestamp", LocalDateTime.now().toString())));
    }

    // ---------- Event dispatch ----------

    @Async
    public void triggerEvent(String eventType, Map<String, Object> data) {
        try {
            List<WebhookEndpoint> endpoints = endpointRepository.findByActiveTrue().stream()
                    .filter(e -> e.getEventsSubscribed() != null
                            && Arrays.asList(e.getEventsSubscribed().split(",")).contains(eventType))
                    .collect(Collectors.toList());

            if (endpoints.isEmpty()) {
                return;
            }

            String payload = toJson(Map.of(
                    "event", eventType,
                    "timestamp", LocalDateTime.now().toString(),
                    "data", data));

            for (WebhookEndpoint endpoint : endpoints) {
                deliverWebhook(endpoint, eventType, payload);
            }
        } catch (Exception e) {
            logger.error("Failed to trigger webhook event {}: {}", eventType, e.getMessage());
        }
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize webhook payload", e);
        }
    }

    private void deliverWebhook(WebhookEndpoint endpoint, String eventType, String payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Vortex-Event", eventType);
            headers.set("X-Vortex-Signature", generateSignature(payload, endpoint.getSecret()));

            HttpEntity<String> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint.getUrl(), request, String.class);

            saveDelivery(endpoint, eventType, payload, response.getStatusCode().value(), response.getBody(), true);
        } catch (Exception e) {
            saveDelivery(endpoint, eventType, payload, 0, e.getMessage(), false);
        }
    }

    private String generateSignature(String payload, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        return Base64.getEncoder().encodeToString(sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    private void saveDelivery(WebhookEndpoint endpoint, String eventType, String payload, int statusCode, String responseBody, boolean success) {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhookEndpoint(endpoint)
                .eventType(eventType)
                .payload(payload)
                .statusCode(statusCode)
                .responseBody(responseBody != null && responseBody.length() > 2000
                        ? responseBody.substring(0, 2000)
                        : responseBody)
                .success(success)
                .build();
        deliveryRepository.save(delivery);
    }
}
