package com.airesume.screening.service;

import com.airesume.screening.dto.DepartmentJobTemplateDto;
import com.airesume.screening.dto.JobDescriptionAssistRequest;
import com.airesume.screening.dto.JobDescriptionAssistResponse;
import com.airesume.screening.repository.PreferredSkillRepository;
import com.airesume.screening.repository.SkillRepository;
import com.airesume.screening.service.ai.AiEngineService;
import com.airesume.screening.service.ai.JobDescriptionAssistResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobDescriptionAssistantService {

    private final DepartmentJobTemplateService templateService;
    private final SkillRepository skillRepository;
    private final PreferredSkillRepository preferredSkillRepository;
    private final AiEngineService aiEngineService;

    public JobDescriptionAssistantService(DepartmentJobTemplateService templateService,
                                          SkillRepository skillRepository,
                                          PreferredSkillRepository preferredSkillRepository,
                                          AiEngineService aiEngineService) {
        this.templateService = templateService;
        this.skillRepository = skillRepository;
        this.preferredSkillRepository = preferredSkillRepository;
        this.aiEngineService = aiEngineService;
    }

    public JobDescriptionAssistResponse assist(JobDescriptionAssistRequest request) {
        DepartmentJobTemplateDto template = templateService.getByDepartment(request.getDepartment());
        List<String> requiredCatalog = skillRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(s -> s.getName())
                .toList();
        List<String> preferredCatalog = preferredSkillRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(s -> s.getName())
                .toList();

        boolean regenerate = Boolean.TRUE.equals(request.getRegenerate());
        int variant = request.getVariant() != null ? request.getVariant() : 0;

        JobDescriptionAssistResult aiResult = aiEngineService.generateJobDescription(
                request.getDepartment(),
                template.getTitle(),
                template.getDescription(),
                template.getRequiredSkills(),
                template.getPreferredSkills(),
                requiredCatalog,
                preferredCatalog,
                request.getTitle(),
                request.getLocation(),
                request.getExperienceRequired(),
                regenerate,
                variant
        );

        List<String> requiredSkills = filterToCatalog(aiResult.getRequiredSkills(), requiredCatalog);
        List<String> preferredSkills = filterToCatalog(aiResult.getPreferredSkills(), preferredCatalog);

        if (requiredSkills.isEmpty()) {
            requiredSkills = filterToCatalog(parseCsv(template.getRequiredSkills()), requiredCatalog);
        }
        if (preferredSkills.isEmpty()) {
            preferredSkills = filterToCatalog(parseCsv(template.getPreferredSkills()), preferredCatalog);
        }

        String title = StringUtils.hasText(aiResult.getTitle())
                ? aiResult.getTitle()
                : (StringUtils.hasText(request.getTitle()) ? request.getTitle() : template.getTitle());

        String description = StringUtils.hasText(aiResult.getDescription())
                ? aiResult.getDescription()
                : com.airesume.screening.service.DepartmentJobDescriptionBuilder.build(
                        request.getDepartment(),
                        request.getLocation(),
                        request.getExperienceRequired(),
                        variant,
                        template.getRequiredSkills(),
                        template.getPreferredSkills());

        return JobDescriptionAssistResponse.builder()
                .title(title)
                .description(description)
                .requiredSkills(requiredSkills)
                .preferredSkills(preferredSkills)
                .assistantMessage(aiResult.getAssistantMessage())
                .aiGenerated(aiResult.isAiGenerated())
                .build();
    }

    private List<String> parseCsv(String csv) {
        if (!StringUtils.hasText(csv)) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private List<String> filterToCatalog(List<String> skills, List<String> catalog) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        Set<String> allowed = catalog.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return skills.stream()
                .filter(s -> allowed.contains(s.toLowerCase()))
                .distinct()
                .toList();
    }
}
