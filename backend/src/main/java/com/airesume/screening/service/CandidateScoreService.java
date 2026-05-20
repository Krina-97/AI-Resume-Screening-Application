package com.airesume.screening.service;

import com.airesume.screening.dto.CandidateScoreDto;
import com.airesume.screening.entity.CandidateScore;
import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.CandidateScoreRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import com.airesume.screening.repository.ResumeRepository;
import com.airesume.screening.service.ai.AiEngineService;
import com.airesume.screening.service.ai.ResumeMatchResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateScoreService {

    private final CandidateScoreRepository candidateScoreRepository;
    private final CandidateRepository candidateRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeRepository resumeRepository;
    private final AiEngineService aiEngineService;

    public CandidateScoreService(CandidateScoreRepository candidateScoreRepository,
                                 CandidateRepository candidateRepository,
                                 JobDescriptionRepository jobDescriptionRepository,
                                 ResumeRepository resumeRepository,
                                 AiEngineService aiEngineService) {
        this.candidateScoreRepository = candidateScoreRepository;
        this.candidateRepository = candidateRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.resumeRepository = resumeRepository;
        this.aiEngineService = aiEngineService;
    }

    @Transactional
    public CandidateScoreDto scoreCandidate(Long candidateId, Long jobDescriptionId, String resumeTextOverride) {
        var candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Candidate not found"));
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));

        String resumeText = resumeTextOverride;
        if (resumeText == null) {
            var resume = resumeRepository.findById(candidate.getResumeId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Resume not found"));
            resumeText = resume.getRawText();
        }

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

        CandidateScore saved = candidateScoreRepository.save(score);

        candidate.setJobDescriptionId(jobDescriptionId);
        candidateRepository.save(candidate);

        return toDto(saved);
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
                .scoredAt(score.getScoredAt())
                .build();
    }
}
