package com.airesume.screening.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MultiJobCompareRequest {
    private Long candidateId;
    @NotEmpty
    private List<Long> jobDescriptionIds;
}
