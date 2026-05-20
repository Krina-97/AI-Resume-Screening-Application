package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class LinkedInVerificationDto {
    Long id;
    String searchQuery;
    String profileHeadline;
    String profileUrl;
    String verificationStatus;
    String screenshotPath;
    String logDetails;
    LocalDateTime verifiedAt;
}
