# 一键启动：Redis → 后端 jar → 前端 dev
param(
    [switch]$SkipRedis,
    [switch]$SkipFrontend,
    [switch]$Rebuild,
    [int]$BackendPort = 8081
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$redisDir = 'D:\Redis'
$backendDir = Join-Path $root 'backend'
$frontendDir = Join-Path $root 'frontend'
$jar = Join-Path $backendDir 'target\game-price-backend-1.0.0.jar'

function Test-Port([int]$p) {
    (Test-NetConnection -ComputerName 127.0.0.1 -Port $p -WarningAction SilentlyContinue).TcpTestSucceeded
}

Write-Host '=== 游戏比价平台 · 本地启动 ===' -ForegroundColor Cyan

# 1) Redis
if (-not $SkipRedis) {
    if (Test-Port 6379) {
        Write-Host '[1/3] Redis 已在运行（6379）'
    } else {
        Write-Host '[1/3] 启动 Redis ...'
        Start-Process -FilePath (Join-Path $redisDir 'redis-server.exe') `
            -ArgumentList (Join-Path $redisDir 'redis.conf') `
            -WorkingDirectory $redisDir -WindowStyle Minimized
        Start-Sleep -Seconds 2
        if (-not (Test-Port 6379)) { throw 'Redis 启动失败：检查 D:\Redis\redis.conf 与 redis-server.exe' }
        Write-Host '      Redis OK'
    }
} else { Write-Host '[1/3] 跳过 Redis' }

# 2) 后端
if (Test-Port $BackendPort) {
    Write-Host "[2/3] 端口 $BackendPort 已被占用（多半是旧后端在跑），跳过启动" -ForegroundColor Yellow
} else {
    if ($Rebuild -or -not (Test-Path $jar)) {
        Write-Host '[2/3] 重新打包后端 ...'
        Push-Location $backendDir
        try { & mvn -q clean package -DskipTests } finally { Pop-Location }
        if (-not (Test-Path $jar)) { throw "打包失败，未生成 $jar" }
    }
    Write-Host '[2/3] 启动后端 ...'
        Start-Process -FilePath 'java' -ArgumentList '-Djavax.net.ssl.trustStoreType=Windows-ROOT', '-jar', $jar `
        -WorkingDirectory $backendDir -WindowStyle Minimized `
        -RedirectStandardOutput (Join-Path $backendDir 'app.log') `
        -RedirectStandardError  (Join-Path $backendDir 'app.err.log')
    for ($i = 0; $i -lt 40; $i++) {
        Start-Sleep -Seconds 1
        if (Test-Port $BackendPort) { break }
    }
    if (-not (Test-Port $BackendPort)) { throw '后端启动失败：看 backend\app.log / app.err.log' }
    Write-Host "      后端 OK → http://localhost:$BackendPort/api"
}

# 3) 前端
if (-not $SkipFrontend) {
    Write-Host '[3/3] 启动前端（npm run dev）...'
    Start-Process -FilePath 'powershell' `
        -ArgumentList '-NoExit', '-Command', "cd '$frontendDir'; npm run dev" `
        -WindowStyle Normal
    Write-Host '      前端 → http://localhost:5173'
} else { Write-Host '[3/3] 跳过前端' }

Write-Host '=== 启动流程结束 ===' -ForegroundColor Cyan
Write-Host '提示：后端日志 backend\app.log；停止用 .\stop.ps1'
