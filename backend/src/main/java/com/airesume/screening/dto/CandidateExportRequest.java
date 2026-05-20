package com.airesume.screening.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CandidateExportRequest {
    @NotEmpty
    private List<Long> ids;

    /** Supported: xlsx, pdf. Defaults to both when omitted. */
    private List<String> formats;
}
