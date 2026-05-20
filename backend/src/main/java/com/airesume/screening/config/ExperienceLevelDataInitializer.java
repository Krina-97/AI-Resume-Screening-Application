package com.airesume.screening.config;

import com.airesume.screening.entity.ExperienceLevel;
import com.airesume.screening.repository.ExperienceLevelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class ExperienceLevelDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(ExperienceLevelDataInitializer.class);

    private record LevelSeed(String name, int displayOrder) {}

    private static final List<LevelSeed> DEFAULT_LEVELS = List.of(
            new LevelSeed("3-5 years", 1),
            new LevelSeed("6-10 years", 2),
            new LevelSeed("10-15 years", 3),
            new LevelSeed("15+ years", 4)
    );

    @Bean
    @Order(0)
    public CommandLineRunner seedExperienceLevels(ExperienceLevelRepository experienceLevelRepository) {
        return args -> {
            if (experienceLevelRepository.count() > 0) {
                return;
            }
            DEFAULT_LEVELS.forEach(level -> experienceLevelRepository.save(
                    ExperienceLevel.builder()
                            .name(level.name())
                            .displayOrder(level.displayOrder())
                            .active(true)
                            .build()));
            log.info("Seeded {} experience levels", DEFAULT_LEVELS.size());
        };
    }
}
