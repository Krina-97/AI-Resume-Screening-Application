package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ResumeUploadResponse {
    Long resumeId;
    Long candidateId;
    String message;
    boolean possibleDuplicate;
    BigDecimal matchScore;
    String assignedStatus;
    Long jobDescriptionId;
}
