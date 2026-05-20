# Run from backend/ folder — frees port 8080 then starts Spring Boot.
$stop = Join-Path $PSScriptRoot "..\scripts\stop-backend.ps1"
& $stop
Set-Location $PSScriptRoot
if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = "mysql" }
Write-Host "Starting on http://localhost:8080/api ..."
mvn spring-boot:run
