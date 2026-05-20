package com.airesume.screening.controller;

import com.airesume.screening.dto.PreferredSkillDto;
import com.airesume.screening.service.PreferredSkillService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/preferred-skills")
public class PreferredSkillController {

    private final PreferredSkillService preferredSkillService;

    public PreferredSkillController(PreferredSkillService preferredSkillService) {
        this.preferredSkillService = preferredSkillService;
    }

    @GetMapping
    @Operation(summary = "List active nice-to-have skills for job posting")
    public List<PreferredSkillDto> listActive() {
        return preferredSkillService.listActive();
    }
}
