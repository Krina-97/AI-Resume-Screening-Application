package com.airesume.screening.service.ai;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ResumeMatchResult {
    BigDecimal matchScore;
    List<String> missingSkills;
    List<String> matchingSkills;
    String fitmentSummary;
    String recommendation;
}
