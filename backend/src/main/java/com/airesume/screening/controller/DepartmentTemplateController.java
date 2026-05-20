package com.airesume.screening.controller;

import com.airesume.screening.dto.DepartmentTemplateDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/department-templates")
public class DepartmentTemplateController {

    @GetMapping("/catalog")
    @Operation(summary = "Starter job templates (all departments)")
    public List<DepartmentTemplateDto> list() {
        return List.of(
                DepartmentTemplateDto.builder()
                        .id(1L)
                        .departmentName("Engineering")
                        .title("Senior Java Backend Engineer")
                        .requiredSkills("Java, Spring Boot, REST APIs, SQL, MySQL, Git, unit testing, microservices")
                        .preferredSkills("React, Docker, Kubernetes, AWS, CI/CD, Redis")
                        .description("""
                                Build and maintain HR and analytics APIs for our resume screening platform.

                                Responsibilities:
                                - Design REST APIs with Java 17 and Spring Boot
                                - Implement JWT security, validation, and layered services
                                - Integrate AI resume parsing and job matching
                                - Write tests and participate in code reviews

                                Requirements: 5+ years backend development, strong Java/Spring, SQL/MySQL.
                                """)
                        .build(),
                DepartmentTemplateDto.builder()
                        .id(2L)
                        .departmentName("Product Engineering")
                        .title("Frontend React Developer")
                        .requiredSkills("React, TypeScript, HTML, CSS, REST APIs, responsive design, Git")
                        .preferredSkills("Tailwind CSS, Vite, Jest, React Router, Recharts, accessibility (WCAG)")
                        .description("""
                                Deliver modern HR dashboards for resume screening and candidate ranking.

                                Responsibilities:
                                - Build UI with React, TypeScript, and Tailwind
                                - Integrate Spring Boot APIs for jobs, candidates, uploads
                                - Implement tables, filters, and recruiter workflows

                                Requirements: 3+ years React, TypeScript, REST integration.
                                """)
                        .build(),
                DepartmentTemplateDto.builder()
                        .id(3L)
                        .departmentName("Data & AI")
                        .title("ML Engineer — Resume Intelligence")
                        .requiredSkills("Python, NLP, LLM APIs, prompt engineering, data pipelines, evaluation metrics")
                        .preferredSkills("Java integration, PDF/DOCX parsing, RAG, MLOps, Gemini/OpenAI SDKs")
                        .description("""
                                Improve resume extraction, job-fit scoring, and interview recommendations.

                                Responsibilities:
                                - Tune prompts and evaluate model outputs for HR use cases
                                - Build guardrails for PII and scoring consistency
                                - Partner with backend on API contracts for AI features

                                Requirements: experience shipping NLP/LLM features in production.
                                """)
                        .build()
        );
    }
}
