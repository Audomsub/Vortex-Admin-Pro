package com.vortexadmin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    @Value("${vortex.ai.gemini.key:}")
    private String geminiApiKey;

    @Override
    public String analyzeAuditLogs(List<Map<String, Object>> logs) {
        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.contains("YOUR_API_KEY")) {
            return "❌ Gemini API Key is missing. Please set 'vortex.ai.gemini.key' in application.yaml to use AI features.";
        }

        try {
            // Convert logs to a readable string format for the prompt
            ObjectMapper mapper = new ObjectMapper();
            String logsJson = mapper.writeValueAsString(logs);
            
            // Limit to last 50 logs to avoid token limits
            if (logsJson.length() > 15000) {
                logsJson = logsJson.substring(0, 15000) + "... (truncated)";
            }

            String prompt = "You are a cybersecurity and system administration expert. " +
                    "Analyze the following recent system audit logs for an Enterprise Dashboard. " +
                    "Identify any suspicious activities, unauthorized access attempts, or repetitive failures. " +
                    "Provide a brief, professional summary of the system's health and security status based on these logs. " +
                    "Please provide your analysis and summary entirely in Thai language. " +
                    "Use formatting like bolding and bullet points.\n\nLogs:\n" + logsJson;

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) resContent.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }

            return "⚠️ Gemini analyzed the data, but no text was returned.";

        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to analyze logs with Gemini: " + e.getMessage());
        }
    }
}
