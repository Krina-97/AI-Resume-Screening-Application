package com.airesume.screening.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CandidateBulkDeleteRequest {
    @NotEmpty
    private List<Long> ids;
}
