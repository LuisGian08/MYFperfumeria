$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
Write-Host "1. Login"
$login = Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" -ContentType "application/json" -Body '{"email":"admin@myf.local","password":"Admin123*"}'
$headers = @{ Authorization = "Bearer $($login.token)" }
Write-Host "OK - usuario $($login.email)"
Write-Host "2. Productos publicos"
$products = Invoke-RestMethod -Uri "$base/api/productos"
Write-Host "OK - $($products.Count) productos"
if ($products.Count -gt 0) {
    $publicFields = $products[0].PSObject.Properties.Name
    foreach ($restrictedField in @("costo", "estado", "codigo", "categoriaId", "marcaId")) {
        if ($publicFields -contains $restrictedField) {
            throw "El catálogo público expuso el campo interno '$restrictedField'."
        }
    }
    foreach ($restrictedField in @("costo", "estado", "codigo")) {
        if ($products[0].presentaciones.Count -gt 0 -and
            $products[0].presentaciones[0].PSObject.Properties.Name -contains $restrictedField) {
            throw "Una presentación pública expuso el campo interno '$restrictedField'."
        }
    }
}
Write-Host "OK - catálogo sin campos internos"
Write-Host "3. Inventario requiere autenticacion"
try {
    Invoke-RestMethod -Uri "$base/api/inventario" | Out-Null
    throw "El inventario aceptó una solicitud sin token."
} catch {
    $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
    if ($status -ne 401) { throw }
    Write-Host "OK - solicitud anónima rechazada (401)"
}
$inventory = Invoke-RestMethod -Uri "$base/api/inventario" -Headers $headers
Write-Host "OK - administrador consulta $($inventory.Count) registros"
Write-Host "4. Dashboard protegido"
$dash = Invoke-RestMethod -Uri "$base/api/reportes/dashboard" -Headers $headers
Write-Host "OK - ventas hoy: $($dash.ventasHoy)"
Write-Host "Smoke test completado" -ForegroundColor Green
