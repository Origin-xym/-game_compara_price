-- ============================================================
-- 游戏跨平台比价网站 数据库结构 v1.0
-- 库：game_compara_price   字符集：utf8mb4   引擎：InnoDB
-- 说明：不建物理外键，用逻辑关联 + 索引（便于手工维护与后续迁移）
-- ============================================================

CREATE DATABASE IF NOT EXISTS game_compara_price
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE game_compara_price;

-- ------------------------------------------------------------
-- 1. 游戏类型字典
-- ------------------------------------------------------------
DROP TABLE IF EXISTS game_type;
CREATE TABLE game_type (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    code         VARCHAR(32)     NOT NULL                COMMENT '类型编码，如 ACTION/RPG',
    name         VARCHAR(32)     NOT NULL                COMMENT '展示名称，如 动作',
    sort_no      INT             NOT NULL DEFAULT 0      COMMENT '排序',
    deleted      TINYINT         NOT NULL DEFAULT 0      COMMENT '0正常 1删除',
    create_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏类型字典';

-- ------------------------------------------------------------
-- 2. 游戏
-- ------------------------------------------------------------
DROP TABLE IF EXISTS game;
CREATE TABLE game (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    name          VARCHAR(200)    NOT NULL                COMMENT '中文名（去空格后的唯一键）',
    name_en       VARCHAR(200)             DEFAULT NULL   COMMENT '英文名，搜索用',
    cover_url     VARCHAR(500)             DEFAULT NULL   COMMENT '封面图',
    summary       VARCHAR(1000)            DEFAULT NULL   COMMENT '简介',
    game_type_id  BIGINT UNSIGNED NOT NULL                COMMENT '类型，逻辑关联 game_type.id',
    release_year  SMALLINT                 DEFAULT NULL   COMMENT '发行年份（Q7 若加年份筛选则用）',
    status        TINYINT         NOT NULL DEFAULT 1      COMMENT '1上架 0下架（AC-11.3）',
    sort_weight   INT             NOT NULL DEFAULT 0      COMMENT '列表默认排序权重',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)                             COMMENT 'AC-11.4 重复新增拦截',
    KEY idx_type_status (game_type_id, status),
    KEY idx_status_weight (status, sort_weight)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏主数据';

-- ------------------------------------------------------------
-- 3. 平台
-- ------------------------------------------------------------
DROP TABLE IF EXISTS platform;
CREATE TABLE platform (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    code              VARCHAR(32)     NOT NULL                COMMENT '平台编码 STEAM/EPIC/PSN/XBOX/NS',
    name              VARCHAR(64)     NOT NULL                COMMENT '展示名',
    region            VARCHAR(16)     NOT NULL                COMMENT '区域 US/HK/CN/TW…',
    currency          CHAR(3)         NOT NULL                COMMENT '该区域结算币种 USD/HKD…',
    link_template     VARCHAR(500)    NOT NULL                COMMENT '商店链接模板，占位 {externalId}（AC-7.2）',
    data_source_type  TINYINT         NOT NULL                COMMENT '1官方API 2授权第三方 3仅手动（AC-12.7 依据）',
    api_ready         TINYINT         NOT NULL DEFAULT 0      COMMENT '数据源是否就绪，0未就绪（AC-13.4）',
    enabled           TINYINT         NOT NULL DEFAULT 1      COMMENT '1启用 0禁用，禁用不参与最低价（AC-13.2）',
    remark            VARCHAR(200)             DEFAULT NULL,
    deleted           TINYINT         NOT NULL DEFAULT 0,
    create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code_region (code, region),
    KEY idx_enabled (enabled)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '平台信息';

-- ------------------------------------------------------------
-- 4. 价格记录（核心表）
--    当前价规则：is_current = 1 且 status = 1；历史价 is_current = NULL
-- ------------------------------------------------------------
DROP TABLE IF EXISTS price_record;
CREATE TABLE price_record (
    id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    game_id           BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 game.id',
    platform_id       BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 platform.id',
    currency          CHAR(3)         NOT NULL                COMMENT '原币种',
    original_amount   DECIMAL(10,2)   NOT NULL                COMMENT '原币现价（实付）',
    original_price    DECIMAL(10,2)            DEFAULT NULL   COMMENT '原币划线原价，无折扣时等于 original_amount',
    discount_rate     DECIMAL(5,2)             DEFAULT NULL   COMMENT '折扣率，40.00 表示减 40%（AC-6.1）',
    discount_end_time DATETIME                 DEFAULT NULL   COMMENT '折扣截止时间，为空表示无截止（AC-6.1）',
    rate              DECIMAL(12,6)   NOT NULL                COMMENT '入库时使用的汇率（原币→CNY）',
    rate_time         DATETIME        NOT NULL                COMMENT '汇率快照时间（AC-4.7 展示汇率时间）',
    cny_amount        DECIMAL(10,2)   NOT NULL                COMMENT '人民币实付价，入库即固化（AC-4.5/4.6）',
    source_type       TINYINT         NOT NULL                COMMENT '1手动 2官方API 3第三方授权',
    source_ref        VARCHAR(200)             DEFAULT NULL   COMMENT '来源标识：采集批次号/录入备注（AC-12.5）',
    operator          VARCHAR(64)     NOT NULL                COMMENT '操作人用户名或采集任务名（AC-12.5）',
    recorded_at       DATETIME        NOT NULL                COMMENT '该价格的生效时间（来源侧时间）',
    is_current        TINYINT                  DEFAULT NULL   COMMENT '1当前生效 NULL历史或待确认',
    status            TINYINT         NOT NULL DEFAULT 1      COMMENT '1生效 2待确认 3已驳回（AC-12.6）',
    verified_by       VARCHAR(64)              DEFAULT NULL   COMMENT '待确认队列的审核人',
    verified_time     DATETIME                 DEFAULT NULL   COMMENT '审核时间',
    create_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_current (game_id, platform_id, is_current)  COMMENT 'AC-12.4 同游戏同平台仅一条当前价（NULL 不受限）',
    KEY idx_gp_time (game_id, platform_id, recorded_at)        COMMENT 'AC-5.2 走势按时间取点',
    KEY idx_status (status),
    KEY idx_current_platform (is_current, platform_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '跨平台价格记录';

-- ------------------------------------------------------------
-- 5. 汇率快照
-- ------------------------------------------------------------
DROP TABLE IF EXISTS exchange_rate_snapshot;
CREATE TABLE exchange_rate_snapshot (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    base_currency  CHAR(3)         NOT NULL                COMMENT '原币，如 USD',
    quote_currency CHAR(3)         NOT NULL DEFAULT 'CNY'  COMMENT '目标币',
    rate           DECIMAL(12,6)   NOT NULL                COMMENT '1 单位原币 = rate 人民币',
    provider       VARCHAR(64)     NOT NULL                COMMENT '汇率服务商标识',
    fetched_at     DATETIME        NOT NULL                COMMENT '拉取时间（AC-4.7 沿用最近成功快照）',
    success        TINYINT         NOT NULL DEFAULT 1      COMMENT '1成功 0失败',
    error_msg      VARCHAR(200)             DEFAULT NULL,
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_base_quote_time (base_currency, quote_currency, fetched_at),
    KEY idx_base_fetched (base_currency, fetched_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '汇率快照';

-- ------------------------------------------------------------
-- 6. 第三方史低（独立表，禁止参与最低价计算）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS historical_low;
CREATE TABLE historical_low (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    game_id     BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 game.id',
    platform_id BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 platform.id',
    low_amount  DECIMAL(10,2)   NOT NULL                COMMENT '史低原币金额',
    currency    CHAR(3)         NOT NULL                COMMENT '史低币种',
    low_cny     DECIMAL(10,2)            DEFAULT NULL   COMMENT '史低折算人民币（仅展示）',
    low_date    DATE                     DEFAULT NULL   COMMENT '史低发生日期',
    source      VARCHAR(100)    NOT NULL                COMMENT '数据源名（AC-5.3/D6 可追溯）',
    source_ref  VARCHAR(200)             DEFAULT NULL   COMMENT '第三方记录ID',
    fetched_at  DATETIME        NOT NULL                COMMENT '本次同步时间',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_game_platform_source (game_id, platform_id, source),
    KEY idx_game (game_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '官方史低价（第三方授权源）';

-- ------------------------------------------------------------
-- 7. 游戏 × 平台 关联（外部ID / 商店链接 / 是否上架）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS game_platform;
CREATE TABLE game_platform (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    game_id     BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 game.id',
    platform_id BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 platform.id',
    external_id VARCHAR(100)             DEFAULT NULL   COMMENT '平台侧商品ID，如 Steam appid',
    store_url   VARCHAR(500)             DEFAULT NULL   COMMENT '最终商店链接，优先于模板生成（AC-7.3 可后台改）',
    listed      TINYINT         NOT NULL DEFAULT 1      COMMENT '1该平台已上架 0未上架（AC-4.3）',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_game_platform (game_id, platform_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏平台关联';

-- ------------------------------------------------------------
-- 8. 锁区风险
-- ------------------------------------------------------------
DROP TABLE IF EXISTS region_risk;
CREATE TABLE region_risk (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    platform_id BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 platform.id',
    region      VARCHAR(16)     NOT NULL                COMMENT '区域',
    risk_level  TINYINT         NOT NULL DEFAULT 0      COMMENT '0无风险 1低 2中 3高（AC-6.2）',
    reason      VARCHAR(500)             DEFAULT NULL   COMMENT '风险原因（AC-6.2）',
    source      VARCHAR(200)             DEFAULT NULL   COMMENT '判定依据来源（AC-6.4）',
    updated_by  VARCHAR(64)              DEFAULT NULL   COMMENT '最后修改人（AC-6.4）',
    deleted     TINYINT         NOT NULL DEFAULT 0,
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_region (platform_id, region)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '锁区风险（Q5 未定时按平台+区域维护）';

-- ------------------------------------------------------------
-- 9. 用户
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(64)     NOT NULL                COMMENT '登录名，唯一',
    password_hash   VARCHAR(100)    NOT NULL                COMMENT 'BCrypt 密文（AC-8.6 不明文）',
    nickname        VARCHAR(64)              DEFAULT NULL,
    email           VARCHAR(128)             DEFAULT NULL   COMMENT 'Q6 若支持邮箱注册再用',
    phone           VARCHAR(32)              DEFAULT NULL   COMMENT 'Q6 若支持手机注册再用',
    role            TINYINT         NOT NULL DEFAULT 1      COMMENT '1普通用户 2管理员（AC-11.5）',
    status          TINYINT         NOT NULL DEFAULT 1      COMMENT '1正常 0禁用',
    failed_count    INT             NOT NULL DEFAULT 0      COMMENT '连续登录失败次数（AC-8.6 防爆破）',
    locked_until    DATETIME                 DEFAULT NULL   COMMENT '锁定到期时间（AC-8.6）',
    last_login_time DATETIME                 DEFAULT NULL,
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户';

-- ------------------------------------------------------------
-- 10. 收藏（取消即删行，不做软删）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 sys_user.id（AC-9.4 隔离）',
    game_id     BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 game.id',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间（AC-9.2 展示）',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_game (user_id, game_id),
    KEY idx_user_time (user_id, create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏';

-- ------------------------------------------------------------
-- 11. 降价提醒规则
-- ------------------------------------------------------------
DROP TABLE IF EXISTS alert_rule;
CREATE TABLE alert_rule (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 sys_user.id',
    game_id         BIGINT UNSIGNED NOT NULL                COMMENT '逻辑关联 game.id',
    platform_id     BIGINT UNSIGNED          DEFAULT NULL   COMMENT 'NULL 表示全平台任一',
    type            TINYINT         NOT NULL                COMMENT '1目标价 2跌至史低 3任意降价（AC-10.1）',
    target_price    DECIMAL(10,2)            DEFAULT NULL   COMMENT '目标价，type=1 必填（AC-10.1 非法值拒绝）',
    baseline_price  DECIMAL(10,2)   NOT NULL                COMMENT '基准价=设置时最低人民币价（D8/AC-10.7）',
    baseline_time   DATETIME        NOT NULL                COMMENT '基准取值时间',
    status          TINYINT         NOT NULL DEFAULT 1      COMMENT '1启用 0暂停（AC-10.5）',
    last_fired_price DECIMAL(10,2)           DEFAULT NULL   COMMENT '上次触发价（AC-10.3 去重锚点）',
    last_fired_time DATETIME                 DEFAULT NULL   COMMENT '上次触发时间（AC-10.3）',
    deleted         TINYINT         NOT NULL DEFAULT 0,
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_status (user_id, status),
    KEY idx_game_type (game_id, type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '降价提醒规则';

-- ------------------------------------------------------------
-- 12. 站内通知
-- ------------------------------------------------------------
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT UNSIGNED NOT NULL                COMMENT '接收人',
    alert_rule_id  BIGINT UNSIGNED          DEFAULT NULL   COMMENT '来源规则',
    game_id        BIGINT UNSIGNED NOT NULL                COMMENT '关联游戏（AC-10.2 可进详情）',
    platform_id    BIGINT UNSIGNED          DEFAULT NULL   COMMENT '触发平台',
    condition_type TINYINT         NOT NULL                COMMENT '1目标价 2史低 3任意降价',
    price_cny      DECIMAL(10,2)   NOT NULL                COMMENT '触发时人民币价（AC-10.2）',
    title          VARCHAR(200)    NOT NULL,
    content        VARCHAR(1000)            DEFAULT NULL,
    is_read        TINYINT         NOT NULL DEFAULT 0      COMMENT '0未读 1已读（AC-10.6）',
    create_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_user_time (user_id, create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站内通知';

-- ------------------------------------------------------------
-- 13. 后台操作留痕
-- ------------------------------------------------------------
DROP TABLE IF EXISTS admin_audit_log;
CREATE TABLE admin_audit_log (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    operator_id   BIGINT UNSIGNED          DEFAULT NULL   COMMENT '操作人ID',
    operator_name VARCHAR(64)     NOT NULL                COMMENT '操作人用户名',
    module        VARCHAR(32)     NOT NULL                COMMENT '模块 GAME/PLATFORM/PRICE',
    action        VARCHAR(32)     NOT NULL                COMMENT '动作 CREATE/UPDATE/OFFLINE/PRICE_CHANGE',
    target_id     BIGINT UNSIGNED          DEFAULT NULL   COMMENT '目标主键',
    target_name   VARCHAR(200)             DEFAULT NULL   COMMENT '目标名称快照',
    before_json   JSON                     DEFAULT NULL   COMMENT '变更前（AC-14.4 可追溯）',
    after_json    JSON                     DEFAULT NULL   COMMENT '变更后',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_module_target (module, target_id),
    KEY idx_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '后台操作留痕';

-- ------------------------------------------------------------
-- 14. 采集任务日志
-- ------------------------------------------------------------
DROP TABLE IF EXISTS collect_task_log;
CREATE TABLE collect_task_log (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    platform_id   BIGINT UNSIGNED NOT NULL                COMMENT '采集平台',
    batch_no      VARCHAR(64)     NOT NULL                COMMENT '批次号（AC-12.5 关联 price_record.source_ref）',
    task_type     TINYINT         NOT NULL DEFAULT 1      COMMENT '1全量 2增量',
    start_time    DATETIME        NOT NULL,
    end_time      DATETIME                 DEFAULT NULL,
    total         INT             NOT NULL DEFAULT 0      COMMENT '处理条数',
    success_count INT             NOT NULL DEFAULT 0,
    fail_count    INT             NOT NULL DEFAULT 0,
    status        TINYINT         NOT NULL DEFAULT 0      COMMENT '0运行中 1成功 2部分失败 3失败',
    error_msg     VARCHAR(1000)            DEFAULT NULL   COMMENT '失败原因（AC-12.3 告警）',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_batch (batch_no),
    KEY idx_platform_time (platform_id, start_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '采集任务日志';

-- ------------------------------------------------------------
-- 15. 统一口径视图：某游戏当前最低人民币价
--     列表起价 / 详情最低 / 收藏最低 三处都只读这里（AC-14.1）
--     史低表不参与，满足 AC-14.2
-- ------------------------------------------------------------
CREATE OR REPLACE VIEW v_game_min_price AS
SELECT p.game_id,
       MIN(p.cny_amount)              AS min_price_cny,
       COUNT(DISTINCT p.platform_id)  AS platform_count
FROM price_record p
JOIN platform pf ON pf.id = p.platform_id AND pf.enabled = 1
WHERE p.is_current = 1
  AND p.status = 1
GROUP BY p.game_id;
