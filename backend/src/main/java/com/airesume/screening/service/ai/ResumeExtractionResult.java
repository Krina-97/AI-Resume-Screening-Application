package com.airesume.screening.service.ai;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResumeExtractionResult {
    String fullName;
    String email;
    String phone;
    String skills;
    String strengths;
    String experience;
    String education;
    String certifications;
    String linkedinUrl;
    String aiSummary;
}
