package com.airesume.screening.repository;

import com.airesume.screening.entity.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewStatusRepository extends JpaRepository<InterviewStatus, Long> {
    List<InterviewStatus> findByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);
}
