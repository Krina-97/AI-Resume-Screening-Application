package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JobQueueItemDto {
    Long id;
    String title;
}
