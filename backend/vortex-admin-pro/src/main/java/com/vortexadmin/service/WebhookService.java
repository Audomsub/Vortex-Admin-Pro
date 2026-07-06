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

/**
 * Service responsible for managing webhook endpoints and orchestrating event delivery to
 * registered consumer URLs, including HMAC signature generation, delivery logging, and
 * asynchronous dispatch.
 */
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookDeliveryRepository deliveryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- CRUD ----------

    /**
     * Maps a {@link WebhookEndpoint} entity to its DTO response, splitting the
     * comma-separated {@code eventsSubscribed} string into a list of event-type strings.
     *
     * @param endpoint the webhook endpoint entity to convert
     * @return the corresponding {@link WebhookEndpointResponse} DTO
     */
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

    /**
     * Returns all registered webhook endpoints ordered by creation date descending.
     *
     * @return a list of webhook endpoint responses, newest first
     */
    public List<WebhookEndpointResponse> getAllEndpoints() {
        return endpointRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new webhook endpoint with a freshly generated signing secret.  The plaintext
     * secret is included in the response exactly once so the caller can store it; it is not
     * retrievable afterwards.
     *
     * @param request the endpoint creation payload including name, URL, subscribed events, and
     *                active flag
     * @return the newly created endpoint response, including the one-time plaintext secret
     */
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

    /**
     * Updates an existing webhook endpoint's name, URL, subscribed events, and active flag.
     * The signing secret is not changed by this operation.
     *
     * @param id      the primary key of the endpoint to update
     * @param request the updated endpoint data
     * @return the updated endpoint response
     * @throws ApiException with HTTP 404 if no endpoint with the given ID exists
     */
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

    /**
     * Permanently deletes the specified webhook endpoint.
     *
     * @param id the primary key of the endpoint to delete
     * @throws ApiException with HTTP 404 if no endpoint with the given ID exists
     */
    @Transactional
    public void deleteEndpoint(Long id) {
        WebhookEndpoint endpoint = endpointRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Webhook endpoint not found"));
        endpointRepository.delete(endpoint);
    }

    /**
     * Returns the 20 most recent delivery attempts for the specified endpoint, ordered by
     * delivery timestamp descending.
     *
     * @param endpointId the primary key of the webhook endpoint
     * @return a list of up to 20 delivery responses for the given endpoint
     * @throws ApiException with HTTP 404 if no endpoint with the given ID exists
     */
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

    /**
     * Sends a {@code test.ping} event to the specified endpoint to allow operators to verify
     * that the endpoint URL is reachable and correctly configured.
     *
     * @param endpointId the primary key of the endpoint to test
     * @throws ApiException with HTTP 404 if no endpoint with the given ID exists
     */
    public void sendTestEvent(Long endpointId) {
        WebhookEndpoint endpoint = endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Webhook endpoint not found"));
        deliverWebhook(endpoint, "test.ping", toJson(Map.of(
                "event", "test.ping",
                "message", "This is a test event from Vortex Admin Pro",
                "timestamp", LocalDateTime.now().toString())));
    }

    // ---------- Event dispatch ----------

    /**
     * Dispatches a domain event asynchronously to all active webhook endpoints that have
     * subscribed to the given event type.  Each eligible endpoint receives an HMAC-signed
     * JSON payload and a delivery record is persisted regardless of success or failure.
     *
     * @param eventType the event type string to dispatch (e.g., "user.created", "role.updated")
     * @param data      the event payload data to include in the JSON body
     */
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

    /**
     * Serialises the given map to a JSON string using the shared {@link ObjectMapper}.
     *
     * @param map the data to serialise
     * @return the JSON string representation of the map
     * @throws IllegalStateException if serialisation fails
     */
    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize webhook payload", e);
        }
    }

    /**
     * Performs the actual HTTP POST delivery of a webhook payload to the endpoint URL,
     * attaching the event-type header and an HMAC-SHA256 signature header.  A delivery
     * record is persisted with the outcome regardless of whether the request succeeds.
     *
     * @param endpoint  the target webhook endpoint
     * @param eventType the event type string included in the {@code X-Vortex-Event} header
     * @param payload   the JSON payload string to POST
     */
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

    /**
     * Generates an HMAC-SHA256 signature for the given payload using the endpoint's signing
     * secret, Base64-encoded for transmission in the {@code X-Vortex-Signature} header.
     *
     * @param payload the raw JSON payload string to sign
     * @param secret  the endpoint's signing secret
     * @return the Base64-encoded HMAC-SHA256 signature
     * @throws Exception if the HMAC algorithm is unavailable or key initialisation fails
     */
    private String generateSignature(String payload, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        return Base64.getEncoder().encodeToString(sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Persists a delivery record for an attempted webhook dispatch.  The response body is
     * truncated to 2000 characters if it exceeds that length to prevent oversized rows.
     *
     * @param endpoint     the endpoint that was targeted
     * @param eventType    the event type that was delivered
     * @param payload      the JSON payload that was sent
     * @param statusCode   the HTTP status code returned by the consumer (or {@code 0} on error)
     * @param responseBody the raw response body from the consumer (may be an error message)
     * @param success      {@code true} if the HTTP call completed successfully, otherwise {@code false}
     */
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
