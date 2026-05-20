package com.airesume.screening.controller;

import com.airesume.screening.dto.LinkedInVerificationDto;
import com.airesume.screening.service.LinkedInAutomationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/linkedin")
public class LinkedInController {

    private final LinkedInAutomationService linkedInAutomationService;

    public LinkedInController(LinkedInAutomationService linkedInAutomationService) {
        this.linkedInAutomationService = linkedInAutomationService;
    }

    @PostMapping("/verify/{candidateId}")
    @Operation(summary = "Run Selenium-assisted LinkedIn profile discovery (Google search + screenshot)")
    public LinkedInVerificationDto verify(@PathVariable Long candidateId) {
        return linkedInAutomationService.verifyProfile(candidateId);
    }

    @GetMapping("/{candidateId}/logs")
    @Operation(summary = "LinkedIn verification history")
    public List<LinkedInVerificationDto> logs(@PathVariable Long candidateId) {
        return linkedInAutomationService.history(candidateId);
    }
}
