$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
Write-Host "1. Login"
$login = Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" -ContentType "application/json" -Body '{"email":"admin@myf.local","password":"Admin123*"}'
$headers = @{ Authorization = "Bearer $($login.token)" }
Write-Host "OK - usuario $($login.email)"
Write-Host "2. Productos publicos"
$products = Invoke-RestMethod -Uri "$base/api/productos"
Write-Host "OK - $($products.Count) productos"
Write-Host "3. Inventario publico"
$inventory = Invoke-RestMethod -Uri "$base/api/inventario"
Write-Host "OK - $($inventory.Count) registros"
Write-Host "4. Dashboard protegido"
$dash = Invoke-RestMethod -Uri "$base/api/reportes/dashboard" -Headers $headers
Write-Host "OK - ventas hoy: $($dash.ventasHoy)"
Write-Host "Smoke test completado" -ForegroundColor Green
