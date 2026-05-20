package com.airesume.screening.dto;

import com.airesume.screening.entity.InterviewStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewScheduleRequest {
    @NotNull
    private LocalDateTime scheduledAt;
    private String interviewerName;
    private String interviewType;
    private String location;
    private String meetingLink;
    private String notes;
    private InterviewStatusType status = InterviewStatusType.SCHEDULED;
}
