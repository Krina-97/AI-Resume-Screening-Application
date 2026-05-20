# Stops any process listening on port 8080 (Spring Boot API).
$ErrorActionPreference = "Continue"
$port = 8080

$listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if (-not $listeners) {
    Write-Host "Port $port is free - no backend to stop."
    exit 0
}

$pids = $listeners | Select-Object -ExpandProperty OwningProcess -Unique | Where-Object { $_ -gt 0 }
foreach ($procId in $pids) {
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    $name = if ($proc) { $proc.ProcessName } else { "unknown" }
    Write-Host "Stopping PID $procId ($name) on port $port..."
    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
}

Start-Sleep -Seconds 2
$still = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if ($still) {
    Write-Host "WARNING: Port $port may still be in use. Run: netstat -ano | findstr :8080"
    exit 1
}

Write-Host "Port $port is free."
exit 0
