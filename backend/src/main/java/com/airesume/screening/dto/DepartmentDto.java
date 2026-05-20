package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DepartmentDto {
    Long id;
    String name;
}
