# Starts ONE backend on port 8080. Stops any existing listener first (avoids "port already in use").
param(
    [switch]$MySql
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path $PSScriptRoot -Parent
$backendDir = Join-Path $repoRoot "backend"

if (-not (Test-Path (Join-Path $backendDir "pom.xml"))) {
    Write-Error "backend/pom.xml not found. Run from repo root scripts folder."
    exit 1
}

& (Join-Path $PSScriptRoot "stop-backend.ps1")
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Set-Location $backendDir
if ($MySql) {
    $env:SPRING_PROFILES_ACTIVE = "mysql"
    Write-Host "Profile: mysql"
} else {
    if (-not $env:SPRING_PROFILES_ACTIVE) {
        $env:SPRING_PROFILES_ACTIVE = "mysql"
    }
    Write-Host "Profile: $env:SPRING_PROFILES_ACTIVE"
}

Write-Host ""
Write-Host "Starting API at http://localhost:8080/api (Ctrl+C to stop)"
Write-Host "Logs: backend\logs\application.log"
Write-Host ""

mvn spring-boot:run
