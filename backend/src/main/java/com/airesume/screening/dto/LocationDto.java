package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LocationDto {
    Long id;
    String name;
}
