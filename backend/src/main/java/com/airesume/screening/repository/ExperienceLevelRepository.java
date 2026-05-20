package com.airesume.screening.repository;

import com.airesume.screening.entity.ExperienceLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceLevelRepository extends JpaRepository<ExperienceLevel, Long> {

    List<ExperienceLevel> findByActiveTrueOrderByDisplayOrderAsc();
}
