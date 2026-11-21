# 停止后端(8081)、前端(5173)；-IncludeRedis 时连 Redis(6379) 一起停
param([switch]$IncludeRedis)

$ports = @(8081, 5173)
if ($IncludeRedis) { $ports += 6379 }

foreach ($p in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue
    if (-not $conns) { Write-Host "端口 $p 无监听"; continue }
    foreach ($c in ($conns | Select-Object -ExpandProperty OwningProcess -Unique)) {
        $proc = Get-Process -Id $c -ErrorAction SilentlyContinue
        if ($proc) {
            Write-Host ("停止 {0} (PID {1}) 端口 {2}" -f $proc.ProcessName, $c, $p)
            Stop-Process -Id $c -Force
        }
    }
}
Write-Host '完成。'
