# Local MySQL setup (persistent storage)

The app stores **users**, **job descriptions**, **candidates**, **scores**, **interviews**, and related data in **MySQL**.

By default the backend uses the **`dev`** profile (**H2** stored under your user home — data **survives** normal backend restarts; see `application-dev.yml`).  
To keep data in **MySQL** or share with a team, use **MySQL** with the **`mysql`** profile.

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

## Using a MySQL database you created yourself

No extra code is required: the API already reads and writes **job descriptions** (and users, candidates, etc.) through JPA.

1. **Create an empty database** (any name you like), or use `ai_resume_screening` to match the defaults.
2. **Optional:** run `database/schema.sql` in Workbench or `mysql` CLI so tables match production exactly. If you skip this, with `spring.jpa.hibernate.ddl-auto: update` the app will create/update tables on first start — fine for local dev.
3. **Start the backend with the `mysql` profile** and credentials that match **your** server:

```powershell
cd "path\to\ai-resume-screening-tool\backend"

$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_NAME = "ai_resume_screening"   # <- change to YOUR database name
$env:DB_USERNAME = "root"              # <- your MySQL user
$env:DB_PASSWORD = "your_password"     # <- your MySQL password

mvn spring-boot:run
```

4. On first connect to an **empty** database, the app seeds **users** (`hruser`, `admin`) and **three sample jobs** if those tables are empty — then you can add more jobs from the UI; they are stored in **`job_descriptions`**.

5. Confirm in MySQL:

```sql
USE your_database_name;
SELECT id, title FROM job_descriptions;
```

**If login fails with DB errors:** MySQL must be running, port must match `DB_PORT`, and the user must have `CREATE`/`ALTER` rights if you rely on Hibernate to create tables.

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

**Or** use the app: create jobs and upload resumes, restart the backend — with **`dev`** (file H2) or **`mysql`**, data should still be there.

---

## H2 vs MySQL

| | `dev` profile (default) | `mysql` profile |
|--|-------------------------|-----------------|
| Database | H2 file under `%USERPROFILE%\ai-resume-screening\h2-data\` (Windows) | MySQL on disk |
| Data after restart | **Kept** (same machine / same H2 files) | **Kept** |
| Setup | None | Docker or MySQL install |
| Good for | Solo local dev | Shared DB, Workbench |

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
