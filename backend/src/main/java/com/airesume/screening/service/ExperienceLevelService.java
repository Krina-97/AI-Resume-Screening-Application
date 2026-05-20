package com.airesume.screening.service;

import com.airesume.screening.dto.ExperienceLevelDto;
import com.airesume.screening.repository.ExperienceLevelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExperienceLevelService {

    private final ExperienceLevelRepository experienceLevelRepository;

    public ExperienceLevelService(ExperienceLevelRepository experienceLevelRepository) {
        this.experienceLevelRepository = experienceLevelRepository;
    }

    @Transactional(readOnly = true)
    public List<ExperienceLevelDto> listActive() {
        return experienceLevelRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
                .map(e -> ExperienceLevelDto.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .build())
                .toList();
    }
}
