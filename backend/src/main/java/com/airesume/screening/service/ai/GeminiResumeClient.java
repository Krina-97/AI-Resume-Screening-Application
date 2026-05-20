package com.airesume.screening.service.ai;

import com.airesume.screening.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiResumeClient implements AiResumeClient {

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiResumeClient(AppProperties appProperties,
                              RestTemplate restTemplate,
                              ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(appProperties.getGemini().getApiKey())
                && appProperties.getGemini().isEnabled();
    }

    public String generatePlain(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("Gemini API is not configured");
        }
        String url = UriComponentsBuilder.fromUriString(GEMINI_URL)
                .queryParam("key", appProperties.getGemini().getApiKey())
                .toUriString();

        String combined = systemPrompt + "\n\n" + userPrompt;
        Map<String, Object> part = Map.of("text", combined);
        Map<String, Object> content = Map.of("role", "user", "parts", List.of(part));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String raw = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(raw);
            return root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } catch (Exception ex) {
            log.error("Gemini plain generation failed", ex);
            throw new IllegalStateException("Gemini request failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String completeJson(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("Gemini API is not configured");
        }

        String url = UriComponentsBuilder.fromUriString(GEMINI_URL)
                .queryParam("key", appProperties.getGemini().getApiKey())
                .toUriString();

        Map<String, Object> partUser = Map.of("text", userPrompt + "\nRespond ONLY with valid JSON, no markdown.");
        Map<String, Object> partSystem = Map.of("text", "System instructions: " + systemPrompt);
        Map<String, Object> content = Map.of("role", "user", "parts", List.of(partSystem, partUser));
        Map<String, Object> body = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String raw = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(raw);
            return root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } catch (Exception ex) {
            log.error("Gemini request failed", ex);
            throw new IllegalStateException("Gemini request failed: " + ex.getMessage(), ex);
        }
    }
}
