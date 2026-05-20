package com.airesume.screening.config;

import com.airesume.screening.entity.JobDescription;
import com.airesume.screening.repository.JobDescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class JobDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(JobDataInitializer.class);

    @Bean
    @Order(1)
    public CommandLineRunner seedJobs(JobDescriptionRepository jobDescriptionRepository) {
        return args -> {
            if (jobDescriptionRepository.count() > 0) {
                return;
            }

            jobDescriptionRepository.save(JobDescription.builder()
                    .title("Senior Java Backend Engineer")
                    .department("Engineering")
                    .location("Remote (India)")
                    .experienceRequired("6-10 years")
                    .requiredSkills("Java, Spring Boot, REST APIs, SQL, MySQL, Git, unit testing, microservices")
                    .preferredSkills("React, Docker, Kubernetes, AWS, CI/CD, Redis")
                    .description("""
                            We are hiring a Senior Java Backend Engineer to build and maintain HR and analytics APIs \
                            for our resume screening platform.

                            Responsibilities:
                            - Design and develop REST APIs using Java 17 and Spring Boot
                            - Implement business logic, validation, and security (JWT)
                            - Work with MySQL and JPA/Hibernate for persistent data
                            - Integrate third-party AI services for resume parsing and job matching
                            - Write unit and integration tests; participate in code reviews
                            - Collaborate with frontend (React) and DevOps on deployment

                            Requirements:
                            - 5+ years of professional backend development
                            - Strong Java and Spring Boot experience
                            - Solid SQL and relational database design (MySQL preferred)
                            - Experience with REST, JSON, and API documentation (Swagger/OpenAPI)
                            - Understanding of authentication, error handling, and layered architecture

                            Nice to have:
                            - Docker, Kubernetes, or cloud (AWS/GCP)
                            - Experience with HR/recruitment or document processing (PDF/DOCX)
                            - Familiarity with React or TypeScript for full-stack collaboration
                            """)
                    .active(true)
                    .build());

            jobDescriptionRepository.save(JobDescription.builder()
                    .title("Frontend React Developer")
                    .department("Product Engineering")
                    .location("Hybrid — Bangalore")
                    .experienceRequired("3-5 years")
                    .requiredSkills("React, TypeScript, HTML, CSS, REST APIs, responsive design, Git")
                    .preferredSkills("Tailwind CSS, Vite, Jest, React Router, Recharts, accessibility (WCAG)")
                    .description("""
                            Join the product team to deliver modern HR dashboards for resume screening and candidate ranking.

                            Responsibilities:
                            - Build responsive UI with React 18, TypeScript, and Tailwind CSS
                            - Integrate with Spring Boot REST APIs (auth, jobs, candidates, uploads)
                            - Implement forms, tables, filters, and data visualizations for recruiters
                            - Improve UX for resume upload, job selection, and pipeline status updates
                            - Write component tests and fix accessibility and performance issues

                            Requirements:
                            - 3+ years frontend development with React
                            - Strong TypeScript and modern CSS (Flexbox/Grid)
                            - Experience consuming REST APIs and handling auth tokens
                            - Attention to detail for enterprise HR workflows

                            Nice to have:
                            - Vite, React Router, chart libraries (Recharts)
                            - Experience with design systems or dark mode theming
                            - Basic understanding of Java/Spring Boot APIs
                            """)
                    .active(true)
                    .build());

            jobDescriptionRepository.save(JobDescription.builder()
                    .title("Data Analyst — HR Analytics")
                    .department("Analytics")
                    .location("On-site — Mumbai")
                    .experienceRequired("3-5 years")
                    .requiredSkills("SQL, Excel, Python, statistics, data visualization, Power BI, reporting")
                    .preferredSkills("Tableau, ETL, MySQL, pandas, hiring/recruitment metrics")
                    .description("""
                            Support HR leadership with hiring funnel analytics, candidate pipeline reporting, \
                            and data quality for the AI resume screening platform.

                            Responsibilities:
                            - Write SQL queries against MySQL for candidates, jobs, scores, and statuses
                            - Build Power BI dashboards for time-to-hire, source quality, and match score trends
                            - Clean and validate candidate and job data imported from resumes and uploads
                            - Partner with engineering to define metrics and export requirements
                            - Present insights to HR and hiring managers in clear, actionable language

                            Requirements:
                            - 2+ years in analytics or business intelligence
                            - Strong SQL and Excel; comfortable with Python for ad-hoc analysis
                            - Experience building dashboards and explaining data to non-technical stakeholders
                            - Basic statistics (distributions, trends, cohort analysis)

                            Nice to have:
                            - Tableau or similar BI tools
                            - Familiarity with HR/recruitment KPIs
                            - Experience with ETL or data modeling
                            """)
                    .active(true)
                    .build());

            log.info("Seeded 3 sample job descriptions for AI resume evaluation");
        };
    }
}
