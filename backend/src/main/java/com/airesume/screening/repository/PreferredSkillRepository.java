package com.airesume.screening.repository;

import com.airesume.screening.entity.PreferredSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferredSkillRepository extends JpaRepository<PreferredSkill, Long> {

    List<PreferredSkill> findByActiveTrueOrderByNameAsc();
}
