# Updates the 3 seeded job descriptions in the running app (requires backend on :8080).
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'

$login = Invoke-RestMethod -Uri "$base/auth/login" -Method POST `
  -Body '{"username":"hruser","password":"Hr@123456"}' -ContentType 'application/json'
$headers = @{ Authorization = "Bearer $($login.token)" }

$jobs = Get-Content -Raw -Path "$PSScriptRoot\..\sample-data\sample-jobs.json" | ConvertFrom-Json
$ids = @(1, 2, 3)

for ($i = 0; $i -lt $ids.Count; $i++) {
  $body = $jobs[$i] | ConvertTo-Json -Depth 5 -Compress
  $updated = Invoke-RestMethod -Uri "$base/jobs/$($ids[$i])" -Method PUT `
    -Headers $headers -Body $body -ContentType 'application/json; charset=utf-8'
  Write-Host "Updated job $($ids[$i]): $($updated.title)"
}

Write-Host 'Done — refresh Jobs page in the app.'
