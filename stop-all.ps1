$ports = @(8080,8081,8082,8083,8084,8085,8086,5500)
foreach ($port in $ports) {
  $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
  foreach ($c in $connections) {
    $p = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
    if ($p -and ($p.ProcessName -match 'java|python')) {
      Write-Host "Deteniendo $($p.ProcessName) PID $($p.Id) en puerto $port"
      Stop-Process -Id $p.Id -Force
    }
  }
}
