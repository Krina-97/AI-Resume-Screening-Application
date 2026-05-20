package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SkillDto {
    Long id;
    String name;
}
