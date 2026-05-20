package com.airesume.screening.controller;

import com.airesume.screening.dto.*;
import com.airesume.screening.entity.CandidateStatus;
import com.airesume.screening.service.CandidateScoreService;
import com.airesume.screening.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    /** Static paths must be declared before /{id} so segments like "bulk-delete" are not treated as an id. */
    @GetMapping("/recommendations")
    @Operation(summary = "Candidate recommendation engine for a job (top match scores)")
    public List<CandidateDto> recommend(@RequestParam Long jobId,
                                        @RequestParam(defaultValue = "10") int limit) {
        return candidateService.topForJob(jobId, limit);
    }

    @PostMapping("/bulk-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete multiple candidates and related data")
    public void bulkDelete(@Valid @RequestBody CandidateBulkDeleteRequest request) {
        candidateService.deleteMany(request.getIds());
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete candidate and related resume/scores")
    public void delete(@PathVariable Long id) {
        candidateService.delete(id);
    }

    @PostMapping("/{id}/score")
    @Operation(summary = "Compute or refresh AI match score for a job")
    public CandidateDto score(@PathVariable Long id, @RequestParam Long jobDescriptionId) {
        candidateScoreService.scoreCandidate(id, jobDescriptionId, null);
        return candidateService.get(id);
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
}
