$ErrorActionPreference = "Stop"

$composeFile = Join-Path $PSScriptRoot "docker-compose.integration.yml"
$envFile = Join-Path $PSScriptRoot ".env.integration"
$projectName = "myf-integration"
$docker = Get-Command docker -ErrorAction SilentlyContinue

if (-not $docker) {
    throw "Docker CLI was not found. Install Docker Desktop and retry."
}
& $docker.Source info *> $null
if ($LASTEXITCODE -ne 0) {
    throw "Docker Desktop is not running. Start it and retry."
}
if (-not (Test-Path $composeFile)) {
    throw "Integration Compose file was not found: $composeFile"
}

if (-not (Test-Path $envFile)) {
    $password = "MyfInt-" + [guid]::NewGuid().ToString("N") + "aA1!"
    @(
        "DB_PASSWORD=$password"
        "RABBIT_USER=myf_integration"
        "RABBIT_PASSWORD=$password"
    ) | Set-Content -LiteralPath $envFile -Encoding ascii
}

$composeArgs = @(
    "compose", "--project-name", $projectName,
    "--file", $composeFile,
    "--env-file", $envFile
)

Write-Host "Starting disposable SQL Server and RabbitMQ for integration tests..." -ForegroundColor Cyan
& $docker.Source @composeArgs up -d sqlserver rabbitmq
if ($LASTEXITCODE -ne 0) {
    throw "Could not start disposable integration infrastructure. Inspect Docker Desktop and the port bindings."
}

Write-Host "Waiting for RabbitMQ health..." -ForegroundColor Cyan
$rabbitReady = $false
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    $rabbitId = (& $docker.Source @composeArgs ps -q rabbitmq 2>$null | Out-String).Trim()
    if ($rabbitId) {
        $status = (& $docker.Source inspect --format='{{.State.Health.Status}}' $rabbitId 2>$null | Out-String).Trim()
    } else {
        $status = ""
    }
    if ($status -eq "healthy") {
        $rabbitReady = $true
        break
    }
    Start-Sleep -Seconds 2
}
if (-not $rabbitReady) {
    throw "Disposable RabbitMQ did not become healthy. Inspect: docker logs myf-integration-rabbitmq"
}

Write-Host "Creating the four isolated integration databases..." -ForegroundColor Cyan
& $docker.Source @composeArgs run --rm db-init
if ($LASTEXITCODE -ne 0) {
    throw "Could not initialize integration databases. Inspect: docker logs myf-integration-sqlserver"
}

Write-Host "Disposable integration infrastructure is ready:" -ForegroundColor Green
Write-Host "  SQL Server: localhost,14330 (databases MYF_*_DB)"
Write-Host "  RabbitMQ:   localhost:5673"
Write-Host "  Rabbit UI:  http://localhost:15673"
Write-Host "  Credentials are in the ignored local file .env.integration."
Write-Host "No application services or existing local databases were changed."
