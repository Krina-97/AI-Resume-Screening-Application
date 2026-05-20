package com.airesume.screening.controller;

import com.airesume.screening.dto.DashboardStatsDto;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import com.airesume.screening.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analytics/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;
    private final CandidateRepository candidateRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    public AdminAnalyticsController(AnalyticsService analyticsService,
                                    CandidateRepository candidateRepository,
                                    JobDescriptionRepository jobDescriptionRepository) {
        this.analyticsService = analyticsService;
        this.candidateRepository = candidateRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    @GetMapping("/summary")
    @Operation(summary = "Admin analytics overview")
    public Map<String, Object> summary() {
        DashboardStatsDto dashboard = analyticsService.dashboard();
        Map<String, Object> payload = new HashMap<>();
        payload.put("dashboard", dashboard);
        payload.put("totalJobs", jobDescriptionRepository.count());
        payload.put("totalCandidates", candidateRepository.count());
        return payload;
    }
}
