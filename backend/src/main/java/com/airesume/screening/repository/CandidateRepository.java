package com.airesume.screening.repository;

import com.airesume.screening.entity.Candidate;
import com.airesume.screening.entity.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    Optional<Candidate> findByDuplicateHash(String duplicateHash);

    List<Candidate> findByJobDescriptionIdOrderByCreatedAtDesc(Long jobDescriptionId);

    List<Candidate> findByStatus(CandidateStatus status);

    long countByStatus(CandidateStatus status);

    @Query("SELECT c FROM Candidate c WHERE " +
           "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.skills) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Candidate> searchByQuery(@Param("query") String query);
}
