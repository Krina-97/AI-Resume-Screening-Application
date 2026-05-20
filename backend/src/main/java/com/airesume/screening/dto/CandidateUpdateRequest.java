package com.airesume.screening.dto;

import com.airesume.screening.entity.CandidateStatus;
import lombok.Data;

@Data
public class CandidateUpdateRequest {
    private CandidateStatus status;
    private String linkedinUrl;
    private String hrNotes;
}
