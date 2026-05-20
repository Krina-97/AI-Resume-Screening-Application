package com.airesume.screening.controller;

import com.airesume.screening.dto.DashboardStatsDto;
import com.airesume.screening.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "HR dashboard metrics")
    public DashboardStatsDto dashboard() {
        return analyticsService.dashboard();
    }
}
