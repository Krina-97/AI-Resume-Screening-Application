package com.airesume.screening.controller;

import com.airesume.screening.dto.ExperienceLevelDto;
import com.airesume.screening.service.ExperienceLevelService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/experience-levels")
public class ExperienceLevelController {

    private final ExperienceLevelService experienceLevelService;

    public ExperienceLevelController(ExperienceLevelService experienceLevelService) {
        this.experienceLevelService = experienceLevelService;
    }

    @GetMapping
    @Operation(summary = "List active experience ranges for job posting")
    public List<ExperienceLevelDto> listActive() {
        return experienceLevelService.listActive();
    }
}
