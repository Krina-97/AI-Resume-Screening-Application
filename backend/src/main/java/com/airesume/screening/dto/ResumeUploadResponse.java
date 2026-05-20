package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeUploadResponse {
    Long resumeId;
    Long candidateId;
    String message;
    boolean possibleDuplicate;
}
