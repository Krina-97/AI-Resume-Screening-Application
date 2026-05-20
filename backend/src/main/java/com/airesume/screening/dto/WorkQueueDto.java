package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class WorkQueueDto {
    long newCandidates;
    long shortlisted;
    long interviewScheduled;
    long jobsWithoutCandidates;
    List<JobQueueItemDto> emptyJobs;
}
