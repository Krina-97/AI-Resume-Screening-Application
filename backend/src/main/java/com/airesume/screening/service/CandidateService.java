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
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.CandidateScoreRepository;
import com.airesume.screening.repository.InterviewStatusRepository;
import com.airesume.screening.specification.CandidateSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateScoreRepository candidateScoreRepository;
    private final InterviewStatusRepository interviewStatusRepository;
    private final EmailNotificationService emailNotificationService;

    public CandidateService(CandidateRepository candidateRepository,
                            CandidateScoreRepository candidateScoreRepository,
                            InterviewStatusRepository interviewStatusRepository,
                            EmailNotificationService emailNotificationService) {
        this.candidateRepository = candidateRepository;
        this.candidateScoreRepository = candidateScoreRepository;
        this.interviewStatusRepository = interviewStatusRepository;
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
        return toDto(candidateRepository.save(candidate));
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
        BigDecimal latest = candidateScoreRepository.findFirstByCandidateIdOrderByScoredAtDesc(candidate.getId())
                .map(com.airesume.screening.entity.CandidateScore::getMatchScore)
                .orElse(null);

        return CandidateDto.builder()
                .id(candidate.getId())
                .resumeId(candidate.getResumeId())
                .jobDescriptionId(candidate.getJobDescriptionId())
                .fullName(candidate.getFullName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .skills(candidate.getSkills())
                .experience(candidate.getExperience())
                .education(candidate.getEducation())
                .certifications(candidate.getCertifications())
                .linkedinUrl(candidate.getLinkedinUrl())
                .aiSummary(candidate.getAiSummary())
                .status(candidate.getStatus())
                .latestMatchScore(latest)
                .createdAt(candidate.getCreatedAt())
                .build();
    }
}
