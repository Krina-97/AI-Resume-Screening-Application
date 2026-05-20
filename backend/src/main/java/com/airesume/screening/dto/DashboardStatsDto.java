package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DashboardStatsDto {
    long totalCandidates;
    long shortlisted;
    long rejected;
    long interviewsScheduled;
    Double averageMatchScore;
    List<CandidateDto> topCandidates;
    WorkQueueDto workQueue;
}
