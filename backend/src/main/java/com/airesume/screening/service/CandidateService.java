package com.airesume.screening.service;

import com.airesume.screening.dto.CandidateDto;
import com.airesume.screening.dto.CandidateScoreDto;
import com.airesume.screening.dto.CandidateUpdateRequest;
import com.airesume.screening.dto.InterviewScheduleRequest;
import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.CandidateStatus;
import com.airesume.screening.entity.InterviewStatus;
import com.airesume.screening.entity.InterviewStatusType;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.entity.CandidateScore;
import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.CandidateScoreRepository;
import com.airesume.screening.repository.InterviewStatusRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import com.airesume.screening.repository.LinkedInVerificationLogRepository;
import com.airesume.screening.repository.ResumeRepository;
import com.airesume.screening.utils.TextFormatUtils;
import com.airesume.screening.specification.CandidateSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateScoreRepository candidateScoreRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final InterviewStatusRepository interviewStatusRepository;
    private final LinkedInVerificationLogRepository linkedInVerificationLogRepository;
    private final ResumeRepository resumeRepository;
    private final EmailNotificationService emailNotificationService;

    public CandidateService(CandidateRepository candidateRepository,
                            CandidateScoreRepository candidateScoreRepository,
                            JobDescriptionRepository jobDescriptionRepository,
                            InterviewStatusRepository interviewStatusRepository,
                            LinkedInVerificationLogRepository linkedInVerificationLogRepository,
                            ResumeRepository resumeRepository,
                            EmailNotificationService emailNotificationService) {
        this.candidateRepository = candidateRepository;
        this.candidateScoreRepository = candidateScoreRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.interviewStatusRepository = interviewStatusRepository;
        this.linkedInVerificationLogRepository = linkedInVerificationLogRepository;
        this.resumeRepository = resumeRepository;
        this.emailNotificationService = emailNotificationService;
    }

    public List<CandidateDto> search(Long jobId, CandidateStatus status, String query) {
        Specification<Candidate> spec = CandidateSpecifications.filter(jobId, status, query);
        return candidateRepository.findAll(spec).stream()
                .map(this::toDto)
                .toList();
    }

    public CandidateDto get(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));
        return toDto(candidate);
    }

    @Transactional
    public CandidateDto update(Long id, CandidateUpdateRequest request) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));
        if (request.getStatus() != null) {
            candidate.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getLinkedinUrl())) {
            candidate.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getHrNotes() != null) {
            candidate.setHrNotes(request.getHrNotes());
        }
        return toDto(candidateRepository.save(candidate));
    }

    @Transactional
    public void delete(Long id) {
        performDelete(id);
    }

    @Transactional
    public void deleteMany(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            performDelete(id);
        }
    }

    /**
     * Deletes a candidate and related rows. Uses deleteById (not delete(entity)) to avoid
     * Hibernate stale-state errors when the persistence context is out of sync.
     */
    private void performDelete(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));
        Long resumeId = candidate.getResumeId();

        candidateScoreRepository.deleteByCandidateId(id);
        interviewStatusRepository.deleteByCandidateId(id);
        linkedInVerificationLogRepository.deleteByCandidateId(id);

        candidateRepository.deleteById(id);
        candidateRepository.flush();

        if (resumeId != null) {
            resumeRepository.findById(resumeId).ifPresent(resume -> {
                deleteResumeFile(resume.getFilePath());
                resumeRepository.deleteById(resumeId);
            });
        }
    }

    private void deleteResumeFile(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException ignored) {
            // Best-effort file cleanup; DB row is still removed.
        }
    }

    @Transactional
    public void scheduleInterview(Long candidateId, InterviewScheduleRequest request) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));

        InterviewStatus interview = InterviewStatus.builder()
                .candidateId(candidateId)
                .scheduledAt(request.getScheduledAt())
                .interviewerName(request.getInterviewerName())
                .interviewType(request.getInterviewType())
                .location(request.getLocation())
                .meetingLink(request.getMeetingLink())
                .notes(request.getNotes())
                .status(request.getStatus() != null ? request.getStatus() : InterviewStatusType.SCHEDULED)
                .build();
        interviewStatusRepository.save(interview);

        candidate.setStatus(CandidateStatus.INTERVIEW_SCHEDULED);
        candidateRepository.save(candidate);

        if (StringUtils.hasText(candidate.getEmail())) {
            emailNotificationService.sendInterviewInvite(candidate, interview);
        }
    }

    public List<CandidateScoreDto> compareAcrossJobs(Long candidateId, List<Long> jobIds) {
        candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));
        return jobIds.stream()
                .map(jobId -> candidateScoreRepository.findByCandidateIdAndJobDescriptionId(candidateId, jobId)
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(score -> CandidateScoreDto.builder()
                        .id(score.getId())
                        .candidateId(score.getCandidateId())
                        .jobDescriptionId(score.getJobDescriptionId())
                        .matchScore(score.getMatchScore())
                        .missingSkills(score.getMissingSkills())
                        .matchingSkills(score.getMatchingSkills())
                        .fitmentSummary(score.getFitmentSummary())
                        .recommendation(score.getRecommendation())
                        .interviewPros(splitScoreLines(score.getInterviewPros()))
                        .interviewCons(splitScoreLines(score.getInterviewCons()))
                        .scoredAt(score.getScoredAt())
                        .build())
                .toList();
    }

    public List<CandidateDto> topForJob(Long jobId, int limit) {
        return candidateScoreRepository.findByJobDescriptionIdOrderByMatchScoreDesc(jobId).stream()
                .limit(limit)
                .map(score -> candidateRepository.findById(score.getCandidateId())
                        .map(this::toDto)
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private CandidateDto toDto(Candidate candidate) {
        var latestScore = candidateScoreRepository.findFirstByCandidateIdOrderByScoredAtDesc(candidate.getId());
        BigDecimal latest = latestScore.map(CandidateScore::getMatchScore).orElse(null);
        String jobTitle = resolveJobTitle(candidate.getJobDescriptionId(), latestScore);
        var overview = CandidateOverviewBuilder.build(candidate, latestScore.orElse(null), jobTitle);

        return CandidateDto.builder()
                .id(candidate.getId())
                .resumeId(candidate.getResumeId())
                .jobDescriptionId(candidate.getJobDescriptionId())
                .fullName(TextFormatUtils.normalizeDisplayText(candidate.getFullName()))
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .skills(TextFormatUtils.normalizeDisplayText(candidate.getSkills()))
                .experience(TextFormatUtils.normalizeDisplayText(candidate.getExperience()))
                .education(TextFormatUtils.normalizeDisplayText(candidate.getEducation()))
                .certifications(TextFormatUtils.normalizeDisplayText(candidate.getCertifications()))
                .linkedinUrl(candidate.getLinkedinUrl())
                .aiSummary(TextFormatUtils.normalizeDisplayText(candidate.getAiSummary()))
                .hrNotes(candidate.getHrNotes())
                .overview(overview)
                .status(candidate.getStatus())
                .latestMatchScore(latest)
                .matchingSkills(latestScore.map(CandidateScore::getMatchingSkills).orElse(null))
                .missingSkills(latestScore.map(CandidateScore::getMissingSkills).orElse(null))
                .createdAt(candidate.getCreatedAt())
                .build();
    }

    private static List<String> splitScoreLines(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return java.util.Arrays.stream(raw.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String resolveJobTitle(Long jobId, java.util.Optional<CandidateScore> latestScore) {
        Long effectiveJobId = jobId != null ? jobId : latestScore.map(CandidateScore::getJobDescriptionId).orElse(null);
        if (effectiveJobId == null) {
            return null;
        }
        return jobDescriptionRepository.findById(effectiveJobId).map(JobDescription::getTitle).orElse(null);
    }
}
