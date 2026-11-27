# 游戏比价网站 一键启动脚本
# 由计划任务 GamePriceAutoStart 在用户登录后调用，也可以手动运行
# 作用：按依赖顺序拉起 Redis / MySQL / 后端 jar / Caddy / cloudflared 隧道

$ErrorActionPreference = "Continue"
$root    = "D:\游戏比价平台"
$tools   = Join-Path $root "tools"
$logFile = Join-Path $tools "startup.log"

function Write-Log([string]$msg) {
    "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')  $msg" | Out-File -FilePath $logFile -Append -Encoding utf8
}

function Test-Port([int]$port) {
    $null -ne (Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue)
}

Write-Log "===== 启动检查开始 ====="

# 1) Redis（登出黑名单依赖）
if (-not (Test-Port 6379)) {
    $redisSvc = Get-Service | Where-Object { $_.Name -like "*redis*" -and $_.Status -ne "Running" } | Select-Object -First 1
    if ($redisSvc) {
        Start-Service -Name $redisSvc.Name -ErrorAction SilentlyContinue
        Write-Log "Redis 服务 $($redisSvc.Name) 已启动"
    }
    elseif (Test-Path "D:\Redis\redis-server.exe") {
        Start-Process -FilePath "D:\Redis\redis-server.exe" -ArgumentList "D:\Redis\redis.conf" -WorkingDirectory "D:\Redis" -WindowStyle Hidden
        Write-Log "Redis 进程已启动"
    }
}

# 2) MySQL（一般已是自启动服务，兜底再查一次）
if (-not (Test-Port 3306)) {
    $mysqlSvc = Get-Service | Where-Object { $_.Name -like "*mysql*" } | Select-Object -First 1
    if ($mysqlSvc) {
        Start-Service -Name $mysqlSvc.Name -ErrorAction SilentlyContinue
        Write-Log "MySQL 服务 $($mysqlSvc.Name) 已启动"
    }
}

# 3) 后端 Spring Boot（端口 8081）
if (-not (Test-Port 8081)) {
    $java = "D:\jdk17\jdk-17.0.12\bin\java.exe"
    $jar  = Join-Path $root "backend\target\game-price-backend-1.0.0.jar"
    if ((Test-Path $java) -and (Test-Path $jar)) {
        Start-Process -FilePath $java -ArgumentList "-jar", $jar `
            -WorkingDirectory (Join-Path $root "backend") -WindowStyle Hidden `
            -RedirectStandardOutput (Join-Path $root "backend\app.log") `
            -RedirectStandardError  (Join-Path $root "backend\app.err.log")
        Write-Log "后端已启动，等待 8081 就绪"
        for ($i = 0; $i -lt 45; $i++) {
            Start-Sleep -Seconds 2
            if (Test-Port 8081) { Write-Log "后端就绪，用时约 $($i * 2) 秒"; break }
        }
    }
    else {
        Write-Log "java 或 jar 不存在，跳过后端启动"
    }
}

# 4) Caddy 统一入口（8090）
if (-not (Test-Port 8090)) {
    Start-Process -FilePath (Join-Path $tools "caddy.exe") `
        -ArgumentList "run", "--config", (Join-Path $tools "Caddyfile") `
        -WorkingDirectory $tools -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $tools "caddy.out.log") `
        -RedirectStandardError  (Join-Path $tools "caddy.err.log")
    Start-Sleep -Seconds 3
    Write-Log "Caddy 已启动"
}

# 5) cloudflared 隧道（quick tunnel 每次重启域名都会变，抓到后写到 current-url.txt）
if (-not (Get-Process -Name cloudflared -ErrorAction SilentlyContinue)) {
    $cfLog = Join-Path $tools "tunnel.log"
    Remove-Item -LiteralPath $cfLog -Force -ErrorAction SilentlyContinue
    Start-Process -FilePath (Join-Path $tools "cloudflared.exe") `
        -ArgumentList "tunnel", "--url", "http://localhost:8090", "--protocol", "http2" -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $tools "tunnel.out.log") `
        -RedirectStandardError  $cfLog
    Write-Log "隧道已启动，等待分配域名"
    $url = $null
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 2
        if (Test-Path $cfLog) {
            $text = Get-Content -LiteralPath $cfLog -Raw -ErrorAction SilentlyContinue
            if ($text -match 'https://[a-z0-9][a-z0-9-]*\.trycloudflare\.com') { $url = $Matches[0]; break }
        }
    }
    if ($url) {
        "当前公网地址（cloudflared 每次重启都会变）`r`n$url`r`n生成时间：$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" |
            Out-File -FilePath (Join-Path $tools "current-url.txt") -Encoding utf8
        Write-Log "隧道域名：$url"
    }
    else {
        Write-Log "未捕获到隧道域名，请查看 tools\tunnel.log"
    }
}

Write-Log "===== 启动流程结束 ====="
