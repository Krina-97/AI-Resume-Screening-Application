package com.airesume.screening.repository;

import com.airesume.screening.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByActiveTrueOrderByCreatedAtDesc();
}
