package com.airesume.screening.service.ai;

public interface AiResumeClient {

    String completeJson(String systemPrompt, String userPrompt);

    boolean isAvailable();
}
