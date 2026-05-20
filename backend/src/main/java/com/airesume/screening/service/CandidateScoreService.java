package com.airesume.screening.service;

import com.airesume.screening.dto.CandidateScoreDto;
import com.airesume.screening.entity.CandidateScore;
import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.CandidateScoreRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import com.airesume.screening.repository.ResumeRepository;
import com.airesume.screening.entity.Resume;
import com.airesume.screening.service.ai.AiEngineService;
import com.airesume.screening.service.ai.ResumeMatchResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Path;

@Service
public class CandidateScoreService {

    private final CandidateScoreRepository candidateScoreRepository;
    private final CandidateRepository candidateRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeTextExtractorService resumeTextExtractorService;
    private final AiEngineService aiEngineService;

    public CandidateScoreService(CandidateScoreRepository candidateScoreRepository,
                                 CandidateRepository candidateRepository,
                                 JobDescriptionRepository jobDescriptionRepository,
                                 ResumeRepository resumeRepository,
                                 ResumeTextExtractorService resumeTextExtractorService,
                                 AiEngineService aiEngineService) {
        this.candidateScoreRepository = candidateScoreRepository;
        this.candidateRepository = candidateRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeRepository = resumeRepository;
        this.resumeTextExtractorService = resumeTextExtractorService;
        this.aiEngineService = aiEngineService;
    }

    @Transactional
    public CandidateScoreDto scoreCandidate(Long candidateId, Long jobDescriptionId, String resumeTextOverride) {
        var candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));

        String resumeText = resolveResumeText(candidate, resumeTextOverride);

        ResumeMatchResult match = aiEngineService.matchResumeToJob(resumeText, jd);

        CandidateScore score = candidateScoreRepository
                .findByCandidateIdAndJobDescriptionId(candidateId, jobDescriptionId)
                .orElse(CandidateScore.builder()
                        .candidateId(candidateId)
                        .jobDescriptionId(jobDescriptionId)
                        .build());

        score.setMatchScore(match.getMatchScore());
        score.setMissingSkills(String.join(", ", match.getMissingSkills()));
        score.setMatchingSkills(String.join(", ", match.getMatchingSkills()));
        score.setFitmentSummary(match.getFitmentSummary());
        score.setRecommendation(match.getRecommendation());
        score.setInterviewPros(joinList(match.getInterviewPros()));
        score.setInterviewCons(joinList(match.getInterviewCons()));

        CandidateScore saved = candidateScoreRepository.save(score);

        candidate.setJobDescriptionId(jobDescriptionId);
        candidate.setAiSummary(CandidateOverviewBuilder.formatStoredOverview(candidate, saved, jd.getTitle()));
        candidateRepository.save(candidate);

        return toDto(saved);
    }

    private String resolveResumeText(com.airesume.screening.entity.Candidate candidate, String resumeTextOverride) {
        if (StringUtils.hasText(resumeTextOverride)) {
            return resumeTextOverride;
        }
        Resume resume = resumeRepository.findById(candidate.getResumeId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
        if (StringUtils.hasText(resume.getRawText())) {
            return resume.getRawText();
        }
        if (StringUtils.hasText(resume.getFilePath())) {
            String extracted = resumeTextExtractorService.extractFromPath(Path.of(resume.getFilePath()));
            if (StringUtils.hasText(extracted)) {
                resume.setRawText(extracted);
                resumeRepository.save(resume);
                return extracted;
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST,
                "Resume text is missing. Re-upload the resume or check the stored file.");
    }

    public CandidateScoreDto getScore(Long candidateId, Long jobDescriptionId) {
        CandidateScore score = candidateScoreRepository
                .findByCandidateIdAndJobDescriptionId(candidateId, jobDescriptionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Score not found"));
        return toDto(score);
    }

    private CandidateScoreDto toDto(CandidateScore score) {
        return CandidateScoreDto.builder()
                .id(score.getId())
                .candidateId(score.getCandidateId())
                .jobDescriptionId(score.getJobDescriptionId())
                .matchScore(score.getMatchScore())
                .missingSkills(score.getMissingSkills())
                .matchingSkills(score.getMatchingSkills())
                .fitmentSummary(score.getFitmentSummary())
                .recommendation(score.getRecommendation())
                .interviewPros(splitList(score.getInterviewPros()))
                .interviewCons(splitList(score.getInterviewCons()))
                .scoredAt(score.getScoredAt())
                .build();
    }

    private static String joinList(java.util.List<String> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return String.join("\n", items);
    }

    private static java.util.List<String> splitList(String raw) {
        if (raw == null || raw.isBlank()) {
            return java.util.List.of();
        }
        return java.util.Arrays.stream(raw.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
