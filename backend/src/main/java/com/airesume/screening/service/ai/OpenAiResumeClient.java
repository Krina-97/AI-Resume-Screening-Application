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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OpenAiResumeClient implements AiResumeClient {

    private final AppProperties appProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiResumeClient(AppProperties appProperties,
                              RestTemplate restTemplate,
                              ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(appProperties.getOpenai().getApiKey());
    }

    @Override
    public String completeJson(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        String url = appProperties.getOpenai().getBaseUrl() + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(appProperties.getOpenai().getApiKey());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", appProperties.getOpenai().getModel());
        body.put("response_format", Map.of("type", "json_object"));
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            String raw = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(raw);
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception ex) {
            log.error("OpenAI request failed", ex);
            throw new IllegalStateException("OpenAI request failed: " + ex.getMessage(), ex);
        }
    }
}
