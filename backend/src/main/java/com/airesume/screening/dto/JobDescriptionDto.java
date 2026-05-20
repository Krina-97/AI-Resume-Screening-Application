package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class JobDescriptionDto {
    Long id;
    String title;
    String department;
    String location;
    String experienceRequired;
    String requiredSkills;
    String preferredSkills;
    String description;
    Boolean active;
    LocalDateTime createdAt;
}
