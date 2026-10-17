$ErrorActionPreference = "Stop"
Write-Host "MYF Perfumes - iniciando servicios locales" -ForegroundColor Cyan

$services = @(
    @{ Name = "Auth";      Module = "auth-service";      Port = 8081 },
    @{ Name = "Product";   Module = "product-service";   Port = 8082 },
    @{ Name = "Inventory"; Module = "inventory-service"; Port = 8083 },
    @{ Name = "Sales";     Module = "sales-service";     Port = 8084 },
    @{ Name = "Alert";     Module = "alert-service";     Port = 8085 },
    @{ Name = "Report";    Module = "report-service";    Port = 8086 },
    @{ Name = "Gateway";   Module = "gateway-service";   Port = 8080 }
)

foreach ($s in $services) {
    $occupied = Get-NetTCPConnection -LocalPort $s.Port -State Listen -ErrorAction SilentlyContinue
    if ($occupied) {
        Write-Host "[$($s.Name)] puerto $($s.Port) ya esta en uso. No se inicia otra copia." -ForegroundColor Yellow
        continue
    }
    $modulePath = Join-Path $PSScriptRoot $s.Module
    Write-Host "Iniciando $($s.Name) en $($s.Port)..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$modulePath'; mvn spring-boot:run"
    Start-Sleep -Seconds 2
}

$frontOccupied = Get-NetTCPConnection -LocalPort 5500 -State Listen -ErrorAction SilentlyContinue
if (-not $frontOccupied) {
    $frontPath = Join-Path $PSScriptRoot "frontend"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$frontPath'; python -m http.server 5500"
    Write-Host "Frontend: http://localhost:5500" -ForegroundColor Cyan
} else {
    Write-Host "Frontend 5500 ya esta en uso." -ForegroundColor Yellow
}

Write-Host "Tienda: http://localhost:5500/index.html" -ForegroundColor Cyan
Write-Host "Admin:  http://localhost:5500/admin.html" -ForegroundColor Cyan
