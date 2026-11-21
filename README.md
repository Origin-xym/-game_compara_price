# 游戏跨平台比价网站（game_compara_price）

SpringBoot 3.3.5 + Vue 3 + MySQL 8 + Redis，单体前后端分离。

## 目录

- `backend/` Java 17 / Maven，包根 `com.gameprice`，端口 **8081**，`context-path = /api`
- `frontend/` Vue 3 + Vite + Element Plus + Pinia + ECharts
- `backend/src/main/resources/db/schema.sql` 14 表 + 1 视图（`v_game_min_price`）

## 环境依赖

| 组件 | 版本 / 位置 |
|---|---|
| JDK | 17（`D:\jdk17\jdk-17.0.12`，需把 `%JAVA_HOME%\bin` 加进 PATH） |
| Maven | 3.10（`D:\Maven\apache-maven-3.10.0`，同样补 `bin` 到 PATH） |
| Node | 22.x / npm 10.x |
| MySQL | 8.0，服务 3306，库名 `game_compara_price` |
| Redis | `D:\Redis`，`redis-server.exe` 需手动启动（登出黑名单依赖它） |

## 启动

1. 导库：`mysql -u root -p game_compara_price < backend/src/main/resources/db/schema.sql`
2. 启 Redis：`D:\Redis\redis-server.exe D:\Redis\redis.conf`
3. 启后端（**两种，都可；不要用 `mvn spring-boot:run`**）：
    - IDEA 直接运行 `com.gameprice.GamePriceApplication`
    - `cd backend; mvn -q clean package -DskipTests; java -jar target\game-price-backend-1.0.0.jar`
4. 启前端：`cd frontend; npm install; npm run dev`（默认 5173，`vite.config.js` 代理 `/api` → 8081）

> `spring-boot:run` 在中文路径 `D:\游戏比价平台` 下会因插件拼 classpath 失败报 `ClassNotFoundException`，与代码无关，固定用上面两种方式。

## 配置要点（application.yml）

- `app.jwt.secret` 必须 ≥ 32 字节（HS256 要求），否则启动抛 `WeakKeyException`
- `app.jwt.expire-minutes: 120`
- `app.price.stale-hours: 24`、`app.price.cny-scale: 2`
- `app.alert.check-interval-ms: 60000`（提醒判定周期）
- `app.collect.volatility-threshold: 0.5`、`app.collect.cron: 0 0 4 * * ?`

## 数据口径（全站唯一，改这里等于改全站）

- 当前价 = `price_record.is_current = 1 AND status = 1`
- 平台可用 = `platform.enabled = 1 AND api_ready = 1 AND deleted = 0`
- 三处读取（列表起价 / 详情最低 / 收藏最低）共用 `PriceRecordMapper.selectCurrentPrices` 与 `GameMapper.xml`
- 史低（`historical_low`）只展示，**不参与**最低价计算

## 后台入口（均需管理员账号，role=2）

`/admin/games` 游戏管理 · `/admin/prices` 价格审核 · `/admin/platforms` 平台信息 · `/admin/risks` 锁区维护

## 验收

56 条 AC 见 `游戏比价网站_验收标准_v0.3.md`；终验用：

1. `GET /api/admin/consistency/scan?limit=20` → 返回 `[]`
2. 录入→可见 ≤ 1000ms
3. 列表 P95 ≤ 300ms（200 条规模）
4. `admin_audit_log` 覆盖全部关键操作

## 部署与运维（本地单机）

### 一键启停

```powershell
cd D:\游戏比价平台
.\start.ps1            # Redis + 后端 jar + 前端 dev
.\start.ps1 -Rebuild    # 改了后端代码：重打包后再启（先 .\stop.ps1）
.\stop.ps1              # 停 8081 / 5173
.\stop.ps1 -IncludeRedis  # 连 Redis 一起停
