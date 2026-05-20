package com.airesume.screening.dto;

import com.airesume.screening.entity.CandidateStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class CandidateDto {
    Long id;
    Long resumeId;
    Long jobDescriptionId;
    String fullName;
    String email;
    String phone;
    String skills;
    String experience;
    String education;
    String certifications;
    String linkedinUrl;
    String aiSummary;
    CandidateStatus status;
    BigDecimal latestMatchScore;
    LocalDateTime createdAt;
}
