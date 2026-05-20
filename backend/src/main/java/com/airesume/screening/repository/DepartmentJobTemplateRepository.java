package com.airesume.screening.repository;

import com.airesume.screening.entity.DepartmentJobTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentJobTemplateRepository extends JpaRepository<DepartmentJobTemplate, Long> {

    Optional<DepartmentJobTemplate> findByDepartmentNameAndActiveTrue(String departmentName);

    Optional<DepartmentJobTemplate> findByDepartmentName(String departmentName);
}
