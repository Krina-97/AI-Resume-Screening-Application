package com.airesume.screening.service;

import org.springframework.util.StringUtils;

/**
 * Builds full, multi-section job descriptions for department templates (with variants for regenerate).
 */
public final class DepartmentJobDescriptionBuilder {

    private DepartmentJobDescriptionBuilder() {
    }

    public static String build(String department,
                               String location,
                               String experience,
                               int variant,
                               String requiredSkillsLine,
                               String preferredSkillsLine) {
        String context = formatContext(location, experience);
        int v = Math.floorMod(variant, 3);

        return switch (department) {
            case "Data & Analytics" -> dataAnalytics(context, requiredSkillsLine, preferredSkillsLine, v);
            case "Engineering" -> engineering(context, requiredSkillsLine, preferredSkillsLine, v);
            case "Finance & Accounting" -> finance(context, requiredSkillsLine, preferredSkillsLine, v);
            case "Marketing" -> marketing(context, requiredSkillsLine, preferredSkillsLine, v);
            case "Operations" -> operations(context, requiredSkillsLine, preferredSkillsLine, v);
            default -> """
                    Job Summary:
                    We are hiring for this role. Please select a supported department.

                    Key Responsibilities:
                    - Contribute to team goals and deliver quality work
                    """;
        };
    }

    private static String formatContext(String location, String experience) {
        StringBuilder ctx = new StringBuilder();
        if (StringUtils.hasText(location)) {
            ctx.append("Location: ").append(location).append(". ");
        }
        if (StringUtils.hasText(experience)) {
            ctx.append("Experience: ").append(experience).append(". ");
        }
        return ctx.toString().trim();
    }

    private static String skillsBlock(String required, String preferred) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(required)) {
            sb.append("\nRequired Skills:\n").append(formatSkillsList(required)).append("\n");
        }
        if (StringUtils.hasText(preferred)) {
            sb.append("\nNice-to-Have Skills:\n").append(formatSkillsList(preferred)).append("\n");
        }
        return sb.toString();
    }

    private static String formatSkillsList(String csv) {
        String[] parts = csv.split(",");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                sb.append("- ").append(part.trim()).append("\n");
            }
        }
        return sb.toString();
    }

    private static String dataAnalytics(String context, String req, String pref, int v) {
        String summary = switch (v) {
            case 1 -> """
                    Job Summary:
                    Join our Data & Analytics function to turn complex data into actionable insights for leadership and operations. \
                    You will partner with business teams to define metrics, build reporting, and improve how we use data across the organization.
                    """;
            case 2 -> """
                    Job Summary:
                    We are expanding our analytics capability and need a hands-on analyst who can own dashboards, ad-hoc analysis, and data quality. \
                    This role is ideal for someone who enjoys storytelling with data and rigorous analytical methods.
                    """;
            default -> """
                    Job Summary:
                    We are seeking a detail-oriented and analytical professional to join our Data & Analytics team. \
                    The candidate will collect, analyze, and interpret data to support business decision-making and improve operational efficiency.
                    """;
        };
        String responsibilities = switch (v) {
            case 1 -> """
                    Key Responsibilities:
                    - Own recurring reports and self-serve dashboards for HR and operations stakeholders
                    - Perform exploratory analysis on hiring funnel, pipeline, and workforce metrics
                    - Document data definitions and maintain a glossary for shared KPIs
                    - Partner with engineering on data extracts and validation rules
                    - Present findings in clear narratives for non-technical audiences
                    """;
            case 2 -> """
                    Key Responsibilities:
                    - Design and maintain Power BI / Tableau assets used by recruiting and HR leadership
                    - Investigate anomalies in candidate and job data; recommend corrective actions
                    - Support A/B tests and cohort analyses for recruitment campaigns
                    - Automate repetitive reporting where possible (SQL, Python, or R)
                    - Coach junior analysts on best practices for visualization and SQL
                    """;
            default -> """
                    Key Responsibilities:
                    - Analyze large datasets to identify trends, patterns, and insights
                    - Develop dashboards and reports using BI tools (Power BI, Tableau, or similar)
                    - Work with stakeholders to understand business requirements and success metrics
                    - Ensure data accuracy, integrity, and consistency across sources
                    - Support data-driven strategic initiatives and executive reporting
                    """;
        };
        return header(context) + summary + "\n" + responsibilities + qualificationsData(v) + skillsBlock(req, pref) + footer();
    }

    private static String engineering(String context, String req, String pref, int v) {
        String summary = switch (v) {
            case 1 -> """
                    Job Summary:
                    We are hiring an Engineering professional to build reliable software for our HR and resume screening platform. \
                    You will work across the stack with product and QA to ship features that recruiters use daily.
                    """;
            case 2 -> """
                    Job Summary:
                    Our engineering team is growing. We need a developer who values clean code, observability, and collaboration. \
                    You will help modernize services, improve performance, and mentor peers through code review.
                    """;
            default -> """
                    Job Summary:
                    We are looking for a motivated Engineering professional to design, develop, and maintain software applications and systems. \
                    The ideal candidate possesses strong technical expertise, curiosity, and problem-solving capabilities.
                    """;
        };
        String responsibilities = switch (v) {
            case 1 -> """
                    Key Responsibilities:
                    - Implement REST APIs and business logic in Java / Spring Boot (or equivalent stack)
                    - Write unit and integration tests; participate in CI/CD pipelines
                    - Troubleshoot production issues and improve logging and monitoring
                    - Collaborate with frontend on API contracts and error handling
                    - Contribute to technical design documents and sprint planning
                    """;
            case 2 -> """
                    Key Responsibilities:
                    - Refactor legacy modules for maintainability and performance
                    - Integrate third-party services (AI, email, document parsing) securely
                    - Conduct code reviews and uphold engineering standards
                    - Optimize database queries and schema where needed
                    - Support deployment and environment configuration with DevOps
                    """;
            default -> """
                    Key Responsibilities:
                    - Develop and maintain scalable applications and services
                    - Collaborate with cross-functional teams for on-time project delivery
                    - Troubleshoot and debug technical issues across environments
                    - Participate in code reviews, testing, and release activities
                    - Ensure software quality, security, and performance standards
                    """;
        };
        return header(context) + summary + "\n" + responsibilities + qualificationsEngineering(v) + skillsBlock(req, pref) + footer();
    }

    private static String finance(String context, String req, String pref, int v) {
        String summary = switch (v) {
            case 1 -> """
                    Job Summary:
                    Finance & Accounting is hiring a professional to strengthen reporting, controls, and planning. \
                    You will work closely with leadership on monthly close, forecasts, and compliance activities.
                    """;
            case 2 -> """
                    Job Summary:
                    We seek a finance specialist who can bring discipline to budgeting and financial analysis while supporting audits. \
                    The role blends operational accounting with strategic insight for growing teams.
                    """;
            default -> """
                    Job Summary:
                    We are seeking a Finance & Accounting professional responsible for financial reporting, budgeting, compliance, \
                    and maintaining accurate financial records for the organization.
                    """;
        };
        String responsibilities = switch (v) {
            case 1 -> """
                    Key Responsibilities:
                    - Prepare monthly P&L, balance sheet support schedules, and variance commentary
                    - Maintain general ledger accuracy and reconciliation procedures
                    - Support annual budget build and rolling forecasts
                    - Coordinate with auditors and external advisors as needed
                    - Improve AP/AR processes and working capital visibility
                    """;
            case 2 -> """
                    Key Responsibilities:
                    - Analyze cost centers and hiring-related spend for HR leadership
                    - Implement controls for expenses, payroll interfaces, and accruals
                    - Produce management dashboards for cash flow and KPIs
                    - Ensure tax and regulatory filings are completed on schedule
                    - Recommend process improvements for close cycle time
                    """;
            default -> """
                    Key Responsibilities:
                    - Prepare financial statements and management reports
                    - Manage budgeting, forecasting, and variance analysis
                    - Ensure compliance with accounting standards and internal policies
                    - Monitor accounts payable, receivable, and treasury activities
                    - Assist in audits, due diligence, and ad-hoc financial analysis
                    """;
        };
        return header(context) + summary + "\n" + responsibilities + qualificationsFinance(v) + skillsBlock(req, pref) + footer();
    }

    private static String marketing(String context, String req, String pref, int v) {
        String summary = switch (v) {
            case 1 -> """
                    Job Summary:
                    Our Marketing team is looking for a creative, metrics-driven marketer to grow brand awareness and candidate engagement. \
                    You will plan campaigns end-to-end and optimize based on performance data.
                    """;
            case 2 -> """
                    Job Summary:
                    We need a marketing professional who can balance brand storytelling with demand generation. \
                    You will collaborate with sales and product on messaging, content, and digital channels.
                    """;
            default -> """
                    Job Summary:
                    We are looking for a creative and result-driven Marketing professional to develop and execute marketing strategies \
                    that increase brand awareness, lead quality, and customer engagement.
                    """;
        };
        String responsibilities = switch (v) {
            case 1 -> """
                    Key Responsibilities:
                    - Plan and execute integrated campaigns across digital and social channels
                    - Manage content calendar, briefs, and agency/freelancer coordination
                    - Run SEO/SEM experiments and report on ROI
                    - Conduct competitor and market research for positioning
                    - Track funnel metrics and recommend optimizations weekly
                    """;
            case 2 -> """
                    Key Responsibilities:
                    - Develop employer-brand assets for hiring and campus outreach
                    - Partner with design on creatives for ads, landing pages, and email
                    - Maintain CRM lists, nurture flows, and event follow-up
                    - Analyze campaign performance; present insights to leadership
                    - Support product launches with go-to-market checklists
                    """;
            default -> """
                    Key Responsibilities:
                    - Plan and execute marketing campaigns across channels
                    - Conduct market and competitor research
                    - Manage social media and digital marketing activities
                    - Coordinate with design, sales, and product teams
                    - Analyze campaign performance metrics and optimize spend
                    """;
        };
        return header(context) + summary + "\n" + responsibilities + qualificationsMarketing(v) + skillsBlock(req, pref) + footer();
    }

    private static String operations(String context, String req, String pref, int v) {
        String summary = switch (v) {
            case 1 -> """
                    Job Summary:
                    Operations is hiring a coordinator to streamline workflows, vendor touchpoints, and service delivery. \
                    You will be the connective tissue between teams and external partners.
                    """;
            case 2 -> """
                    Job Summary:
                    We are building operational excellence across HR programs and need someone who thrives on process, KPIs, and continuous improvement. \
                    You will document SOPs and drive cross-functional execution.
                    """;
            default -> """
                    Job Summary:
                    We are seeking an organized and proactive Operations professional to oversee daily operational activities \
                    and improve business processes for maximum efficiency and reliability.
                    """;
        };
        String responsibilities = switch (v) {
            case 1 -> """
                    Key Responsibilities:
                    - Map end-to-end hiring operations workflows; remove bottlenecks
                    - Track SLAs, capacity, and vendor scorecards
                    - Facilitate stand-ups and status reporting for operations projects
                    - Maintain documentation for tools, vendors, and escalation paths
                    - Lead small improvement initiatives using Lean principles
                    """;
            case 2 -> """
                    Key Responsibilities:
                    - Coordinate logistics for interviews, onboarding kits, and events
                    - Monitor operational KPIs and prepare weekly dashboards
                    - Negotiate with vendors on scope, pricing, and delivery timelines
                    - Train team members on updated procedures and checklists
                    - Support audits of process adherence and risk controls
                    """;
            default -> """
                    Key Responsibilities:
                    - Monitor and improve operational workflows
                    - Coordinate with internal teams and external vendors
                    - Track operational KPIs and performance indicators
                    - Ensure timely delivery of services and projects
                    - Identify areas for process optimization and automation
                    """;
        };
        return header(context) + summary + "\n" + responsibilities + qualificationsOperations(v) + skillsBlock(req, pref) + footer();
    }

    private static String header(String context) {
        if (!StringUtils.hasText(context)) {
            return "";
        }
        return context + "\n\n";
    }

    private static String footer() {
        return """

                What We Offer:
                - Collaborative team environment with clear goals
                - Opportunities for learning and career growth
                - Competitive compensation aligned with experience

                How to Apply:
                Submit your application through our hiring portal. We review applications on a rolling basis.
                """;
    }

    private static String qualificationsData(int v) {
        return switch (v) {
            case 1 -> """
                    Required Qualifications:
                    - Bachelor's degree in Statistics, Economics, Computer Science, or related field
                    - 2+ years in analytics, BI, or data-focused roles
                    - Strong SQL skills and experience with Excel or similar tools
                    - Ability to communicate insights to business stakeholders
                    """;
            case 2 -> """
                    Required Qualifications:
                    - Proven experience building dashboards in Power BI, Tableau, or equivalent
                    - Solid understanding of data modeling and metric design
                    - Experience working with HR, recruiting, or operations data is a plus
                    - Detail-oriented mindset with strong documentation habits
                    """;
            default -> """
                    Required Qualifications:
                    - Degree or equivalent experience in analytics, business, or a quantitative discipline
                    - Hands-on experience with SQL, spreadsheets, and visualization tools
                    - Strong analytical, problem-solving, and communication skills
                    - Comfort working with cross-functional partners and changing priorities
                    """;
        };
    }

    private static String qualificationsEngineering(int v) {
        return switch (v) {
            case 1 -> """
                    Required Qualifications:
                    - Bachelor's in Computer Science or equivalent practical experience
                    - Strong programming fundamentals in Java, Python, or similar languages
                    - Experience building web services and working with relational databases
                    - Familiarity with Git, testing, and agile delivery
                    """;
            case 2 -> """
                    Required Qualifications:
                    - 3+ years of professional software development experience
                    - Experience with Spring Boot or similar backend frameworks
                    - Understanding of REST APIs, authentication, and error handling
                    - Ability to write clear technical documentation
                    """;
            default -> """
                    Required Qualifications:
                    - Solid programming knowledge and software development lifecycle exposure
                    - Strong problem-solving skills and attention to detail
                    - Team-oriented mindset with effective communication
                    - Willingness to learn new tools, frameworks, and domains
                    """;
        };
    }

    private static String qualificationsFinance(int v) {
        return switch (v) {
            case 1 -> """
                    Required Qualifications:
                    - Degree in Accounting, Finance, or related field (CA/MBA a plus)
                    - Experience with financial reporting and month-end close
                    - Strong Excel and analytical skills
                    - Knowledge of GAAP or local accounting standards
                    """;
            case 2 -> """
                    Required Qualifications:
                    - 2+ years in accounting or FP&A roles
                    - Experience with budgeting and forecasting cycles
                    - Accuracy and integrity when handling confidential financial data
                    - Professional communication with auditors and leadership
                    """;
            default -> """
                    Required Qualifications:
                    - Understanding of financial reporting and accounting principles
                    - Strong numerical and analytical abilities
                    - Experience with budgeting, forecasting, or management reporting
                    - High integrity and organizational skills
                    """;
        };
    }

    private static String qualificationsMarketing(int v) {
        return switch (v) {
            case 1 -> """
                    Required Qualifications:
                    - Degree in Marketing, Communications, or related field
                    - Experience with digital marketing channels and analytics
                    - Strong writing and presentation skills
                    - Portfolio or examples of campaigns managed
                    """;
            case 2 -> """
                    Required Qualifications:
                    - 2+ years in marketing, growth, or brand roles
                    - Familiarity with SEO, paid media, or social platforms
                    - Ability to interpret metrics and optimize campaigns
                    - Creative thinking with structured project management
                    """;
            default -> """
                    Required Qualifications:
                    - Knowledge of digital marketing and brand communication
                    - Market research and analytical abilities
                    - Strong collaboration with design and sales partners
                    - Comfort working in a fast-paced environment
                    """;
        };
    }

    private static String qualificationsOperations(int v) {
        return switch (v) {
            case 1 -> """
                    Required Qualifications:
                    - Bachelor's degree or equivalent operations experience
                    - Strong organizational and multitasking skills
                    - Experience coordinating projects or vendor relationships
                    - Proficiency with spreadsheets and basic reporting tools
                    """;
            case 2 -> """
                    Required Qualifications:
                    - 2+ years in operations, program coordination, or similar
                    - Track record of improving processes and documenting SOPs
                    - Excellent stakeholder communication
                    - Comfort with KPI tracking and issue escalation
                    """;
            default -> """
                    Required Qualifications:
                    - Project management or process improvement experience
                    - Strong organizational and prioritization skills
                    - Ability to work with diverse internal and external partners
                    - Proactive approach to identifying inefficiencies
                    """;
        };
    }
}
