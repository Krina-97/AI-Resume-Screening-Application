package com.airesume.screening.controller;

import com.airesume.screening.dto.DepartmentDto;
import com.airesume.screening.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "List active departments for job posting")
    public List<DepartmentDto> listActive() {
        return departmentService.listActive();
    }
}
