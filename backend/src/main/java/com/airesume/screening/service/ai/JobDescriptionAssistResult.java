package com.airesume.screening.service.ai;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class JobDescriptionAssistResult {
    String title;
    String description;
    List<String> requiredSkills;
    List<String> preferredSkills;
    String assistantMessage;
    boolean aiGenerated;
}
