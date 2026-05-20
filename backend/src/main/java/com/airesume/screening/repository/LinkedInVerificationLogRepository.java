package com.airesume.screening.repository;

import com.airesume.screening.entity.LinkedInVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkedInVerificationLogRepository extends JpaRepository<LinkedInVerificationLog, Long> {
    List<LinkedInVerificationLog> findByCandidateIdOrderByVerifiedAtDesc(Long candidateId);
}
