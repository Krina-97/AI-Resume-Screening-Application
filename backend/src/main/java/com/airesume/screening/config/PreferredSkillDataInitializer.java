package com.airesume.screening.config;

import com.airesume.screening.entity.PreferredSkill;
import com.airesume.screening.repository.PreferredSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class PreferredSkillDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(PreferredSkillDataInitializer.class);

    private static final List<String> DEFAULT_PREFERRED_SKILLS = List.of(
            "Python/R Programming",
            "Machine Learning Basics",
            "Dashboard Tools (Power BI/Tableau)",
            "Cloud Computing",
            "DevOps Practices",
            "Agile/Scrum Methodology",
            "ERP Tools (SAP/Oracle)",
            "Taxation Knowledge",
            "Risk Management",
            "SEO/SEM Knowledge",
            "Content Creation",
            "CRM Tools (HubSpot/Salesforce)",
            "Lean Six Sigma",
            "Vendor Management",
            "Data-Driven Decision Making"
    );

    @Bean
    @Order(0)
    public CommandLineRunner seedPreferredSkills(PreferredSkillRepository preferredSkillRepository) {
        return args -> {
            if (preferredSkillRepository.count() > 0) {
                return;
            }
            DEFAULT_PREFERRED_SKILLS.forEach(name -> preferredSkillRepository.save(
                    PreferredSkill.builder().name(name).active(true).build()));
            log.info("Seeded {} preferred skills", DEFAULT_PREFERRED_SKILLS.size());
        };
    }
}
