package com.airesume.screening.controller;

import com.airesume.screening.dto.ResumeUploadResponse;
import com.airesume.screening.entity.Resume;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.repository.ResumeRepository;
import com.airesume.screening.security.CustomUserDetailsService;
import com.airesume.screening.service.ResumeIngestionService;
import com.airesume.screening.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeIngestionService resumeIngestionService;
    private final CustomUserDetailsService userDetailsService;
    private final ResumeRepository resumeRepository;

    public ResumeController(ResumeIngestionService resumeIngestionService,
                            CustomUserDetailsService userDetailsService,
                            ResumeRepository resumeRepository) {
        this.resumeIngestionService = resumeIngestionService;
        this.userDetailsService = userDetailsService;
        this.resumeRepository = resumeRepository;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload resume (PDF/DOCX) and trigger parsing + optional scoring")
    public ResumeUploadResponse upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "jobDescriptionId", required = false) Long jobDescriptionId) {
        Long userId = userDetailsService.findUserEntity(SecurityUtils.currentUsername()).getId();
        return resumeIngestionService.ingest(file, jobDescriptionId, userId);
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "Preview extracted resume text")
    public String preview(@PathVariable Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
        return resume.getRawText();
    }
}
