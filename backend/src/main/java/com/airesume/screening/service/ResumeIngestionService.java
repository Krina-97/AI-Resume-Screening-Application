package com.airesume.screening.service;

import com.airesume.screening.config.AppProperties;
import com.airesume.screening.dto.CandidateScoreDto;
import com.airesume.screening.dto.ResumeUploadResponse;
import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.CandidateStatus;
import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.entity.Resume;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import com.airesume.screening.repository.ResumeRepository;
import com.airesume.screening.service.ai.AiEngineService;
import com.airesume.screening.service.ai.ResumeExtractionResult;
import com.airesume.screening.utils.FileValidationUtils;
import com.airesume.screening.utils.HashUtils;
import com.airesume.screening.utils.TextFormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ResumeIngestionService {

    private final AppProperties appProperties;
    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeTextExtractorService resumeTextExtractorService;
    private final AiEngineService aiEngineService;
    private final CandidateScoreService candidateScoreService;

    public ResumeIngestionService(AppProperties appProperties,
                                  ResumeRepository resumeRepository,
                                  CandidateRepository candidateRepository,
                                  JobDescriptionRepository jobDescriptionRepository,
                                  ResumeTextExtractorService resumeTextExtractorService,
                                  AiEngineService aiEngineService,
                                  CandidateScoreService candidateScoreService) {
        this.appProperties = appProperties;
        this.resumeRepository = resumeRepository;
        this.candidateRepository = candidateRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeTextExtractorService = resumeTextExtractorService;
        this.aiEngineService = aiEngineService;
        this.candidateScoreService = candidateScoreService;
    }

    @Transactional
    public ResumeUploadResponse ingest(MultipartFile file, Long jobDescriptionId, Long uploadedByUserId) {
        FileValidationUtils.validateResume(file);
        Path uploadDir = Paths.get(appProperties.getUpload().getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("Unable to prepare upload directory: {}", uploadDir, e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to prepare upload directory");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "resume";
        String lower = original.toLowerCase();
        String ext = lower.endsWith(".docx") ? ".docx" : ".pdf";
        String storedName = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(storedName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store resume at {}", target, e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store resume");
        }
        log.debug("Stored resume at {}", target);

        String rawText = resumeTextExtractorService.extractText(file, target);
        String duplicateHash = HashUtils.sha256Hex(HashUtils.normalizeResumeText(rawText));
        Optional<Candidate> duplicate = candidateRepository.findByDuplicateHash(duplicateHash);

        Resume resume = Resume.builder()
                .fileName(original)
                .filePath(target.toAbsolutePath().toString())
                .fileType(ext.replace(".", ""))
                .fileSize(file.getSize())
                .rawText(rawText)
                .uploadedBy(uploadedByUserId)
                .build();
        resumeRepository.save(resume);

        ResumeExtractionResult extraction = aiEngineService.extractFromResumeText(rawText);

        Long effectiveJobId = jobDescriptionId;
        if (effectiveJobId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Select a job description to evaluate this resume against.");
        }
        JobDescription job = jobDescriptionRepository.findById(effectiveJobId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));

        Candidate candidate = Candidate.builder()
                .resumeId(resume.getId())
                .jobDescriptionId(effectiveJobId)
                .fullName(TextFormatUtils.toTitleCaseName(extraction.getFullName()))
                .email(extraction.getEmail())
                .phone(extraction.getPhone())
                .skills(extraction.getSkills())
                .strengths(extraction.getStrengths())
                .experience(extraction.getExperience())
                .education(extraction.getEducation())
                .certifications(extraction.getCertifications())
                .linkedinUrl(StringUtils.hasText(extraction.getLinkedinUrl()) ? extraction.getLinkedinUrl() : null)
                .aiSummary(extraction.getAiSummary())
                .duplicateHash(duplicateHash)
                .status(CandidateStatus.NEW)
                .build();
        candidateRepository.save(candidate);

        BigDecimal matchScore = null;
        CandidateStatus assignedStatus = CandidateStatus.NEW;
        String resultMessage = "Resume saved. AI evaluation could not be completed — status set to New.";

        try {
            CandidateScoreDto score = candidateScoreService.scoreCandidate(candidate.getId(), effectiveJobId, rawText);
            matchScore = score.getMatchScore();
            assignedStatus = CandidatePipelineRules.statusFromMatchPercent(matchScore);
            candidate.setStatus(assignedStatus);
            candidate.setAiSummary(CandidateOverviewBuilder.formatStoredOverview(candidate, score, job.getTitle()));
            candidateRepository.save(candidate);
            resultMessage = String.format(
                    "Match score: %.1f%% against \"%s\". Status set to %s (%s).",
                    matchScore.doubleValue(),
                    job.getTitle(),
                    formatStatusLabel(assignedStatus),
                    CandidatePipelineRules.statusRuleDescription());
            log.info("Candidate {} evaluated: {}% → {}", candidate.getId(), matchScore, assignedStatus);
        } catch (Exception e) {
            log.warn("Candidate {} saved but job evaluation failed for job {}: {}",
                    candidate.getId(), effectiveJobId, e.getMessage());
        }

        return ResumeUploadResponse.builder()
                .resumeId(resume.getId())
                .candidateId(candidate.getId())
                .message(resultMessage)
                .possibleDuplicate(duplicate.isPresent())
                .matchScore(matchScore)
                .assignedStatus(assignedStatus.name())
                .jobDescriptionId(effectiveJobId)
                .build();
    }

    private static String formatStatusLabel(CandidateStatus status) {
        return switch (status) {
            case REJECTED -> "Rejected";
            case SHORTLISTED -> "Shortlisted";
            case INTERVIEW_SCHEDULED -> "Interview scheduled";
            case NEW -> "New";
        };
    }
}
