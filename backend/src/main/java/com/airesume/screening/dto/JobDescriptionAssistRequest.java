package com.airesume.screening.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobDescriptionAssistRequest {
    @NotBlank
    private String department;
    private String title;
    private String location;
    private String experienceRequired;
    /** When true, request an alternate draft (different variant). */
    private Boolean regenerate = false;
    /** Rotates template wording on regenerate (0, 1, 2, …). */
    private Integer variant = 0;
}
