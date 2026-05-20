package com.airesume.screening.service;

import com.airesume.screening.dto.CandidateDto;
import com.airesume.screening.dto.DashboardStatsDto;
import com.airesume.screening.dto.JobQueueItemDto;
import com.airesume.screening.dto.WorkQueueDto;
import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.CandidateStatus;
import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.CandidateScoreRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AnalyticsService {

    private final CandidateRepository candidateRepository;
    private final CandidateScoreRepository candidateScoreRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final CandidateService candidateService;

    public AnalyticsService(CandidateRepository candidateRepository,
                            CandidateScoreRepository candidateScoreRepository,
                            JobDescriptionRepository jobDescriptionRepository,
                            CandidateService candidateService) {
        this.candidateRepository = candidateRepository;
        this.candidateScoreRepository = candidateScoreRepository;
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.candidateService = candidateService;
    }

    public DashboardStatsDto dashboard() {
        long total = candidateRepository.count();
        long shortlisted = candidateRepository.countByStatus(CandidateStatus.SHORTLISTED);
        long rejected = candidateRepository.countByStatus(CandidateStatus.REJECTED);
        long interviews = candidateRepository.countByStatus(CandidateStatus.INTERVIEW_SCHEDULED);
        Double avg = candidateScoreRepository.averageScoreOverall();

        Comparator<Candidate> byScoreDesc = Comparator.comparingDouble((Candidate c) ->
                candidateScoreRepository.findFirstByCandidateIdOrderByScoredAtDesc(c.getId())
                        .map(s -> s.getMatchScore().doubleValue())
                        .orElse(0d)).reversed();

        List<CandidateDto> top = candidateRepository.findAll().stream()
                .sorted(byScoreDesc)
                .limit(5)
                .map(c -> candidateService.get(c.getId()))
                .toList();

        WorkQueueDto workQueue = buildWorkQueue();

        return DashboardStatsDto.builder()
                .totalCandidates(total)
                .shortlisted(shortlisted)
                .rejected(rejected)
                .interviewsScheduled(interviews)
                .averageMatchScore(avg)
                .topCandidates(top)
                .workQueue(workQueue)
                .build();
    }

    private WorkQueueDto buildWorkQueue() {
        long newCount = candidateRepository.countByStatus(CandidateStatus.NEW);
        long shortlisted = candidateRepository.countByStatus(CandidateStatus.SHORTLISTED);
        long interviews = candidateRepository.countByStatus(CandidateStatus.INTERVIEW_SCHEDULED);

        List<JobQueueItemDto> emptyJobs = new ArrayList<>();
        for (JobDescription job : jobDescriptionRepository.findByActiveTrueOrderByCreatedAtDesc()) {
            if (candidateRepository.countByJobDescriptionId(job.getId()) == 0) {
                emptyJobs.add(JobQueueItemDto.builder()
                        .id(job.getId())
                        .title(job.getTitle())
                        .build());
            }
        }

        return WorkQueueDto.builder()
                .newCandidates(newCount)
                .shortlisted(shortlisted)
                .interviewScheduled(interviews)
                .jobsWithoutCandidates(emptyJobs.size())
                .emptyJobs(emptyJobs)
                .build();
    }
}
