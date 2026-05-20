package com.airesume.screening.controller;

import com.airesume.screening.dto.*;
import com.airesume.screening.entity.CandidateStatus;
import com.airesume.screening.service.CandidateScoreService;
import com.airesume.screening.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateService candidateService;
    private final CandidateScoreService candidateScoreService;

    public CandidateController(CandidateService candidateService,
                               CandidateScoreService candidateScoreService) {
        this.candidateService = candidateService;
        this.candidateScoreService = candidateScoreService;
    }

    @GetMapping
    @Operation(summary = "Search and filter candidates")
    public List<CandidateDto> search(@RequestParam(required = false) Long jobId,
                                     @RequestParam(required = false) CandidateStatus status,
                                     @RequestParam(required = false) String q) {
        return candidateService.search(jobId, status, q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get candidate details")
    public CandidateDto get(@PathVariable Long id) {
        return candidateService.get(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update candidate status or LinkedIn URL")
    public CandidateDto update(@PathVariable Long id, @RequestBody CandidateUpdateRequest request) {
        return candidateService.update(id, request);
    }

    @PostMapping("/{id}/score")
    @Operation(summary = "Compute or refresh AI match score for a job")
    public CandidateScoreDto score(@PathVariable Long id, @RequestParam Long jobDescriptionId) {
        return candidateScoreService.scoreCandidate(id, jobDescriptionId, null);
    }

    @PostMapping("/{id}/interviews")
    @Operation(summary = "Schedule interview and notify candidate by email when configured")
    public void scheduleInterview(@PathVariable Long id, @Valid @RequestBody InterviewScheduleRequest request) {
        candidateService.scheduleInterview(id, request);
    }

    @PostMapping("/{id}/compare")
    @Operation(summary = "Multi-job comparison: scores candidate against multiple job descriptions")
    public List<CandidateScoreDto> compare(@PathVariable Long id, @Valid @RequestBody MultiJobCompareRequest request) {
        for (Long jobId : request.getJobDescriptionIds()) {
            candidateScoreService.scoreCandidate(id, jobId, null);
        }
        return candidateService.compareAcrossJobs(id, request.getJobDescriptionIds());
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Candidate recommendation engine for a job (top match scores)")
    public List<CandidateDto> recommend(@RequestParam Long jobId,
                                        @RequestParam(defaultValue = "10") int limit) {
        return candidateService.topForJob(jobId, limit);
    }
}
