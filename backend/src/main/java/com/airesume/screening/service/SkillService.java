package com.airesume.screening.service;

import com.airesume.screening.dto.SkillDto;
import com.airesume.screening.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<SkillDto> listActive() {
        return skillRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(s -> SkillDto.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .build())
                .toList();
    }
}
