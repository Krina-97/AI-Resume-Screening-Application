package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class CandidateOverviewDto {
    String executiveSummary;
    String interviewVerdict;
    String interviewHeadline;
    List<String> interviewPros;
    List<String> interviewConcerns;
    BigDecimal matchPercent;
    String jobTitle;
    String workExperience;
    String skills;
    String strengths;
}
