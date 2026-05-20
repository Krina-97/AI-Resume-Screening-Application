package com.airesume.screening.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DepartmentTemplateDto {
    long id;
    String departmentName;
    String title;
    String description;
    String requiredSkills;
    String preferredSkills;
}
