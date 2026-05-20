package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class CandidateScoreDto {
    Long id;
    Long candidateId;
    Long jobDescriptionId;
    BigDecimal matchScore;
    String missingSkills;
    String matchingSkills;
    String fitmentSummary;
    String recommendation;
    List<String> interviewPros;
    List<String> interviewCons;
    LocalDateTime scoredAt;
}
