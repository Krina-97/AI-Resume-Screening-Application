package com.airesume.screening.service;

import com.airesume.screening.dto.JobDescriptionDto;
import com.airesume.screening.dto.JobDescriptionRequest;
import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.mapper.JobDescriptionMapper;
import com.airesume.screening.entity.Candidate;
import com.airesume.screening.repository.CandidateRepository;
import com.airesume.screening.repository.CandidateScoreRepository;
import com.airesume.screening.repository.JobDescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateScoreRepository candidateScoreRepository;
    private final JobDescriptionMapper jobDescriptionMapper;

    public JobDescriptionService(JobDescriptionRepository jobDescriptionRepository,
                                 CandidateRepository candidateRepository,
                                 CandidateScoreRepository candidateScoreRepository,
                                 JobDescriptionMapper jobDescriptionMapper) {
        this.jobDescriptionRepository = jobDescriptionRepository;
        this.candidateRepository = candidateRepository;
        this.candidateScoreRepository = candidateScoreRepository;
        this.jobDescriptionMapper = jobDescriptionMapper;
    }

    public List<JobDescriptionDto> listActive() {
        return jobDescriptionRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(jobDescriptionMapper::toDto)
                .toList();
    }

    public List<JobDescriptionDto> listAll() {
        return jobDescriptionRepository.findAll().stream()
                .map(jobDescriptionMapper::toDto)
                .toList();
    }

    public JobDescriptionDto get(Long id) {
        JobDescription jd = jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));
        return jobDescriptionMapper.toDto(jd);
    }

    @Transactional
    public JobDescriptionDto create(JobDescriptionRequest request, Long userId) {
        JobDescription entity = jobDescriptionMapper.toEntity(request);
        entity.setCreatedBy(userId);
        return jobDescriptionMapper.toDto(jobDescriptionRepository.save(entity));
    }

    @Transactional
    public JobDescriptionDto update(Long id, JobDescriptionRequest request) {
        JobDescription entity = jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));
        jobDescriptionMapper.updateEntity(request, entity);
        return jobDescriptionMapper.toDto(jobDescriptionRepository.save(entity));
    }

    public JobDescription getEntity(Long id) {
        return jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));
    }

    @Transactional
    public void delete(Long id) {
        jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job description not found"));

        candidateScoreRepository.deleteByJobDescriptionId(id);
        for (Candidate candidate : candidateRepository.findByJobDescriptionIdOrderByCreatedAtDesc(id)) {
            candidate.setJobDescriptionId(null);
            candidateRepository.save(candidate);
        }
        jobDescriptionRepository.deleteById(id);
    }
}
