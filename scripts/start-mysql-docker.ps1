# Starts only MySQL in Docker (persistent volume). Run from repo root or scripts folder.
$ErrorActionPreference = "Stop"
$dockerDir = Join-Path $PSScriptRoot "..\docker"
Set-Location $dockerDir
Write-Host "Starting MySQL on localhost:3306 (database: ai_resume_screening)..."
docker compose -f docker-compose.mysql.yml up -d
Write-Host ""
Write-Host "Next, start the backend with MySQL profile:"
Write-Host '  $env:SPRING_PROFILES_ACTIVE="mysql"'
Write-Host "  cd ..\backend"
Write-Host "  mvn spring-boot:run"
Write-Host ""
Write-Host "See database/LOCAL-MYSQL-SETUP.md for full instructions."
