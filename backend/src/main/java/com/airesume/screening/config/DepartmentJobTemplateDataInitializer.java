package com.airesume.screening.config;

import com.airesume.screening.entity.DepartmentJobTemplate;
import com.airesume.screening.repository.DepartmentJobTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class DepartmentJobTemplateDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DepartmentJobTemplateDataInitializer.class);

    private record TemplateSeed(
            String departmentName,
            String title,
            String description,
            String requiredSkills,
            String preferredSkills) {}

    @Bean
    @Order(2)
    public CommandLineRunner seedDepartmentJobTemplates(DepartmentJobTemplateRepository repository) {
        return args -> {
            List<TemplateSeed> templates = List.of(
                    new TemplateSeed(
                            "Data & Analytics",
                            "Data & Analytics Associate / Analyst",
                            """
                                    Job Summary:
                                    We are seeking a detail-oriented and analytical professional to join our Data & Analytics team. \
                                    The candidate will be responsible for collecting, analyzing, and interpreting data to support \
                                    business decision-making and improve operational efficiency.

                                    Key Responsibilities:
                                    - Analyze large datasets to identify trends and insights
                                    - Develop dashboards and reports using BI tools
                                    - Work with stakeholders to understand business requirements
                                    - Ensure data accuracy, integrity, and consistency
                                    - Support data-driven strategic initiatives
                                    """,
                            "SQL & Database Management, Data Visualization, Statistical Analysis, Analytical Skills, Problem Solving",
                            "Python/R Programming, Machine Learning Basics, Cloud Computing"
                    ),
                    new TemplateSeed(
                            "Engineering",
                            "Software Engineer / Engineering Associate",
                            """
                                    Job Summary:
                                    We are looking for a motivated Engineering professional to design, develop, and maintain software \
                                    applications and systems. The ideal candidate should possess strong technical expertise and \
                                    problem-solving capabilities.

                                    Key Responsibilities:
                                    - Develop and maintain scalable applications
                                    - Collaborate with cross-functional teams for project delivery
                                    - Troubleshoot and debug technical issues
                                    - Participate in code reviews and testing activities
                                    - Ensure software quality and performance standards
                                    """,
                            "Programming & Development, Problem Solving, System Design",
                            "Cloud Computing, DevOps Practices, Agile/Scrum Methodology"
                    ),
                    new TemplateSeed(
                            "Finance & Accounting",
                            "Finance & Accounting Executive",
                            """
                                    Job Summary:
                                    We are seeking a Finance & Accounting professional responsible for financial reporting, budgeting, \
                                    compliance, and maintaining accurate financial records for the organization.

                                    Key Responsibilities:
                                    - Prepare financial statements and reports
                                    - Manage budgeting and forecasting activities
                                    - Ensure compliance with accounting standards and regulations
                                    - Monitor accounts payable and receivable
                                    - Assist in audits and financial analysis
                                    """,
                            "Financial Reporting, Budgeting & Forecasting, Analytical Skills",
                            "ERP Tools (SAP/Oracle), Taxation Knowledge, Risk Management"
                    ),
                    new TemplateSeed(
                            "Marketing",
                            "Marketing Executive / Marketing Associate",
                            """
                                    Job Summary:
                                    We are looking for a creative and result-driven Marketing professional to develop and execute \
                                    marketing strategies that increase brand awareness and customer engagement.

                                    Key Responsibilities:
                                    - Plan and execute marketing campaigns
                                    - Conduct market and competitor research
                                    - Manage social media and digital marketing activities
                                    - Coordinate with design and sales teams
                                    - Analyze campaign performance metrics
                                    """,
                            "Digital Marketing, Communication & Branding, Market Research",
                            "SEO/SEM Knowledge, Content Creation, CRM Tools (HubSpot/Salesforce)"
                    ),
                    new TemplateSeed(
                            "Operations",
                            "Operations Executive / Operations Coordinator",
                            """
                                    Job Summary:
                                    We are seeking an organized and proactive Operations professional to oversee daily operational \
                                    activities and improve business processes for maximum efficiency.

                                    Key Responsibilities:
                                    - Monitor and improve operational workflows
                                    - Coordinate with internal teams and vendors
                                    - Track operational KPIs and performance
                                    - Ensure timely delivery of services/projects
                                    - Identify areas for process optimization
                                    """,
                            "Process Optimization, Project Management, Analytical Skills",
                            "Lean Six Sigma, Vendor Management, Data-Driven Decision Making"
                    )
            );

            templates.forEach(seed -> {
                DepartmentJobTemplate entity = repository.findByDepartmentName(seed.departmentName())
                        .orElse(DepartmentJobTemplate.builder()
                                .departmentName(seed.departmentName())
                                .build());
                entity.setTitle(seed.title());
                entity.setDescription(seed.description().trim());
                entity.setRequiredSkills(seed.requiredSkills());
                entity.setPreferredSkills(seed.preferredSkills());
                entity.setActive(true);
                repository.save(entity);
            });

            log.info("Synced {} department job templates", templates.size());
        };
    }
}
