# Test data guide — AI Resume Screening Tool

Use this while **backend** (`mvn spring-boot:run`) and **frontend** (`npm.cmd run dev`) are running.

---

## 1. Login accounts (auto-created on first backend start)

| Username | Password     | Role  | Use for                          |
|----------|--------------|-------|----------------------------------|
| `hruser` | `Hr@123456`  | HR    | Normal testing (upload, jobs)    |
| `admin`  | `Admin@123`  | ADMIN | Admin analytics (`/analytics/admin/summary`) |

**UI:** http://localhost:5173/login  
**API health:** http://localhost:8080/api/auth/health → `{"status":"UP"}`

---

## 2. Sample job descriptions (create in UI → **Jobs**, or use API)

After login, create these jobs (or rely on auto-seed — see `jobs-reference.json`). Note each job’s **ID** from the list (often `1`, `2`, `3` on a fresh DB).

### Job A — Senior Java Engineer (strong match for Priya / Alex)

- **Title:** Senior Java Engineer  
- **Department:** Engineering  
- **Location:** Remote  
- **Experience:** 5+ years  
- **Required skills:** Java, Spring Boot, SQL, REST, Microservices  
- **Preferred skills:** React, Kubernetes, AWS, Docker  
- **Description:** Build and maintain HR analytics microservices. Own API design, code reviews, unit/integration tests, and production support. Mentor junior developers.

### Job B — Frontend React Developer (strong match for Jordan)

- **Title:** Frontend React Developer  
- **Department:** Product Engineering  
- **Location:** Hybrid — Bangalore  
- **Experience:** 3+ years  
- **Required skills:** React, TypeScript, HTML, CSS, REST APIs  
- **Preferred skills:** Tailwind CSS, Vite, unit testing (Jest)  
- **Description:** Implement responsive HR dashboards, integrate with Spring Boot APIs, and improve accessibility and performance.

### Job C — Data Analyst (weak match for Java resumes)

- **Title:** Data Analyst  
- **Department:** Analytics  
- **Location:** On-site — Mumbai  
- **Experience:** 2+ years  
- **Required skills:** SQL, Excel, Power BI, Python, Statistics  
- **Preferred skills:** Tableau, ETL  
- **Description:** Build reports and dashboards for hiring metrics. Clean and model candidate pipeline data.

---

## 3. Sample resumes (upload as PDF or DOCX)

Plain-text sources are in `sample-data/resumes/`:

| File | Candidate        | Best job match | Expected outcome        |
|------|------------------|----------------|-------------------------|
| `priya-sharma.txt` | Priya Sharma   | Job A (Java)   | High score (~70–95 with AI) |
| `alex-kumar.txt`   | Alex Kumar     | Job A (Java)   | High score                |
| `jordan-lee.txt`   | Jordan Lee     | Job B (React)  | High score                |

**How to upload**

1. Open each `.txt` in Word / Google Docs → **Save as PDF** or **DOCX**.  
2. In the app: **Candidates** → choose file → set **Job ID** (e.g. `1`) → **Upload & parse**.  
3. Or use any real PDF resume you already have.

**Contacts in sample files (for LinkedIn verify test)**

- Priya: `linkedin.com/in/priyasharma-dev` (in text)  
- Alex: `linkedin.com/in/alexkumar-java`  
- Jordan: `linkedin.com/in/jordanlee-react`

---

## 4. Suggested end-to-end test script

| Step | Action | Expected result |
|------|--------|-----------------|
| 1 | Login as `hruser` | Dashboard loads |
| 2 | Create Job A (or use seeded job id `1`) | Job appears in Jobs list |
| 3 | Upload `priya-sharma.pdf` with Job ID `1` | Candidate row; match score populated |
| 4 | Upload `jordan-lee.pdf` with Job ID `1` | Lower score vs Java job |
| 5 | Filter candidates by status `NEW` | Both visible |
| 6 | Shortlist Priya (PATCH status) | Status = SHORTLISTED |
| 7 | Export Excel `/api/reports/candidates.xlsx` | File downloads |
| 8 | (Optional) `POST /api/linkedin/verify/{candidateId}` | Log + screenshot path (Selenium) |
| 9 | Login as `admin` | Admin summary endpoint works |

---

## 5. API quick test (PowerShell)

```powershell
# Login
$login = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST `
  -ContentType "application/json" `
  -Body '{"username":"hruser","password":"Hr@123456"}'
$token = $login.token
$headers = @{ Authorization = "Bearer $token" }

# List jobs
Invoke-RestMethod -Uri "http://localhost:8080/api/jobs" -Headers $headers

# Create job
Invoke-RestMethod -Uri "http://localhost:8080/api/jobs" -Method POST -Headers $headers `
  -ContentType "application/json" `
  -Body (@{
    title = "Senior Java Engineer"
    department = "Engineering"
    location = "Remote"
    experienceRequired = "5+ years"
    requiredSkills = "Java, Spring Boot, SQL, REST"
    preferredSkills = "React, AWS"
    description = "Microservices for HR platform."
    active = $true
  } | ConvertTo-Json)

# Upload resume (replace path and jobDescriptionId)
$form = @{
  file = Get-Item "C:\path\to\priya-sharma.pdf"
  jobDescriptionId = 1
}
Invoke-RestMethod -Uri "http://localhost:8080/api/resumes/upload" -Method POST `
  -Headers $headers -Form $form
```

Import **`postman/AI-Resume-Screening.postman_collection.json`** for more endpoints.

---

## 6. AI behavior notes

- Set **`OPENAI_API_KEY`** for best extraction and scoring.  
- Without a key, the app uses **keyword heuristics** (scores still appear, less accurate).  
- Duplicate upload of the same resume text triggers **`possibleDuplicate: true`**.

---

## 7. Chatbot test prompts

`POST /api/chat` with body:

```json
{ "message": "What should I ask in a technical interview for a Spring Boot role?", "context": "Job: Senior Java Engineer" }
```

---

## 8. Multi-job compare test

After scoring the same candidate against jobs `1` and `3`:

```json
POST /api/candidates/{id}/compare
{ "jobDescriptionIds": [1, 3] }
```

Expect higher score on job `1` for Java resumes.
