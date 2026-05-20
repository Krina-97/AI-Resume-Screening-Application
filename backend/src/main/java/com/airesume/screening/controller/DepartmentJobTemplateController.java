package com.airesume.screening.controller;

import com.airesume.screening.dto.DepartmentJobTemplateDto;
import com.airesume.screening.service.DepartmentJobTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/department-templates")
public class DepartmentJobTemplateController {

    private final DepartmentJobTemplateService templateService;

    public DepartmentJobTemplateController(DepartmentJobTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    @Operation(summary = "Get job description template for a department")
    public DepartmentJobTemplateDto getByDepartment(@RequestParam String department) {
        return templateService.getByDepartment(department);
    }
}
