$ErrorActionPreference = "Stop"

$composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
$envFile = Join-Path $PSScriptRoot ".env"
$projectName = "myf_perfumes_sistema_final_2026"
$docker = Get-Command docker -ErrorAction SilentlyContinue

if (-not $docker) {
    throw "Docker CLI was not found. Install Docker Desktop and retry."
}
if (-not (Test-Path -LiteralPath $envFile)) {
    throw "Missing $envFile. Create it from .env.example, then set the local credentials."
}
& $docker.Source info *> $null
if ($LASTEXITCODE -ne 0) {
    throw "Docker Desktop is not running. Start it and retry."
}

$composeArgs = @(
    "compose", "--project-name", $projectName,
    "--file", $composeFile,
    "--env-file", $envFile
)

& $docker.Source @composeArgs config --quiet
if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose configuration is invalid. Review .env and docker-compose.yml."
}

Write-Host "Building and starting the outer MYF Perfumes stack..." -ForegroundColor Cyan
& $docker.Source @composeArgs up -d --build
if ($LASTEXITCODE -ne 0) {
    throw "Could not start the MYF Perfumes stack. Inspect Docker Desktop logs."
}

Write-Host "Stack started. Web: http://localhost:5500 | API/Swagger: http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "SQL Server data is persisted in the existing Docker volume; do not use 'down -v'." -ForegroundColor Yellow
