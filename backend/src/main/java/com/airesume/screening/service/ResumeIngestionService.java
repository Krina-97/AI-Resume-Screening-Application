package com.airesume.screening.service;

import com.airesume.screening.config.AppProperties;
import com.airesume.screening.dto.CandidateScoreDto;
import com.airesume.screening.dto.ResumeUploadResponse;
import com.airesume.screening.entity.Candidate;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

        Candidate candidate = Candidate.builder()
                .resumeId(resume.getId())
                .jobDescriptionId(jobDescriptionId)
                .fullName(extraction.getFullName())
                .email(extraction.getEmail())
                .phone(extraction.getPhone())
                .skills(extraction.getSkills())
                .experience(extraction.getExperience())
                .education(extraction.getEducation())
                .certifications(extraction.getCertifications())
                .linkedinUrl(StringUtils.hasText(extraction.getLinkedinUrl()) ? extraction.getLinkedinUrl() : null)
                .aiSummary(extraction.getAiSummary())
                .duplicateHash(duplicateHash)
                .build();
        candidateRepository.save(candidate);

        if (jobDescriptionId != null) {
            JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));
            CandidateScoreDto score = candidateScoreService.scoreCandidate(candidate.getId(), jd.getId(), rawText);
            log.info("Initial AI score for candidate {}: {}", candidate.getId(), score.getMatchScore());
        }

        return ResumeUploadResponse.builder()
                .resumeId(resume.getId())
                .candidateId(candidate.getId())
                .message("Resume processed successfully")
                .possibleDuplicate(duplicate.isPresent())
                .build();
    }
}
