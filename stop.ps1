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

# 采集用浏览器（Epic 浏览器渲染采集会拉起一个 Edge/Chrome，窗口在屏幕外常驻）也需要一起收
# 只匹配本项目专用目录/调试端口的进程，不会动用户自己的浏览器
$browserProcs = Get-CimInstance Win32_Process -ErrorAction SilentlyContinue |
    Where-Object {
        ($_.Name -eq 'msedge.exe' -or $_.Name -eq 'chrome.exe') -and
        ($_.CommandLine -like '*gameprice-browser*' -or $_.CommandLine -like '*remote-debugging-port=9333*')
    }
if ($browserProcs) {
    foreach ($b in $browserProcs) {
        Stop-Process -Id $b.ProcessId -Force -ErrorAction SilentlyContinue
    }
    Write-Host ("停止采集浏览器进程 {0} 个" -f @($browserProcs).Count)
} else {
    Write-Host '采集浏览器未运行'
}

Write-Host '完成。'
