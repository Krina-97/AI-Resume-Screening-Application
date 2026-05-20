-- Sample job descriptions for AI resume screening (MySQL)
USE ai_resume_screening;

INSERT INTO job_descriptions (
    title, department, location, experience_required,
    required_skills, preferred_skills, description, active
) VALUES (
    'Senior Java Backend Engineer',
    'Engineering',
    'Remote (India)',
    '5+ years',
    'Java, Spring Boot, REST APIs, SQL, MySQL, Git, unit testing, microservices',
    'React, Docker, Kubernetes, AWS, CI/CD, Redis',
    'We are hiring a Senior Java Backend Engineer to build and maintain HR and analytics APIs for our resume screening platform.

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
- Familiarity with React or TypeScript for full-stack collaboration',
    TRUE
);

INSERT INTO job_descriptions (
    title, department, location, experience_required,
    required_skills, preferred_skills, description, active
) VALUES (
    'Frontend React Developer',
    'Product Engineering',
    'Hybrid - Bangalore',
    '3+ years',
    'React, TypeScript, HTML, CSS, REST APIs, responsive design, Git',
    'Tailwind CSS, Vite, Jest, React Router, Recharts, accessibility (WCAG)',
    'Join the product team to deliver modern HR dashboards for resume screening and candidate ranking.

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
- Basic understanding of Java/Spring Boot APIs',
    TRUE
);

INSERT INTO job_descriptions (
    title, department, location, experience_required,
    required_skills, preferred_skills, description, active
) VALUES (
    'Data Analyst - HR Analytics',
    'Analytics',
    'On-site - Mumbai',
    '2+ years',
    'SQL, Excel, Python, statistics, data visualization, Power BI, reporting',
    'Tableau, ETL, MySQL, pandas, hiring/recruitment metrics',
    'Support HR leadership with hiring funnel analytics, candidate pipeline reporting, and data quality for the AI resume screening platform.

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
- Experience with ETL or data modeling',
    TRUE
);

SELECT id, title, department, location, active FROM job_descriptions ORDER BY id;
