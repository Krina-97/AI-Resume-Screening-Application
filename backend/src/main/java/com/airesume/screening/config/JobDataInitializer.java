package com.airesume.screening.config;

import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.repository.JobDescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(JobDataInitializer.class);

    @Bean
    public CommandLineRunner seedJobs(JobDescriptionRepository jobDescriptionRepository) {
        return args -> {
            if (jobDescriptionRepository.count() > 0) {
                return;
            }

            jobDescriptionRepository.save(JobDescription.builder()
                    .title("Senior Java Engineer")
                    .department("Engineering")
                    .location("Remote")
                    .experienceRequired("5+ years")
                    .requiredSkills("Java, Spring Boot, SQL, REST, Microservices")
                    .preferredSkills("React, Kubernetes, AWS, Docker")
                    .description("Build and maintain HR analytics microservices. Own API design, code reviews, "
                            + "unit/integration tests, and production support. Mentor junior developers.")
                    .active(true)
                    .build());

            jobDescriptionRepository.save(JobDescription.builder()
                    .title("Frontend React Developer")
                    .department("Product Engineering")
                    .location("Hybrid — Bangalore")
                    .experienceRequired("3+ years")
                    .requiredSkills("React, TypeScript, HTML, CSS, REST APIs")
                    .preferredSkills("Tailwind CSS, Vite, Jest")
                    .description("Implement responsive HR dashboards, integrate with Spring Boot APIs, "
                            + "and improve accessibility and performance.")
                    .active(true)
                    .build());

            jobDescriptionRepository.save(JobDescription.builder()
                    .title("Data Analyst")
                    .department("Analytics")
                    .location("On-site — Mumbai")
                    .experienceRequired("2+ years")
                    .requiredSkills("SQL, Excel, Power BI, Python, Statistics")
                    .preferredSkills("Tableau, ETL")
                    .description("Build reports and dashboards for hiring metrics. "
                            + "Clean and model candidate pipeline data.")
                    .active(true)
                    .build());

            log.info("Seeded 3 sample job descriptions (ids 1–3 on fresh database)");
        };
    }
}
