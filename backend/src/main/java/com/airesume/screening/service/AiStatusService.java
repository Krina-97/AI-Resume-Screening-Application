package com.airesume.screening.service;

import com.airesume.screening.config.AppProperties;
import com.airesume.screening.dto.AiStatusDto;
import com.airesume.screening.service.ai.GeminiResumeClient;
import com.airesume.screening.service.ai.OpenAiResumeClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiStatusService {

    private final AppProperties appProperties;
    private final OpenAiResumeClient openAiResumeClient;
    private final GeminiResumeClient geminiResumeClient;

    public AiStatusService(AppProperties appProperties,
                           OpenAiResumeClient openAiResumeClient,
                           GeminiResumeClient geminiResumeClient) {
        this.appProperties = appProperties;
        this.openAiResumeClient = openAiResumeClient;
        this.geminiResumeClient = geminiResumeClient;
    }

    public AiStatusDto status() {
        boolean gemini = geminiResumeClient.isAvailable();
        boolean openAi = openAiResumeClient.isAvailable();
        String provider = appProperties.getAiProvider();

        if ("gemini".equalsIgnoreCase(provider) && gemini) {
            return AiStatusDto.builder()
                    .mode("ai")
                    .label("AI: Gemini active")
                    .detail("Full resume parsing, 5–6 sentence summaries, and interview recommendations.")
                    .build();
        }
        if (openAi) {
            return AiStatusDto.builder()
                    .mode("ai")
                    .label("AI: OpenAI active")
                    .detail("Full resume parsing, 5–6 sentence summaries, and interview recommendations.")
                    .build();
        }
        if (gemini) {
            return AiStatusDto.builder()
                    .mode("ai")
                    .label("AI: Gemini available")
                    .detail("Set app.ai-provider=gemini to use Gemini as primary.")
                    .build();
        }
        String geminiKey = appProperties.getGemini().getApiKey();
        String openAiKey = appProperties.getOpenai().getApiKey();
        boolean anyKey = StringUtils.hasText(geminiKey) || StringUtils.hasText(openAiKey);
        return AiStatusDto.builder()
                .mode("heuristic")
                .label(anyKey ? "AI: Keys detected, not connected" : "Heuristic mode")
                .detail(anyKey
                        ? "Check API keys and network. Using keyword-based scoring until AI responds."
                        : "Add GEMINI_API_KEY or OPENAI_API_KEY in backend .env for rich summaries.")
                .build();
    }
}
