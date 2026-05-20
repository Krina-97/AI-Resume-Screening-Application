package com.airesume.screening.controller;

import com.airesume.screening.dto.SkillDto;
import com.airesume.screening.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    @Operation(summary = "List active skills for job posting")
    public List<SkillDto> listActive() {
        return skillService.listActive();
    }
}
