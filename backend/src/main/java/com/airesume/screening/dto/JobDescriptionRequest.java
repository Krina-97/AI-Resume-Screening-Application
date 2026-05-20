package com.airesume.screening.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobDescriptionRequest {
    @NotBlank
    private String title;
    private String department;
    private String location;
    private String experienceRequired;
    private String requiredSkills;
    private String preferredSkills;
    @NotBlank
    private String description;
    private Boolean active = true;
}
