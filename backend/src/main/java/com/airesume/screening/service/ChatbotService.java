package com.airesume.screening.service;

import com.airesume.screening.service.ai.AiEngineService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatbotService {

    private final AiEngineService aiEngineService;

    public ChatbotService(AiEngineService aiEngineService) {
        this.aiEngineService = aiEngineService;
    }

    public String ask(String message, String context) {
        StringBuilder system = new StringBuilder();
        system.append("You are an AI assistant for HR recruiters using an AI resume screening platform. ");
        system.append("Provide concise, professional guidance about hiring, resume evaluation, and interview planning.");
        if (StringUtils.hasText(context)) {
            system.append(" Additional context: ").append(context);
        }
        return aiEngineService.chat(system.toString(), message);
    }
}
