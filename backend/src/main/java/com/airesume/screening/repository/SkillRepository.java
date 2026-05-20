package com.airesume.screening.repository;

import com.airesume.screening.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByActiveTrueOrderByNameAsc();
}
