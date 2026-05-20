package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AiStatusDto {
    String mode;
    String label;
    String detail;
}
