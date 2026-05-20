package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class JobDescriptionAssistResponse {
    String title;
    String description;
    List<String> requiredSkills;
    List<String> preferredSkills;
    String assistantMessage;
    boolean aiGenerated;
}
