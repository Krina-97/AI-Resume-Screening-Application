#!/usr/bin/env python3
"""Generate sample .docx resumes for candidate upload testing."""

from __future__ import annotations

import zipfile
from pathlib import Path
from xml.sax.saxutils import escape

ROOT = Path(__file__).resolve().parent.parent
OUT_DIR = ROOT / "sample-data" / "resumes" / "docx"

RESUMES = [
    {
        "filename": "priya-sharma-java.docx",
        "title": "Priya Sharma — Senior Java Developer",
        "blocks": [
            "PRIYA SHARMA",
            "Email: priya.sharma@email.com | Phone: +91 98765 43210",
            "LinkedIn: https://www.linkedin.com/in/priyasharma-dev | Pune, India",
            "",
            "SUMMARY",
            "Senior software engineer with 6 years building enterprise Java applications and REST APIs. "
            "Experienced in Spring Boot, microservices, MySQL, and agile delivery for HR and fintech products.",
            "",
            "SKILLS",
            "Java, Spring Boot, Spring Security, Hibernate, SQL, MySQL, REST APIs, Microservices, "
            "Docker, Git, JUnit, Maven, React (basic)",
            "",
            "EXPERIENCE",
            "TechNova Solutions — Senior Java Developer (2021 – Present)",
            "• Designed Spring Boot microservices for employee onboarding and document workflows",
            "• Improved API latency by 35% through query tuning and caching",
            "• Led code reviews and mentored 3 junior engineers",
            "",
            "InfoCore Pvt Ltd — Java Developer (2019 – 2021)",
            "• Built REST integrations with third-party payroll systems",
            "• Wrote unit and integration tests with JUnit and Mockito",
            "",
            "EDUCATION",
            "B.Tech Computer Science, Pune University, 2019",
            "",
            "CERTIFICATIONS",
            "Oracle Certified Professional: Java SE 11 Developer",
            "AWS Certified Cloud Practitioner",
        ],
    },
    {
        "filename": "alex-kumar-backend.docx",
        "title": "Alex Kumar — Staff Backend Engineer",
        "blocks": [
            "ALEX KUMAR",
            "Email: alex.kumar@outlook.com | Phone: +1 415 555 0198",
            "LinkedIn: https://www.linkedin.com/in/alexkumar-java | San Francisco, CA",
            "",
            "PROFESSIONAL SUMMARY",
            "Backend engineer focused on scalable Java services, event-driven architecture, and cloud deployment on AWS. "
            "7 years in product companies with strong ownership of matching and authentication platforms.",
            "",
            "TECHNICAL SKILLS",
            "Java 17, Spring Boot, Spring Data JPA, Kafka, PostgreSQL, SQL, Kubernetes, Docker, CI/CD, REST",
            "",
            "WORK HISTORY",
            "CloudHire Inc — Staff Engineer (2020 – Present)",
            "• Owned candidate matching service used by 200+ enterprise clients",
            "• Migrated monolith modules to Spring Boot microservices",
            "• Implemented JWT authentication and role-based access",
            "",
            "DataStack — Software Engineer (2017 – 2020)",
            "• Developed reporting APIs and batch ETL jobs in Java",
            "",
            "EDUCATION",
            "MS Computer Science, San Jose State University, 2017",
            "BS Information Technology, 2015",
            "",
            "CERTIFICATIONS",
            "Certified Kubernetes Application Developer (CKAD)",
        ],
    },
    {
        "filename": "jordan-lee-frontend.docx",
        "title": "Jordan Lee — Frontend Engineer",
        "blocks": [
            "JORDAN LEE",
            "Email: jordan.lee@gmail.com | Phone: +91 99887 76655",
            "LinkedIn: https://www.linkedin.com/in/jordanlee-react | Bengaluru, India",
            "",
            "SUMMARY",
            "Frontend developer with 4 years creating responsive web apps with React, TypeScript, and modern CSS. "
            "Passionate about HR tech UX, accessible design, and performance optimization.",
            "",
            "SKILLS",
            "React, TypeScript, JavaScript, HTML5, CSS3, Tailwind CSS, Vite, Axios, React Router, "
            "Jest, React Testing Library, REST API integration, Git",
            "",
            "EXPERIENCE",
            "PeopleFirst HR — Frontend Engineer (2022 – Present)",
            "• Built recruiter dashboard with charts, filters, and dark mode",
            "• Integrated React SPA with Spring Boot backend using JWT auth",
            "• Improved Lighthouse performance score from 62 to 91",
            "",
            "WebCraft Studio — Junior Frontend Developer (2020 – 2022)",
            "• Implemented component library and form validation flows",
            "",
            "EDUCATION",
            "B.E. Information Science, RV College of Engineering, 2020",
            "",
            "CERTIFICATIONS",
            "Meta Front-End Developer Professional Certificate",
        ],
    },
]


def paragraph_xml(text: str) -> str:
    if not text:
        return "<w:p/>"
    safe = escape(text)
    return (
        "<w:p><w:r><w:t xml:space=\"preserve\">"
        + safe
        + "</w:t></w:r></w:p>"
    )


def build_document_xml(blocks: list[str]) -> str:
    body = "".join(paragraph_xml(line) for line in blocks)
    return (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
        f"<w:body>{body}<w:sectPr/></w:body></w:document>"
    )


def write_docx(path: Path, blocks: list[str]) -> None:
    document_xml = build_document_xml(blocks)
    content_types = """<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""
    rels = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""
    doc_rels = """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>"""

    path.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("[Content_Types].xml", content_types)
        zf.writestr("_rels/.rels", rels)
        zf.writestr("word/_rels/document.xml.rels", doc_rels)
        zf.writestr("word/document.xml", document_xml)


def main() -> None:
    for resume in RESUMES:
        out = OUT_DIR / resume["filename"]
        write_docx(out, resume["blocks"])
        print(f"Created {out.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
