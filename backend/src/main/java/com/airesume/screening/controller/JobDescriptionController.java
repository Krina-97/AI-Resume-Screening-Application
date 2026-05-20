package com.airesume.screening.controller;

import com.airesume.screening.dto.JobDescriptionDto;
import com.airesume.screening.dto.JobDescriptionRequest;
import com.airesume.screening.security.CustomUserDetailsService;
import com.airesume.screening.service.JobDescriptionService;
import com.airesume.screening.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;
    private final CustomUserDetailsService userDetailsService;

    public JobDescriptionController(JobDescriptionService jobDescriptionService,
                                    CustomUserDetailsService userDetailsService) {
        this.jobDescriptionService = jobDescriptionService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping
    @Operation(summary = "List active job descriptions")
    public List<JobDescriptionDto> listActive() {
        return jobDescriptionService.listActive();
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all job descriptions (admin)")
    public List<JobDescriptionDto> listAll() {
        return jobDescriptionService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job description by id")
    public JobDescriptionDto get(@PathVariable Long id) {
        return jobDescriptionService.get(id);
    }

    @PostMapping
    @Operation(summary = "Create job description")
    public JobDescriptionDto create(@Valid @RequestBody JobDescriptionRequest request) {
        Long userId = userDetailsService.findUserEntity(SecurityUtils.currentUsername()).getId();
        return jobDescriptionService.create(request, userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job description")
    public JobDescriptionDto update(@PathVariable Long id, @Valid @RequestBody JobDescriptionRequest request) {
        return jobDescriptionService.update(id, request);
    }
}
