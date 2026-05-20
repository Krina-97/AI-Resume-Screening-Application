package com.airesume.screening.config;

import com.airesume.screening.entity.Skill;
import com.airesume.screening.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class SkillDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SkillDataInitializer.class);

    private static final List<String> DEFAULT_SKILLS = List.of(
            "Data Visualization",
            "SQL & Database Management",
            "Statistical Analysis",
            "Problem Solving",
            "Programming & Development",
            "System Design",
            "Financial Reporting",
            "Budgeting & Forecasting",
            "Analytical Skills",
            "Digital Marketing",
            "Market Research",
            "Communication & Branding",
            "Process Optimization",
            "Project Management",
            "Supply Chain/Resource Planning"
    );

    @Bean
    @Order(0)
    public CommandLineRunner seedSkills(SkillRepository skillRepository) {
        return args -> {
            if (skillRepository.count() > 0) {
                return;
            }
            DEFAULT_SKILLS.forEach(name -> skillRepository.save(
                    Skill.builder().name(name).active(true).build()));
            log.info("Seeded {} skills", DEFAULT_SKILLS.size());
        };
    }
}
