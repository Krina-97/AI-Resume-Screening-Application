package com.airesume.screening.service;

import com.airesume.screening.dto.PreferredSkillDto;
import com.airesume.screening.repository.PreferredSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PreferredSkillService {

    private final PreferredSkillRepository preferredSkillRepository;

    public PreferredSkillService(PreferredSkillRepository preferredSkillRepository) {
        this.preferredSkillRepository = preferredSkillRepository;
    }

    @Transactional(readOnly = true)
    public List<PreferredSkillDto> listActive() {
        return preferredSkillRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(s -> PreferredSkillDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .build())
                .toList();
    }
}
