$ErrorActionPreference = "Stop"

$composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
$envFile = Join-Path $PSScriptRoot ".env"
$projectName = "myf_perfumes_sistema_final_2026"
$docker = Get-Command docker -ErrorAction SilentlyContinue

if (-not $docker) {
    throw "Docker CLI was not found. Install Docker Desktop before stopping the stack."
}

$composeArgs = @(
    "compose", "--project-name", $projectName,
    "--file", $composeFile
)
if (Test-Path -LiteralPath $envFile) {
    $composeArgs += @("--env-file", $envFile)
}

& $docker.Source @composeArgs stop
if ($LASTEXITCODE -ne 0) {
    throw "Could not stop the MYF Perfumes Docker stack."
}

Write-Host "MYF Perfumes containers stopped. Persistent data volumes were preserved." -ForegroundColor Green
