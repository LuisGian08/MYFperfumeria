$ErrorActionPreference = "Stop"
Write-Host "MYF Perfumes - iniciando servicios locales" -ForegroundColor Cyan

# 1) Buscar Maven. Primero PATH; si no, usar Maven incluido con IntelliJ IDEA.
$mvnPath = $null
$mvnCmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if (-not $mvnCmd) { $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue }
if ($mvnCmd) { $mvnPath = $mvnCmd.Source }

if (-not $mvnPath) {
    $idea = Get-Process idea64 -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($idea -and $idea.Path) {
        $ideaBin = Split-Path $idea.Path -Parent
        $ideaRoot = Split-Path $ideaBin -Parent
        $candidate = Join-Path $ideaRoot "plugins\maven\lib\maven3\bin\mvn.cmd"
        if (Test-Path $candidate) { $mvnPath = $candidate }
    }
}

if (-not $mvnPath) {
    $commonRoots = @(
        "$env:ProgramFiles\JetBrains",
        "$env:LOCALAPPDATA\Programs",
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps"
    ) | Where-Object { $_ -and (Test-Path $_) }

    foreach ($root in $commonRoots) {
        $found = Get-ChildItem -Path $root -Filter mvn.cmd -Recurse -ErrorAction SilentlyContinue |
                 Where-Object { $_.FullName -match 'plugins\\maven\\lib\\maven3\\bin\\mvn\.cmd$' } |
                 Select-Object -First 1
        if ($found) {
            $mvnPath = $found.FullName
            break
        }
    }
}

if (-not $mvnPath) {
    Write-Host "No se encontro Maven ni el Maven incluido con IntelliJ." -ForegroundColor Red
    Write-Host "Abre IntelliJ IDEA antes de ejecutar este script o instala Maven." -ForegroundColor Yellow
    Read-Host "Presiona Enter para cerrar"
    exit 1
}

Write-Host "Maven encontrado: $mvnPath" -ForegroundColor DarkGray

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

    $cmd = "Set-Location '$modulePath'; & '$mvnPath' spring-boot:run"
    Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $cmd
    Start-Sleep -Seconds 2
}

# Frontend: usar jwebserver (Java 21) si el puerto 5500 esta libre.
$frontOccupied = Get-NetTCPConnection -LocalPort 5500 -State Listen -ErrorAction SilentlyContinue
if (-not $frontOccupied) {
    $frontPath = Join-Path $PSScriptRoot "frontend"
    $jweb = Get-Command jwebserver -ErrorAction SilentlyContinue
    if ($jweb) {
        $frontCmd = "Set-Location '$frontPath'; & '$($jweb.Source)' -p 5500 -d '$frontPath'"
        Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $frontCmd
        Write-Host "Frontend iniciado en http://localhost:5500" -ForegroundColor Cyan
    } else {
        Write-Host "No se encontro jwebserver. Inicia el frontend manualmente." -ForegroundColor Yellow
    }
} else {
    Write-Host "Frontend 5500 ya esta en uso." -ForegroundColor Yellow
}

Write-Host "" 
Write-Host "Tienda: http://localhost:5500/index.html" -ForegroundColor Cyan
Write-Host "Admin:  http://localhost:5500/admin.html" -ForegroundColor Cyan
