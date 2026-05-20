# Stops anything on port 8080, then starts the Spring Boot API (dev profile / H2).
$ErrorActionPreference = "Stop"
$port = 8080

$conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conn) {
    Write-Host "Stopping process $($conn.OwningProcess) on port $port..."
    Stop-Process -Id $conn.OwningProcess -Force
    Start-Sleep -Seconds 2
}

$backend = Join-Path $PSScriptRoot "..\backend"
Set-Location $backend
Write-Host "Starting backend from $backend ..."
mvn spring-boot:run
