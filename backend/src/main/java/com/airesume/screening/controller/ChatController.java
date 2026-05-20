package com.airesume.screening.controller;

import com.airesume.screening.dto.ChatRequest;
import com.airesume.screening.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatbotService chatbotService;

    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    @Operation(summary = "AI HR assistant chatbot")
    public Map<String, String> chat(@Valid @RequestBody ChatRequest request) {
        String reply = chatbotService.ask(request.getMessage(), request.getContext());
        return Map.of("reply", reply);
    }
}
