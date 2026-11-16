$ErrorActionPreference = "Stop"

$composeFile = Join-Path $PSScriptRoot "docker-compose.integration.yml"
$envFile = Join-Path $PSScriptRoot ".env.integration"
$projectName = "myf-integration"
$docker = Get-Command docker -ErrorAction SilentlyContinue

if (-not $docker) {
    throw "Docker CLI was not found. Install Docker Desktop before stopping the integration environment."
}

if (Test-Path $composeFile) {
    $composeArgs = @(
        "compose", "--project-name", $projectName,
        "--file", $composeFile
    )
    if (Test-Path $envFile) {
        $composeArgs += @("--env-file", $envFile)
    }
    & $docker.Source @composeArgs down --remove-orphans
    if ($LASTEXITCODE -ne 0) {
        throw "Could not remove the disposable integration containers."
    }
}

if (Test-Path $envFile) {
    Remove-Item -LiteralPath $envFile
}

Write-Host "Disposable integration containers and local credentials have been removed." -ForegroundColor Green
Write-Host "Existing project containers and local SQL Server were not targeted."
