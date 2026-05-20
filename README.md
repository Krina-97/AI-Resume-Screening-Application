# AI Resume Screening Automation Tool

Enterprise-style monorepo for HR teams to upload resumes (PDF/DOCX), manage job descriptions, run AI extraction and resume–job matching, rank candidates, automate lightweight LinkedIn discovery with Selenium, export reports, and monitor hiring analytics.

**Default workspace path (generated in this session):**  
`C:\Users\KRINA\.cursor\projects\empty-window\ai-resume-screening-tool`

Open that folder in Cursor (or move your agent root there) to work on the code.

---

## Architecture

| Layer | Responsibility |
| --- | --- |
| `controller` | REST endpoints, validation wiring, Swagger annotations |
| `service` | Business workflows (ingestion, scoring, analytics, exports) |
| `repository` | Spring Data JPA |
| `entity` / `dto` | Persistence model vs API contracts |
| `config` | Security, OpenAPI, CORS, properties, seed data |
| `security` | JWT issuance/filter, user details |
| `service/ai` | OpenAI/Gemini clients + `AiEngineService` orchestration |
| `specification` | Dynamic candidate filters |
| `utils` | Hashing, file validation, security helpers |
| `exception` | Consistent API errors |

Frontend (`frontend/`): React 18 + Vite + TypeScript + Tailwind + React Router + Axios + Recharts. Vite dev server proxies `/api` to Spring Boot.

---

## Prerequisites

- **Java 17**, **Maven 3.9+**
- **Node 20+** (for the UI)
- **MySQL 8** (local or Docker)
- **Chrome** installed if you enable Selenium LinkedIn verification locally
- **OpenAI** and/or **Gemini** API keys for full AI behavior (heuristic fallback exists when keys are absent)

---

## Environment variables (backend)

| Variable | Purpose | Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` = file-based H2 (under user home), `mysql` = MySQL | `dev` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL (when profile `mysql`) | `localhost`, `3306`, `ai_resume_screening`, `root`, `root` |
| `JWT_SECRET` | HS256 signing secret (≥ 32 chars recommended) | see `application.yml` |
| `JWT_EXPIRATION_MS` | Token TTL | `86400000` |
| `OPENAI_API_KEY` | OpenAI resume extraction + job match scoring | empty |
| `OPENAI_MODEL` | Model name | `gpt-4o-mini` |
| `OPENAI_BASE_URL` | API base | `https://api.openai.com/v1` |
| `GEMINI_API_KEY` | Google Gemini API key ([AI Studio](https://aistudio.google.com/apikey)) | empty |
| `GEMINI_ENABLED` | Set `true` when using Gemini | `false` |
| `AI_PROVIDER` | `openai` or `gemini` | `openai` |

After upload, match % vs the selected job sets status: **&lt;40% Rejected**, **40–60% New**, **&gt;60% Shortlisted**. See `backend/.env.example`.
| `UPLOAD_DIR` | Resume storage | `./uploads/resumes` |
| `SELENIUM_ENABLED`, `SELENIUM_HEADLESS`, `SELENIUM_SCREENSHOT_DIR` | Automation toggles | `true`, `true`, `./uploads/screenshots` |
| `MAIL_*` | Optional SMTP for interview emails | see `application.yml` |

---

## Database (persistent local storage)

| Profile | Storage | When to use |
| --- | --- | --- |
| **`dev`** (default) | File-based H2 (`%USERPROFILE%\ai-resume-screening\h2-data\` on Windows) | Local dev — data **survives** backend restarts; no MySQL install |
| **`mysql`** | MySQL on your PC | Teams / production-like — shared DB, Workbench, etc. |

**Full setup guide:** [database/LOCAL-MYSQL-SETUP.md](database/LOCAL-MYSQL-SETUP.md)

**Quick start (Docker MySQL):**

```powershell
.\scripts\start-mysql-docker.ps1
cd backend
$env:SPRING_PROFILES_ACTIVE = "mysql"
mvn spring-boot:run
```

---

## Run locally

### Backend

**Important:** Only **one** backend can use port **8080**. If you see `Port 8080 was already in use`, you already have a server running — do **not** run `mvn spring-boot:run` again.

**Recommended (Windows)** — stops the old process, then starts fresh:

```powershell
.\scripts\start-backend.ps1
```

With MySQL profile explicitly:

```powershell
.\scripts\start-backend.ps1 -MySql
```

Stop without starting:

```powershell
.\scripts\stop-backend.ps1
```

Or from `backend/`:

```powershell
.\run.ps1
```

Manual start (only if port 8080 is free — check with `netstat -ano | findstr :8080`):

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE = "mysql"
mvn spring-boot:run
```

- API base URL: `http://localhost:8080/api`
- Health: `http://localhost:8080/api/auth/health`
- Logs: `backend/logs/application.log`
- Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

### Frontend

```bash
cd frontend
npm install
npm run dev
```

- UI: `http://localhost:5173` (proxies API calls to `/api` → `http://localhost:8080/api`)

### Seeded users (first boot, empty database)

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `Admin@123` | `ADMIN` |
| `hruser` | `Hr@123456` | `HR` |

---

## Docker

From `docker/`:

```bash
docker compose up --build
```

- UI (nginx): `http://localhost`
- API: `http://localhost:8080/api` (direct) or `http://localhost/api/...` through nginx
- MySQL: `localhost:3306` (root/root)

Set `OPENAI_API_KEY` in your shell or `.env` next to compose for AI features.

---

## Jenkins

`jenkins/Jenkinsfile` builds backend (`mvn package`) and frontend (`npm install && npm run build`) on a Linux agent and archives artifacts. Copy it to your Jenkins job or multi-branch pipeline as needed.

---

## Postman

Import `postman/AI-Resume-Screening.postman_collection.json`.

1. Call **Auth → Login**, copy `token` from the response.
2. Set collection variable `token` (and `baseUrl` if not using defaults).
3. Call secured endpoints.

---

## Key REST endpoints

| Method | Path | Description |
| --- | --- | --- |
| POST | `/auth/register`, `/auth/login` | HR registration / JWT login |
| GET/POST/PUT | `/jobs`, `/jobs/{id}` | Job descriptions |
| POST | `/resumes/upload` | Multipart upload + parse + optional `jobDescriptionId` scoring |
| GET | `/resumes/{id}/preview` | Raw extracted text |
| GET | `/candidates` | Filters: `jobId`, `status`, `q` |
| GET/PATCH | `/candidates/{id}` | Detail / update status & LinkedIn URL |
| POST | `/candidates/{id}/score?jobDescriptionId=` | Refresh AI score |
| POST | `/candidates/{id}/interviews` | Schedule interview + optional email |
| POST | `/candidates/{id}/compare` | Multi-job comparison (body: `jobDescriptionIds`) |
| GET | `/candidates/recommendations?jobId=&limit=` | Top matches for a job (uses existing `candidate_scores` rows) |
| GET | `/analytics/dashboard` | HR metrics |
| GET | `/analytics/admin/summary` | Admin-only extended summary |
| POST | `/linkedin/verify/{candidateId}` | Selenium-assisted discovery |
| GET | `/linkedin/{candidateId}/logs` | Verification history |
| GET | `/reports/candidates.xlsx` / `.pdf` | Export filtered candidate sets |
| GET | `/reports/candidates/{id}.xlsx` / `.pdf` | Export one candidate |
| POST | `/reports/candidates/bulk-export` | ZIP export for selected candidate IDs |
| POST | `/chat` | HR assistant chatbot |

All secured routes expect `Authorization: Bearer <JWT>`.

---

## LinkedIn / Selenium disclaimer

LinkedIn aggressively blocks automation and may require login or CAPTCHA. The included flow performs a **Google search** for `"{name} site:linkedin.com/in"` and captures the first matching link plus a screenshot for audit purposes. Treat results as **hints**, not proof. For production, prefer official LinkedIn APIs or manual verification.

---

## Testing

```bash
cd backend
mvn test
```

Includes sample unit tests for JWT utilities and resume hashing.

---

## Screenshots

Add UI captures under `docs/screenshots/` (see `docs/screenshots/README.md`).

---

## License

Provided as sample enterprise scaffolding; apply your own license and hardening before production use.
