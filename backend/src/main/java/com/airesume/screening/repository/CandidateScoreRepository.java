package com.airesume.screening.repository;

import com.airesume.screening.entity.CandidateScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateScoreRepository extends JpaRepository<CandidateScore, Long> {

    List<CandidateScore> findByCandidateId(Long candidateId);

    void deleteByCandidateId(Long candidateId);

    void deleteByJobDescriptionId(Long jobDescriptionId);

    Optional<CandidateScore> findFirstByCandidateIdOrderByScoredAtDesc(Long candidateId);
    Optional<CandidateScore> findByCandidateIdAndJobDescriptionId(Long candidateId, Long jobDescriptionId);

    List<CandidateScore> findByJobDescriptionIdOrderByMatchScoreDesc(Long jobDescriptionId);

    @Query("SELECT AVG(cs.matchScore) FROM CandidateScore cs WHERE cs.jobDescriptionId = :jobId")
    Double averageScoreByJob(@Param("jobId") Long jobId);

    @Query("SELECT AVG(cs.matchScore) FROM CandidateScore cs")
    Double averageScoreOverall();
}
