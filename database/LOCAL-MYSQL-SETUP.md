# Local MySQL setup (persistent storage)

The app stores **users**, **job descriptions**, **candidates**, **scores**, **interviews**, and related data in **MySQL**.

By default the backend uses the **`dev`** profile (in-memory **H2** — data is lost when you stop the server).  
To keep data on your machine, use **MySQL** with the **`mysql`** profile.

---

## What gets stored

| Data | Table(s) |
|------|-----------|
| HR users (login) | `users` |
| Job descriptions | `job_descriptions` |
| Uploaded resumes (metadata + extracted text) | `resumes` |
| Candidates (name, email, skills, overview, status) | `candidates` |
| AI match scores | `candidate_scores` |
| Interviews | `interview_status` |
| LinkedIn verification logs | `linkedin_verification_logs` |

Resume **files** (PDF/DOCX) are stored on disk at:

`C:\Users\KRINA\ai-resume-screening\uploads\resumes`

---

## Option A — MySQL with Docker (recommended)

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

### 1. Start MySQL

```powershell
cd "C:\Users\KRINA\.cursor\projects\empty-window\ai-resume-screening-tool\docker"
docker compose -f docker-compose.mysql.yml up -d
```

Wait until healthy:

```powershell
docker ps
```

You should see `ai-resume-mysql` on port **3306**.

**Connection details**

| Setting | Value |
|---------|--------|
| Host | `localhost` |
| Port | `3306` |
| Database | `ai_resume_screening` |
| User | `root` |
| Password | `root` |

Data is kept in Docker volume `mysql_data` (survives container restarts).

### 2. Run the backend with MySQL

```powershell
cd "C:\Users\KRINA\.cursor\projects\empty-window\ai-resume-screening-tool\backend"

$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "ai_resume_screening"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"

mvn spring-boot:run
```

On first start, the app seeds demo users (`hruser` / `Hr@123456`) and **3 sample jobs** if the database is empty.

### 3. Run the frontend

```powershell
cd "..\frontend"
npm.cmd run dev
```

Open http://localhost:5173

---

## Option B — MySQL Installer (no Docker)

### 1. Install MySQL 8

1. Download [MySQL Installer](https://dev.mysql.com/downloads/installer/) (Windows).
2. Choose **MySQL Server 8.0** and **MySQL Workbench** (optional, for browsing data).
3. Set root password (example: `root` — use the same in env vars below).
4. Keep default port **3306**.

### 2. Create the database

**Using MySQL Workbench:** connect as root → run script:

`database/schema.sql`

**Or command line:**

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < "C:\Users\KRINA\.cursor\projects\empty-window\ai-resume-screening-tool\database\schema.sql"
```

### 3. Start backend with `mysql` profile

Same environment variables as Option A, step 2.

---

## Environment variables (backend)

| Variable | Default | Purpose |
|----------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | `dev` | Set to **`mysql`** for persistent DB |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `ai_resume_screening` | Database name |
| `DB_USERNAME` | `root` | MySQL user |
| `DB_PASSWORD` | `root` | MySQL password |

Copy `backend/.env.example` to `backend/.env` and adjust if needed (load manually in PowerShell or your IDE).

---

## Verify data is in MySQL

**Workbench / CLI:**

```sql
USE ai_resume_screening;
SELECT id, title FROM job_descriptions;
SELECT id, full_name, email, status FROM candidates;
```

**Or** use the app: create jobs and upload resumes, restart the backend — data should still be there (unlike H2 `dev` in-memory).

---

## H2 vs MySQL

| | `dev` profile (default) | `mysql` profile |
|--|-------------------------|-----------------|
| Database | H2 in memory | MySQL on disk |
| Data after restart | **Lost** | **Kept** |
| Setup | None | Docker or MySQL install |
| Good for | Quick try | Real local testing |

---

## Troubleshooting

**`Communications link failure` / cannot connect**

- MySQL is not running → start Docker compose or Windows MySQL service.
- Wrong password → match `DB_PASSWORD` to your root password.

**Port 3306 already in use**

- Another MySQL is running. Stop it or change the port in Docker compose and `DB_PORT`.

**Empty job dropdown after switching to MySQL**

- Fresh DB: wait for seed on first backend start, or add jobs in **Job Descriptions** UI.
- Old H2 data does not migrate automatically — re-upload resumes or re-create jobs.

**Stop Docker MySQL**

```powershell
docker compose -f docker-compose.mysql.yml down
```

**Delete all Docker MySQL data (reset)**

```powershell
docker compose -f docker-compose.mysql.yml down -v
```
