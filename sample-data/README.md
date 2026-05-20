# Sample data

| Resource | Purpose |
|----------|---------|
| **[TEST-DATA.md](./TEST-DATA.md)** | Full testing guide (logins, jobs, flows, API examples) |
| **[jobs-reference.json](./jobs-reference.json)** | Job payloads for API/UI copy-paste |
| **[resumes/docx/*.docx](./resumes/docx/)** | Ready-to-upload Word resumes (summary, skills, experience, education, certs) |
| **[resumes/*.txt](./resumes/)** | Same content as plain text (convert to PDF/DOCX if needed) |
| **[sample_job.sql](./sample_job.sql)** | Optional MySQL insert (if not using auto-seed) |

Jobs **1–3** are auto-created on first backend start (empty database). Users are seeded by `DataInitializer` in the backend.
