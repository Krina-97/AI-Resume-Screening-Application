package com.airesume.screening.service;

import com.airesume.screening.dto.DepartmentJobTemplateDto;
import com.airesume.screening.exception.ApiException;
import com.airesume.screening.repository.DepartmentJobTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentJobTemplateService {

    private final DepartmentJobTemplateRepository templateRepository;

    public DepartmentJobTemplateService(DepartmentJobTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public DepartmentJobTemplateDto getByDepartment(String departmentName) {
        return templateRepository.findByDepartmentNameAndActiveTrue(departmentName)
                .map(t -> DepartmentJobTemplateDto.builder()
                        .departmentName(t.getDepartmentName())
                        .title(t.getTitle())
                        .description(t.getDescription())
                        .requiredSkills(t.getRequiredSkills())
                        .preferredSkills(t.getPreferredSkills())
                        .build())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "No job template found for department: " + departmentName));
    }
}
