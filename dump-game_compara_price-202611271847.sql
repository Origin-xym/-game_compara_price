-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: game_compara_price
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_audit_log`
--

DROP TABLE IF EXISTS `admin_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_audit_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operator_id` bigint unsigned DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(64) NOT NULL COMMENT '操作人用户名',
  `module` varchar(32) NOT NULL COMMENT '模块 GAME/PLATFORM/PRICE',
  `action` varchar(32) NOT NULL COMMENT '动作 CREATE/UPDATE/OFFLINE/PRICE_CHANGE',
  `target_id` bigint unsigned DEFAULT NULL COMMENT '目标主键',
  `target_name` varchar(200) DEFAULT NULL COMMENT '目标名称快照',
  `before_json` json DEFAULT NULL COMMENT '变更前（AC-14.4 可追溯）',
  `after_json` json DEFAULT NULL COMMENT '变更后',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_module_target` (`module`,`target_id`),
  KEY `idx_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=237 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台操作留痕';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_audit_log`
--

LOCK TABLES `admin_audit_log` WRITE;
/*!40000 ALTER TABLE `admin_audit_log` DISABLE KEYS */;
INSERT INTO `admin_audit_log` VALUES (1,2,'testuser','GAME','CREATE',10,'回归测试游戏',NULL,'{\"id\": 10, \"name\": \"回归测试游戏\", \"status\": 1, \"createTime\": \"2026-11-21T10:16:18.542089\", \"gameTypeId\": 2, \"sortWeight\": 0, \"updateTime\": \"2026-11-21T10:16:18.542089\"}','2026-11-21 10:16:19'),(2,2,'testuser','GAME','UPDATE',2,'只狼：影逝二度（改名测试）','{\"id\": 2, \"name\": \"只狼：影逝二度\", \"nameEn\": \"Sekiro: Shadows Die Twice\", \"status\": 1, \"createTime\": \"2026-11-19T17:38:09\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-19T17:38:09\"}','{\"id\": 2, \"name\": \"只狼：影逝二度（改名测试）\", \"nameEn\": \"Sekiro: Shadows Die Twice\", \"status\": 1, \"coverUrl\": \"https://example.com/sekiro-cover.jpg\", \"createTime\": \"2026-11-19T17:38:09\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-21T10:17:05\"}','2026-11-21 10:17:06'),(3,2,'testuser','GAME','OFFLINE',10,'回归测试游戏','{\"status\": 1}','{\"status\": 0}','2026-11-21 10:18:07'),(4,2,'testuser','GAME','UPDATE',2,'只狼：影逝二度','{\"id\": 2, \"name\": \"只狼：影逝二度（改名测试）\", \"nameEn\": \"Sekiro: Shadows Die Twice\", \"status\": 1, \"coverUrl\": \"https://example.com/sekiro-cover.jpg\", \"createTime\": \"2026-11-19T17:38:09\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-21T10:17:05\"}','{\"id\": 2, \"name\": \"只狼：影逝二度\", \"nameEn\": \"Sekiro: Shadows Die Twice\", \"status\": 1, \"createTime\": \"2026-11-19T17:38:09\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-21T10:18:38\"}','2026-11-21 10:18:38'),(5,2,'testuser','GAME','OFFLINE',8,'艾尔登法环','{\"status\": 1}','{\"status\": 0}','2026-11-21 10:41:13'),(6,2,'testuser','GAME','ONLINE',8,'艾尔登法环','{\"status\": 0}','{\"status\": 1}','2026-11-21 10:44:01'),(7,2,'testuser','price','APPROVE_PENDING_PRICE',18,'gameId=1',NULL,'{\"operator\": \"testuser\", \"cnyAmount\": 3931.2}','2026-11-21 19:02:18'),(8,2,'testuser','price','REJECT_PENDING_PRICE',14,'gameId=1',NULL,'{\"reason\": \"价格异常偏高，人工复核驳回\", \"operator\": \"testuser\"}','2026-11-21 19:02:28'),(27,2,'testuser','price','REJECT_PENDING_PRICE',51,'gameId=1',NULL,'{\"reason\": \"\", \"operator\": \"testuser\"}','2026-11-23 09:26:31'),(28,2,'testuser','GAME','CREATE',12,'双人成行',NULL,'{\"id\": 12, \"name\": \"双人成行\", \"nameEn\": \"It Takes Two - Friend\'s Pass\", \"status\": 1, \"summary\": \"游玩《双人成行》，踏上生命中最疯狂的旅程，这是一款别开生面的平台冒险游戏，完全为合作模式而设计。利用好友通行证*邀请一位好友免费游玩，共同体验多种多样的乐趣，享受颠覆性的玩法挑战。扮演相互看不顺眼的科迪和小梅夫妇，这两个人被魔咒变成了玩偶。他们一起被困在一个奇幻世界里，每个角落都隐藏着意想不到的东西，他们不得不一起克服挑战，同时挽救他们破裂的关系。\\n\\n在每个新关卡中，掌握角色独特且与对方息息相关的能力。一定要互相帮助，克服大量出乎意料的障碍，共度搞笑欢乐的时刻。用力踢流氓松鼠毛茸茸的尾巴，绕过一条内裤，在热闹的夜店里开舞会，坐雪橇穿过神奇的雪花玻璃球。一个真挚又搞笑的故事在游戏中徐徐展开，交织成独特的隐喻体验。\\n\\n《双人成行》由业界合作游戏领导者且屡获殊荣的工作室 Hazelight 开发。他们即将带您踏上狂野而奇妙的旅程，只有一件事是确定的：二人同心，其利断金。\", \"coverUrl\": \"https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/1426210/header_schinese.jpg?t=1777395376\", \"createTime\": \"2026-11-23T09:31:48.6726467\", \"gameTypeId\": 2, \"sortWeight\": 0, \"updateTime\": \"2026-11-23T09:31:48.6726467\", \"releaseYear\": 2021}','2026-11-23 09:31:49'),(29,2,'testuser','price','MANUAL_CREATE_PRICE',76,'gameId=1,platformId=1',NULL,'{\"currency\": \"USD\", \"operator\": \"admin\", \"cnyAmount\": 143.93, \"originalAmount\": 19.99}','2026-11-23 09:56:33'),(30,2,'testuser','price','MANUAL_CREATE_PRICE',77,'gameId=1,platformId=1',NULL,'{\"currency\": \"USD\", \"operator\": \"admin\", \"cnyAmount\": 143.93, \"originalAmount\": 19.99}','2026-11-23 09:57:41'),(31,2,'testuser','price','MANUAL_CREATE_PRICE',78,'gameId=1,platformId=1',NULL,'{\"currency\": \"USD\", \"operator\": \"admin\", \"cnyAmount\": 72.0, \"originalAmount\": 10.0}','2026-11-23 09:58:42'),(32,2,'testuser','price','APPROVE_PENDING_PRICE',79,'gameId=1',NULL,'{\"operator\": \"testuser\", \"cnyAmount\": 216.0}','2026-11-23 10:04:43'),(33,2,'testuser','price','MANUAL_CREATE_PRICE',84,'gameId=1,platformId=1',NULL,'{\"currency\": \"USD\", \"operator\": \"admin\", \"cnyAmount\": 72.0, \"originalAmount\": 10.0}','2026-11-23 10:18:24'),(34,2,'testuser','platform','UPDATE_PLATFORM',1,'STEAM/US','{\"code\": \"STEAM\", \"name\": \"Steam\", \"region\": \"US\", \"remark\": \"币种变更测试\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"USD\", \"linkTemplate\": \"https://store.steampowered.com/app/{externalId}\", \"dataSourceType\": 1}','{\"code\": \"STEAM\", \"name\": \"Steam\", \"region\": \"US\", \"remark\": \"币种变更测试\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"USD\", \"linkTemplate\": \"https://store.steampowered.com/app/{externalId}\", \"dataSourceType\": 1}','2026-11-23 11:42:54'),(35,2,'testuser','platform','UPDATE_PLATFORM',2,'EPIC/US','{\"code\": \"EPIC\", \"name\": \"Epic Games\", \"region\": \"US\", \"enabled\": 1, \"apiReady\": 0, \"currency\": \"USD\", \"linkTemplate\": \"https://store.epicgames.com/p/{externalId}\", \"dataSourceType\": 1}','{\"code\": \"EPIC\", \"name\": \"Epic Games\", \"region\": \"US\", \"remark\": \"\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"USD\", \"linkTemplate\": \"https://store.epicgames.com/p/{externalId}\", \"dataSourceType\": 1}','2026-11-23 11:43:21'),(36,2,'testuser','price','REJECT_PENDING_PRICE',93,'gameId=1',NULL,'{\"reason\": \"\", \"operator\": \"testuser\"}','2026-11-23 12:44:10'),(37,2,'testuser','price','REJECT_PENDING_PRICE',92,'gameId=8',NULL,'{\"reason\": \"\", \"operator\": \"testuser\"}','2026-11-23 12:44:11'),(38,2,'testuser','price','REJECT_PENDING_PRICE',90,'gameId=2',NULL,'{\"reason\": \"\", \"operator\": \"testuser\"}','2026-11-23 12:44:12'),(39,2,'testuser','price','REJECT_PENDING_PRICE',89,'gameId=1',NULL,'{\"reason\": \"\", \"operator\": \"testuser\"}','2026-11-23 12:44:14'),(40,2,'testuser','platform','DISABLE_PLATFORM',4,'STEAM/HK','{\"enabled\": 1}','{\"enabled\": 0}','2026-11-23 12:44:20'),(41,2,'testuser','platform','DISABLE_PLATFORM',3,'XBOX/US','{\"enabled\": 1}','{\"enabled\": 0}','2026-11-23 12:44:22'),(42,2,'testuser','platform','DISABLE_PLATFORM',2,'EPIC/US','{\"enabled\": 1}','{\"enabled\": 0}','2026-11-23 12:44:25'),(43,2,'testuser','platform','DISABLE_PLATFORM',1,'STEAM/US','{\"enabled\": 1}','{\"enabled\": 0}','2026-11-23 12:44:27'),(44,2,'testuser','platform','CREATE_PLATFORM',9,'STEAM/CN',NULL,'{\"code\": \"STEAM\", \"name\": \"Steam\", \"region\": \"CN\", \"remark\": \"\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"CNY\", \"linkTemplate\": \"https://store.steampowered.com/app/{externalId}?cc=cn\", \"dataSourceType\": 1}','2026-11-23 12:46:52'),(45,2,'testuser','platform','CREATE_PLATFORM',10,'EPIC/CN',NULL,'{\"code\": \"EPIC\", \"name\": \"Epic Games\", \"region\": \"CN\", \"remark\": \"\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"CNY\", \"linkTemplate\": \"https://store.epicgames.com/p/{externalId}\", \"dataSourceType\": 2}','2026-11-23 12:47:49'),(46,2,'testuser','GAME','CREATE',13,'Grand Theft Auto V 增强版',NULL,'{\"id\": 13, \"name\": \"Grand Theft Auto V 增强版\", \"nameEn\": \"Grand Theft Auto V Enhanced\", \"status\": 1, \"summary\": \"一个初涉江湖的街头新丁、一个洗手多年的银行劫匪和一个丧心病狂的杀人狂魔被卷入地下世界、美国政府与娱乐业最为恐怖、最为疯狂的元素之中时，他们必须齐心协力，接连完成一系列的惊天劫案，才能在这个冷血无情的城市中苟延残喘。他们谁也不能相信，尤其是身边的同伙。\\n\\n现在 PC 平台的玩家可以使用一次性迁移将 GTA 5 故事模式进度和 GTA 在线模式角色和进度从传承版的 GTA 5 迁移到 GTA 5 增强版。\", \"coverUrl\": \"https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/3240220/header_schinese.jpg?t=1781187782\", \"createTime\": \"2026-11-23T13:07:47.7144807\", \"gameTypeId\": 2, \"sortWeight\": 0, \"updateTime\": \"2026-11-23T13:07:47.7144807\", \"releaseYear\": 2025}','2026-11-23 13:07:48'),(47,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAM','{\"storeUrl\": \"https://store.steampowered.com/app/947510?cc=cn\"}','{\"storeUrl\": \"https://store.steampowered.com/app/947510?cc=cn\"}','2026-11-23 13:15:02'),(48,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAM','{\"storeUrl\": \"https://store.steampowered.com/app/947510?cc=cn\"}','{\"storeUrl\": \"https://store.steampowered.com/app/947510?cc=cnhttps://store.steampowered.com/app/289070/Sid_Meiers_Civilization_VI/\"}','2026-11-23 13:15:28'),(49,2,'testuser','price','APPROVE_PENDING_PRICE',100,'gameId=9',NULL,'{\"operator\": \"testuser\", \"cnyAmount\": 22.0}','2026-11-23 13:20:11'),(50,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAM','{\"storeUrl\": \"https://store.steampowered.com/app/947510?cc=cnhttps://store.steampowered.com/app/289070/Sid_Meiers_Civilization_VI/\"}','{\"storeUrl\": \"\"}','2026-11-23 13:23:48'),(51,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAM','{\"storeUrl\": \"https://store.steampowered.com/app/947510?cc=cnhttps://store.steampowered.com/app/289070/Sid_Meiers_Civilization_VI/\"}','{\"storeUrl\": \"\"}','2026-11-23 13:24:05'),(52,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAM','{\"storeUrl\": \"https://store.steampowered.com/app/947510?cc=cnhttps://store.steampowered.com/app/289070/Sid_Meiers_Civilization_VI/\"}','{\"storeUrl\": \"\"}','2026-11-23 13:24:08'),(53,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAM','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 13:26:35'),(54,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAM','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 13:26:47'),(55,2,'testuser','price','MANUAL_CREATE_PRICE',101,'gameId=9,platformId=9',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 11.0, \"originalAmount\": 11.0}','2026-11-23 13:31:16'),(56,2,'testuser','price','MANUAL_CREATE_PRICE',102,'gameId=9,platformId=9',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 11.0, \"originalAmount\": 11.0}','2026-11-23 13:32:31'),(57,2,'testuser','platform','DELETE_PLATFORM',1,'STEAM/US','{\"code\": \"STEAM\", \"name\": \"Steam\", \"region\": \"US\", \"remark\": \"币种变更测试\", \"enabled\": 0, \"apiReady\": 1, \"currency\": \"USD\", \"linkTemplate\": \"https://store.steampowered.com/app/{externalId}\", \"dataSourceType\": 1}',NULL,'2026-11-23 13:46:38'),(58,2,'testuser','platform','DELETE_PLATFORM',2,'EPIC/US','{\"code\": \"EPIC\", \"name\": \"Epic Games\", \"region\": \"US\", \"remark\": \"\", \"enabled\": 0, \"apiReady\": 1, \"currency\": \"USD\", \"linkTemplate\": \"https://store.epicgames.com/p/{externalId}\", \"dataSourceType\": 1}',NULL,'2026-11-23 13:46:40'),(59,2,'testuser','platform','DELETE_PLATFORM',3,'XBOX/US','{\"code\": \"XBOX\", \"name\": \"Xbox\", \"region\": \"US\", \"enabled\": 0, \"apiReady\": 1, \"currency\": \"USD\", \"linkTemplate\": \"https://www.xbox.com/games/store/{externalId}\", \"dataSourceType\": 1}',NULL,'2026-11-23 13:46:41'),(60,2,'testuser','platform','DELETE_PLATFORM',4,'STEAM/HK','{\"code\": \"STEAM\", \"name\": \"Steam (HK)\", \"region\": \"HK\", \"enabled\": 0, \"apiReady\": 1, \"currency\": \"HKD\", \"linkTemplate\": \"https://store.steampowered.com/app/{externalId}?cc=hk\", \"dataSourceType\": 1}',NULL,'2026-11-23 13:46:43'),(61,2,'testuser','price','APPROVE_PENDING_PRICE',103,'gameId=9',NULL,'{\"operator\": \"testuser\", \"cnyAmount\": 22.0}','2026-11-23 16:49:43'),(62,2,'testuser','GAME','LINK_UPDATE',13,'game=13 platform=EPIC','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 16:51:38'),(63,2,'testuser','GAME','LINK_UPDATE',12,'game=12 platform=EPIC','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 16:51:44'),(64,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=EPIC','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 16:51:47'),(65,2,'testuser','GAME','LINK_UPDATE',8,'game=8 platform=EPIC','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 16:51:51'),(66,2,'testuser','GAME','LINK_UPDATE',2,'game=2 platform=EPIC','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 16:51:54'),(67,2,'testuser','GAME','LINK_UPDATE',1,'game=1 platform=EPIC','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 16:51:58'),(68,2,'testuser','platform','CREATE_PLATFORM',11,'XIAOHEIHE/CN',NULL,'{\"code\": \"XIAOHEIHE\", \"name\": \"小黑盒\", \"region\": \"CN\", \"remark\": \"\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"CNY\", \"linkTemplate\": \"https://api.xiaoheihe.cn/game/share_game_detail?appid={externalId}&game_type=pc\", \"dataSourceType\": 1}','2026-11-23 16:58:15'),(69,2,'testuser','platform','CREATE_PLATFORM',12,'SHANGUO/CN',NULL,'{\"code\": \"SHANGUO\", \"name\": \"杉果\", \"region\": \"CN\", \"remark\": \"\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"CNY\", \"linkTemplate\": \"https://www.sonkwo.cn/sku/{externalId}\", \"dataSourceType\": 1}','2026-11-23 16:59:08'),(70,2,'testuser','platform','CREATE_PLATFORM',13,'STEAMPY/CN',NULL,'{\"code\": \"STEAMPY\", \"name\": \"steampy\", \"region\": \"CN\", \"remark\": \"\", \"enabled\": 1, \"apiReady\": 1, \"currency\": \"CNY\", \"linkTemplate\": \"https://steampy.com/home?kw={externalId}\", \"dataSourceType\": 1}','2026-11-23 16:59:38'),(71,2,'testuser','GAME','LINK_UPDATE',13,'game=13 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:00:59'),(72,2,'testuser','GAME','LINK_UPDATE',13,'game=13 platform=SHANGUO','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:00:59'),(73,2,'testuser','GAME','LINK_UPDATE',13,'game=13 platform=STEAMPY','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:00'),(74,2,'testuser','GAME','LINK_UPDATE',12,'game=12 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:06'),(75,2,'testuser','GAME','LINK_UPDATE',12,'game=12 platform=SHANGUO','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:07'),(76,2,'testuser','GAME','LINK_UPDATE',12,'game=12 platform=STEAMPY','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:07'),(77,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=STEAMPY','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:11'),(78,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=SHANGUO','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:12'),(79,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:12'),(80,2,'testuser','GAME','LINK_UPDATE',8,'game=8 platform=STEAMPY','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:16'),(81,2,'testuser','GAME','LINK_UPDATE',8,'game=8 platform=SHANGUO','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:17'),(82,2,'testuser','GAME','LINK_UPDATE',8,'game=8 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:17'),(83,2,'testuser','GAME','LINK_UPDATE',2,'game=2 platform=STEAMPY','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:21'),(84,2,'testuser','GAME','LINK_UPDATE',2,'game=2 platform=SHANGUO','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:21'),(85,2,'testuser','GAME','LINK_UPDATE',2,'game=2 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:22'),(86,2,'testuser','GAME','LINK_UPDATE',1,'game=1 platform=STEAMPY','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:26'),(87,2,'testuser','GAME','LINK_UPDATE',1,'game=1 platform=SHANGUO','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:27'),(88,2,'testuser','GAME','LINK_UPDATE',1,'game=1 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-23 17:01:27'),(89,2,'testuser','price','MANUAL_CREATE_PRICE',104,'gameId=9,platformId=10',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 182.0, \"originalAmount\": 182.0}','2026-11-23 17:11:25'),(90,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=EPIC','{\"storeUrl\": \"\"}','{\"storeUrl\": \"https://store.epicgames.com/p/sid-meiers-civilization-vi\"}','2026-11-23 17:12:23'),(91,2,'testuser','price','MANUAL_CREATE_PRICE',105,'gameId=9,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 22.0, \"originalAmount\": 22.0}','2026-11-23 17:46:23'),(92,2,'testuser','GAME','LINK_UPDATE',9,'game=9 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/289070\"}','2026-11-23 17:47:02'),(93,2,'testuser','price','MANUAL_CREATE_PRICE',112,'gameId=9,platformId=10',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 182.0, \"originalAmount\": 182.0}','2026-11-24 14:42:44'),(94,2,'testuser','price','MANUAL_CREATE_PRICE',113,'gameId=9,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 22.0, \"originalAmount\": 22.0}','2026-11-24 14:43:12'),(95,2,'testuser','price','MANUAL_CREATE_PRICE',120,'gameId=9,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 22.0, \"originalAmount\": 22.0}','2026-11-25 09:19:21'),(96,2,'testuser','price','MANUAL_CREATE_PRICE',121,'gameId=9,platformId=10',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 182.0, \"originalAmount\": 182.0}','2026-11-25 09:20:23'),(137,2,'testuser','price','APPROVE_PENDING_PRICE',162,'gameId=1',NULL,'{\"operator\": \"testuser\", \"cnyAmount\": 89.4}','2026-11-27 09:07:55'),(138,2,'testuser','price','MANUAL_CREATE_PRICE',168,'gameId=9,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 22.0, \"originalAmount\": 22.0}','2026-11-27 09:09:05'),(139,2,'testuser','price','MANUAL_CREATE_PRICE',169,'gameId=9,platformId=10',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 182.0, \"originalAmount\": 182.0}','2026-11-27 09:09:53'),(140,2,'testuser','price','MANUAL_CREATE_PRICE',170,'gameId=2,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 264.0, \"originalAmount\": 264.0}','2026-11-27 09:25:36'),(141,2,'testuser','GAME','LINK_UPDATE',2,'game=2 platform=XIAOHEIHE','{\"storeUrl\": \"\"}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/814380\"}','2026-11-27 09:26:26'),(142,2,'testuser','price','MANUAL_CREATE_PRICE',171,'gameId=2,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 129.0, \"originalAmount\": 129.0}','2026-11-27 09:27:16'),(143,2,'testuser','price','MANUAL_CREATE_PRICE',172,'gameId=2,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 268.0, \"originalAmount\": 268.0}','2026-11-27 09:27:47'),(144,2,'testuser','price','MANUAL_CREATE_PRICE',173,'gameId=2,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 129.0, \"originalAmount\": 129.0}','2026-11-27 09:28:24'),(145,2,'testuser','price','MANUAL_CREATE_PRICE',174,'gameId=2,platformId=11',NULL,'{\"currency\": \"CNY\", \"operator\": \"admin\", \"cnyAmount\": 268.0, \"originalAmount\": 268.0}','2026-11-27 09:29:27'),(146,2,'testuser','GAME','CREATE',14,'怪物猎人',NULL,'{\"id\": 14, \"name\": \"怪物猎人\", \"status\": 1, \"createTime\": \"2026-11-27T09:40:27.828701\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T09:40:27.828701\"}','2026-11-27 09:40:28'),(147,2,'testuser','GAME','UPDATE',14,'怪物猎人：世界','{\"id\": 14, \"name\": \"怪物猎人\", \"status\": 1, \"createTime\": \"2026-11-27T09:40:28\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T09:40:28\"}','{\"id\": 14, \"name\": \"怪物猎人：世界\", \"nameEn\": \"Monster Hunter: World\", \"status\": 1, \"summary\": \"新的生命之地。狩猎, 就是本能! 在人气作品「Monster Hunter: World」里, 地形和环境生物, 甚至魔物的生态系统都能被利用, 用一切手段挑战巨大的魔物。在新建构的「Monster Hunter: World」中, 可以体验到你一直期盼的极致猎人生活。\", \"coverUrl\": \"https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/582010/header.jpg?t=1791439347\", \"createTime\": \"2026-11-27T09:40:28\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T09:42:09\", \"releaseYear\": 2018}','2026-11-27 09:42:09'),(148,2,'testuser','GAME','CREATE',15,'怪物猎人：崛起',NULL,'{\"id\": 15, \"name\": \"怪物猎人：崛起\", \"status\": 1, \"createTime\": \"2026-11-27T09:43:46.9468499\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T09:43:46.9468499\"}','2026-11-27 09:43:47'),(149,2,'testuser','GAME','LINK_UPDATE',15,'game=15 platform=STEAM','{\"storeUrl\": \"\"}','{\"storeUrl\": \"\"}','2026-11-27 09:44:03'),(150,2,'testuser','GAME','UPDATE',15,'怪物猎人：崛起','{\"id\": 15, \"name\": \"怪物猎人：崛起\", \"status\": 1, \"createTime\": \"2026-11-27T09:43:47\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T09:43:47\"}','{\"id\": 15, \"name\": \"怪物猎人：崛起\", \"nameEn\": \"MONSTER HUNTER RISE\", \"status\": 1, \"summary\": \"蓬勃跃动的狩猎本能！\\n为狩猎掀起全新风潮、无拘无束尽情跃动的动作；可随心所欲四下驰骋的全新原野；以及能带来未知的兴奋与惊喜的全新怪物。\\n前所未有的狩猎体验正在《MONSTER HUNTER RISE》中等候各位猎人的到来！\", \"coverUrl\": \"https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/1446780/header.jpg?t=1792548454\", \"createTime\": \"2026-11-27T09:43:47\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T09:45:00\", \"releaseYear\": 2022}','2026-11-27 09:45:01'),(155,2,'testuser','GAME','IMPORT_STEAM',18,'Monster Hunter Wilds',NULL,'{\"name\": \"Monster Hunter Wilds\", \"nameEn\": \"Monster Hunter Wilds\", \"gameTypeId\": 1, \"releaseYear\": 2025}','2026-11-27 10:04:43'),(156,2,'testuser','GAME','LINK_UPDATE',18,'game=18 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/2246340?cc=cn\", \"externalId\": \"2246340\"}','2026-11-27 10:04:43'),(157,2,'testuser','GAME','IMPORT_STEAM',19,'Monster Hunter Stories 3: 命运双龙',NULL,'{\"name\": \"Monster Hunter Stories 3: 命运双龙\", \"nameEn\": \"Monster Hunter Stories 3: Twisted Reflection\", \"gameTypeId\": 1, \"releaseYear\": 2026}','2026-11-27 10:04:51'),(158,2,'testuser','GAME','LINK_UPDATE',19,'game=19 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/2852190?cc=cn\", \"externalId\": \"2852190\"}','2026-11-27 10:04:51'),(159,2,'testuser','GAME','IMPORT_STEAM',20,'刺客信条：黑旗 记忆重置',NULL,'{\"name\": \"刺客信条：黑旗 记忆重置\", \"nameEn\": \"Assassin\'s Creed Black Flag Resynced\", \"gameTypeId\": 1, \"releaseYear\": 2026}','2026-11-27 10:05:16'),(160,2,'testuser','GAME','LINK_UPDATE',20,'game=20 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/3751950?cc=cn\", \"externalId\": \"3751950\"}','2026-11-27 10:05:16'),(161,2,'testuser','GAME','IMPORT_STEAM',21,'刺客信条：影',NULL,'{\"name\": \"刺客信条：影\", \"nameEn\": \"Assassin’s Creed Shadows\", \"gameTypeId\": 1, \"releaseYear\": 2025}','2026-11-27 10:05:17'),(162,2,'testuser','GAME','LINK_UPDATE',21,'game=21 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/3159330?cc=cn\", \"externalId\": \"3159330\"}','2026-11-27 10:05:17'),(163,2,'testuser','GAME','IMPORT_STEAM',22,'刺客信条：英灵殿',NULL,'{\"name\": \"刺客信条：英灵殿\", \"nameEn\": \"Assassin\'s Creed Valhalla\", \"gameTypeId\": 1, \"releaseYear\": 2022}','2026-11-27 10:05:19'),(164,2,'testuser','GAME','LINK_UPDATE',22,'game=22 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/2208920?cc=cn\", \"externalId\": \"2208920\"}','2026-11-27 10:05:19'),(165,2,'testuser','GAME','IMPORT_STEAM',23,'刺客信条：起源',NULL,'{\"name\": \"刺客信条：起源\", \"nameEn\": \"Assassin\'s Creed® Origins\", \"gameTypeId\": 1, \"releaseYear\": 2017}','2026-11-27 10:05:22'),(166,2,'testuser','GAME','LINK_UPDATE',23,'game=23 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/582160?cc=cn\", \"externalId\": \"582160\"}','2026-11-27 10:05:22'),(167,2,'testuser','GAME','IMPORT_STEAM',24,'刺客信条：幻景',NULL,'{\"name\": \"刺客信条：幻景\", \"nameEn\": \"Assassin\'s Creed Mirage\", \"gameTypeId\": 1, \"releaseYear\": 2024}','2026-11-27 10:05:24'),(168,2,'testuser','GAME','LINK_UPDATE',24,'game=24 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/3035570?cc=cn\", \"externalId\": \"3035570\"}','2026-11-27 10:05:24'),(169,2,'testuser','GAME','IMPORT_STEAM',25,'刺客信条：大革命',NULL,'{\"name\": \"刺客信条：大革命\", \"nameEn\": \"Assassin\'s Creed® Unity\", \"gameTypeId\": 1, \"releaseYear\": 2014}','2026-11-27 10:05:25'),(170,2,'testuser','GAME','LINK_UPDATE',25,'game=25 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/289650?cc=cn\", \"externalId\": \"289650\"}','2026-11-27 10:05:25'),(171,2,'testuser','GAME','IMPORT_STEAM',26,'HITMAN World of Assassination',NULL,'{\"name\": \"HITMAN World of Assassination\", \"nameEn\": \"HITMAN World of Assassination\", \"gameTypeId\": 1, \"releaseYear\": 2022}','2026-11-27 10:06:05'),(172,2,'testuser','GAME','LINK_UPDATE',26,'game=26 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/1659040?cc=cn\", \"externalId\": \"1659040\"}','2026-11-27 10:06:05'),(173,2,'testuser','GAME','IMPORT_STEAM',27,'Hitman: Absolution™',NULL,'{\"name\": \"Hitman: Absolution™\", \"nameEn\": \"Hitman: Absolution™\", \"gameTypeId\": 1, \"releaseYear\": 2012}','2026-11-27 10:06:06'),(174,2,'testuser','GAME','LINK_UPDATE',27,'game=27 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/203140?cc=cn\", \"externalId\": \"203140\"}','2026-11-27 10:06:06'),(175,2,'testuser','GAME','IMPORT_STEAM',28,'Hitman: Blood Money',NULL,'{\"name\": \"Hitman: Blood Money\", \"nameEn\": \"Hitman: Blood Money\", \"gameTypeId\": 1, \"releaseYear\": 2007}','2026-11-27 10:06:09'),(176,2,'testuser','GAME','LINK_UPDATE',28,'game=28 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/6860?cc=cn\", \"externalId\": \"6860\"}','2026-11-27 10:06:09'),(177,2,'testuser','GAME','IMPORT_STEAM',29,'Hitman: Contracts',NULL,'{\"name\": \"Hitman: Contracts\", \"nameEn\": \"Hitman: Contracts\", \"gameTypeId\": 1, \"releaseYear\": 2014}','2026-11-27 10:06:10'),(178,2,'testuser','GAME','LINK_UPDATE',29,'game=29 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/247430?cc=cn\", \"externalId\": \"247430\"}','2026-11-27 10:06:10'),(179,2,'testuser','GAME','IMPORT_STEAM',30,'Hitman: Codename 47',NULL,'{\"name\": \"Hitman: Codename 47\", \"nameEn\": \"Hitman: Codename 47\", \"gameTypeId\": 1, \"releaseYear\": 2007}','2026-11-27 10:06:11'),(180,2,'testuser','GAME','LINK_UPDATE',30,'game=30 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/6900?cc=cn\", \"externalId\": \"6900\"}','2026-11-27 10:06:11'),(181,2,'testuser','GAME','IMPORT_STEAM',31,'Hitman 2: Silent Assassin',NULL,'{\"name\": \"Hitman 2: Silent Assassin\", \"nameEn\": \"Hitman 2: Silent Assassin\", \"gameTypeId\": 1, \"releaseYear\": 2007}','2026-11-27 10:06:13'),(182,2,'testuser','GAME','LINK_UPDATE',31,'game=31 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/6850?cc=cn\", \"externalId\": \"6850\"}','2026-11-27 10:06:13'),(183,NULL,'system','GAME','LINK_DELETE',2,'只狼：影逝二度',NULL,NULL,'2026-11-27 11:22:18'),(184,NULL,'system','GAME','LINK_DELETE',8,'艾尔登法环',NULL,NULL,'2026-11-27 11:22:18'),(185,5,'xym009621','GAME','IMPORT_STEAM',32,'控制 - 终极合辑 / Control Ultimate Edition',NULL,'{\"name\": \"控制 - 终极合辑 / Control Ultimate Edition\", \"nameEn\": \"CONTROL Ultimate Edition\", \"gameTypeId\": 1, \"releaseYear\": 2020}','2026-11-27 11:34:39'),(186,5,'xym009621','GAME','LINK_UPDATE',32,'game=32 platform=STEAM/CN','{}','{\"storeUrl\": \"https://store.steampowered.com/app/870780?cc=cn\", \"externalId\": \"870780\"}','2026-11-27 11:34:39'),(187,5,'xym009621','GAME','META_UPDATE',1,'game=1 steamAppId=1091500','{\"name\": \"赛博朋克2077\", \"nameEn\": \"Cyberpunk 2077\"}','{\"name\": \"赛博朋克2077\", \"nameEn\": \"Cyberpunk 2077\", \"releaseDate\": \"2020-12-09\", \"releaseYear\": 2020}','2026-11-27 11:51:33'),(188,5,'xym009621','GAME','META_UPDATE',2,'game=2 steamAppId=814380','{\"name\": \"只狼：影逝二度\", \"nameEn\": \"Sekiro: Shadows Die Twice\"}','{\"name\": \"只狼：影逝二度\", \"nameEn\": \"Sekiro: Shadows Die Twice\", \"releaseDate\": \"2019-03-21\", \"releaseYear\": 2019}','2026-11-27 11:51:33'),(189,5,'xym009621','GAME','META_UPDATE',8,'game=8 steamAppId=1245620','{\"name\": \"艾尔登法环\", \"nameEn\": \"ELDEN RING\"}','{\"name\": \"艾尔登法环\", \"nameEn\": \"ELDEN RING\", \"releaseDate\": \"2022-02-24\", \"releaseYear\": 2022}','2026-11-27 11:51:34'),(190,5,'xym009621','GAME','META_UPDATE',9,'game=9 steamAppId=289070','{\"name\": \"文明六\", \"nameEn\": \"Sid Meier\'s Civilization VI\"}','{\"name\": \"文明六\", \"nameEn\": \"Sid Meier\'s Civilization VI\", \"releaseDate\": \"2016-10-20\", \"releaseYear\": 2016}','2026-11-27 11:51:35'),(191,5,'xym009621','GAME','META_UPDATE',12,'game=12 steamAppId=1426210','{\"name\": \"双人成行\", \"nameEn\": \"It Takes Two - Friend\'s Pass\", \"releaseYear\": 2021}','{\"name\": \"双人成行\", \"nameEn\": \"It Takes Two - Friend\'s Pass\", \"releaseDate\": \"2021-03-25\", \"releaseYear\": 2021}','2026-11-27 11:51:36'),(192,5,'xym009621','GAME','META_UPDATE',13,'game=13 steamAppId=3240220','{\"name\": \"Grand Theft Auto V 增强版\", \"nameEn\": \"Grand Theft Auto V Enhanced\", \"releaseYear\": 2025}','{\"name\": \"Grand Theft Auto V 增强版\", \"nameEn\": \"Grand Theft Auto V Enhanced\", \"releaseDate\": \"2025-03-04\", \"releaseYear\": 2025}','2026-11-27 11:51:36'),(193,5,'xym009621','GAME','META_UPDATE',14,'game=14 steamAppId=582010','{\"name\": \"怪物猎人：世界\", \"nameEn\": \"Monster Hunter: World\", \"releaseYear\": 2018}','{\"name\": \"怪物猎人：世界\", \"nameEn\": \"Monster Hunter: World\", \"releaseDate\": \"2018-08-08\", \"releaseYear\": 2018}','2026-11-27 11:51:37'),(194,5,'xym009621','GAME','META_UPDATE',15,'game=15 steamAppId=1446780','{\"name\": \"怪物猎人：崛起\", \"nameEn\": \"MONSTER HUNTER RISE\", \"releaseYear\": 2022}','{\"name\": \"怪物猎人：崛起\", \"nameEn\": \"MONSTER HUNTER RISE\", \"releaseDate\": \"2022-01-12\", \"releaseYear\": 2022}','2026-11-27 11:51:38'),(195,5,'xym009621','GAME','META_UPDATE',18,'game=18 steamAppId=2246340','{\"name\": \"Monster Hunter Wilds\", \"nameEn\": \"Monster Hunter Wilds\", \"releaseYear\": 2025}','{\"name\": \"Monster Hunter Wilds\", \"nameEn\": \"Monster Hunter Wilds\", \"releaseDate\": \"2025-02-27\", \"releaseYear\": 2025}','2026-11-27 11:51:39'),(196,5,'xym009621','GAME','META_UPDATE',19,'game=19 steamAppId=2852190','{\"name\": \"Monster Hunter Stories 3: 命运双龙\", \"nameEn\": \"Monster Hunter Stories 3: Twisted Reflection\", \"releaseYear\": 2026}','{\"name\": \"Monster Hunter Stories 3: 命运双龙\", \"nameEn\": \"Monster Hunter Stories 3: Twisted Reflection\", \"releaseDate\": \"2026-03-12\", \"releaseYear\": 2026}','2026-11-27 11:51:40'),(197,5,'xym009621','GAME','META_UPDATE',20,'game=20 steamAppId=3751950','{\"name\": \"刺客信条：黑旗 记忆重置\", \"nameEn\": \"Assassin\'s Creed Black Flag Resynced\", \"releaseYear\": 2026}','{\"name\": \"刺客信条：黑旗 记忆重置\", \"nameEn\": \"Assassin\'s Creed Black Flag Resynced\", \"releaseDate\": \"2026-07-09\", \"releaseYear\": 2026}','2026-11-27 11:51:41'),(198,5,'xym009621','GAME','META_UPDATE',21,'game=21 steamAppId=3159330','{\"name\": \"刺客信条：影\", \"nameEn\": \"Assassin’s Creed Shadows\", \"releaseYear\": 2025}','{\"name\": \"刺客信条：影\", \"nameEn\": \"Assassin’s Creed Shadows\", \"releaseDate\": \"2025-03-19\", \"releaseYear\": 2025}','2026-11-27 11:51:42'),(199,5,'xym009621','GAME','META_UPDATE',22,'game=22 steamAppId=2208920','{\"name\": \"刺客信条：英灵殿\", \"nameEn\": \"Assassin\'s Creed Valhalla\", \"releaseYear\": 2022}','{\"name\": \"刺客信条：英灵殿\", \"nameEn\": \"Assassin\'s Creed Valhalla\", \"releaseDate\": \"2022-12-06\", \"releaseYear\": 2022}','2026-11-27 11:51:43'),(200,5,'xym009621','GAME','META_UPDATE',23,'game=23 steamAppId=582160','{\"name\": \"刺客信条：起源\", \"nameEn\": \"Assassin\'s Creed® Origins\", \"releaseYear\": 2017}','{\"name\": \"刺客信条：起源\", \"nameEn\": \"Assassin\'s Creed® Origins\", \"releaseDate\": \"2017-10-26\", \"releaseYear\": 2017}','2026-11-27 11:51:43'),(201,5,'xym009621','GAME','META_UPDATE',24,'game=24 steamAppId=3035570','{\"name\": \"刺客信条：幻景\", \"nameEn\": \"Assassin\'s Creed Mirage\", \"releaseYear\": 2024}','{\"name\": \"刺客信条：幻景\", \"nameEn\": \"Assassin\'s Creed Mirage\", \"releaseDate\": \"2024-10-17\", \"releaseYear\": 2024}','2026-11-27 11:51:44'),(202,5,'xym009621','GAME','META_UPDATE',25,'game=25 steamAppId=289650','{\"name\": \"刺客信条：大革命\", \"nameEn\": \"Assassin\'s Creed® Unity\", \"releaseYear\": 2014}','{\"name\": \"刺客信条：大革命\", \"nameEn\": \"Assassin\'s Creed® Unity\", \"releaseDate\": \"2014-11-12\", \"releaseYear\": 2014}','2026-11-27 11:51:45'),(203,5,'xym009621','GAME','META_UPDATE',26,'game=26 steamAppId=1659040','{\"name\": \"HITMAN World of Assassination\", \"nameEn\": \"HITMAN World of Assassination\", \"releaseYear\": 2022}','{\"name\": \"HITMAN World of Assassination\", \"nameEn\": \"HITMAN World of Assassination\", \"releaseDate\": \"2022-01-20\", \"releaseYear\": 2022}','2026-11-27 11:51:46'),(204,5,'xym009621','GAME','META_UPDATE',27,'game=27 steamAppId=203140','{\"name\": \"Hitman: Absolution™\", \"nameEn\": \"Hitman: Absolution™\", \"releaseYear\": 2012}','{\"name\": \"Hitman: Absolution™\", \"nameEn\": \"Hitman: Absolution™\", \"releaseDate\": \"2012-11-19\", \"releaseYear\": 2012}','2026-11-27 11:51:46'),(205,5,'xym009621','GAME','META_UPDATE',28,'game=28 steamAppId=6860','{\"name\": \"Hitman: Blood Money\", \"nameEn\": \"Hitman: Blood Money\", \"releaseYear\": 2007}','{\"name\": \"Hitman: Blood Money\", \"nameEn\": \"Hitman: Blood Money\", \"releaseDate\": \"2007-03-15\", \"releaseYear\": 2007}','2026-11-27 11:51:47'),(206,5,'xym009621','GAME','META_UPDATE',29,'game=29 steamAppId=247430','{\"name\": \"Hitman: Contracts\", \"nameEn\": \"Hitman: Contracts\", \"releaseYear\": 2014}','{\"name\": \"Hitman: Contracts\", \"nameEn\": \"Hitman: Contracts\", \"releaseDate\": \"2014-01-21\", \"releaseYear\": 2014}','2026-11-27 11:51:48'),(207,5,'xym009621','GAME','META_UPDATE',30,'game=30 steamAppId=6900','{\"name\": \"Hitman: Codename 47\", \"nameEn\": \"Hitman: Codename 47\", \"releaseYear\": 2007}','{\"name\": \"Hitman: Codename 47\", \"nameEn\": \"Hitman: Codename 47\", \"releaseDate\": \"2007-03-15\", \"releaseYear\": 2007}','2026-11-27 11:51:49'),(208,5,'xym009621','GAME','META_UPDATE',31,'game=31 steamAppId=6850','{\"name\": \"Hitman 2: Silent Assassin\", \"nameEn\": \"Hitman 2: Silent Assassin\", \"releaseYear\": 2007}','{\"name\": \"Hitman 2: Silent Assassin\", \"nameEn\": \"Hitman 2: Silent Assassin\", \"releaseDate\": \"2007-03-15\", \"releaseYear\": 2007}','2026-11-27 11:51:49'),(209,NULL,'system','GAME','META_UPDATE',13,'game=13 steamAppId=3240220','{\"name\": \"Grand Theft Auto V 增强版\", \"nameEn\": \"Grand Theft Auto V Enhanced\", \"releaseYear\": 2025}','{\"name\": \"Grand Theft Auto V 增强版\", \"nameEn\": \"Grand Theft Auto V Enhanced\", \"releaseDate\": \"2025-03-04\", \"releaseYear\": 2025}','2026-11-27 11:57:09'),(210,5,'xym009621','GAME','LINK_UPDATE',14,'game=14 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/582010\", \"externalId\": \"582010\"}','2026-11-27 12:26:34'),(211,5,'xym009621','GAME','LINK_UPDATE',15,'game=15 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/1446780\", \"externalId\": \"1446780\"}','2026-11-27 12:26:34'),(212,5,'xym009621','GAME','LINK_UPDATE',18,'game=18 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/2246340\", \"externalId\": \"2246340\"}','2026-11-27 12:26:34'),(213,5,'xym009621','GAME','LINK_UPDATE',19,'game=19 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/2852190\", \"externalId\": \"2852190\"}','2026-11-27 12:26:34'),(214,5,'xym009621','GAME','LINK_UPDATE',20,'game=20 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/3751950\", \"externalId\": \"3751950\"}','2026-11-27 12:26:34'),(215,5,'xym009621','GAME','LINK_UPDATE',21,'game=21 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/3159330\", \"externalId\": \"3159330\"}','2026-11-27 12:26:34'),(216,5,'xym009621','GAME','LINK_UPDATE',22,'game=22 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/2208920\", \"externalId\": \"2208920\"}','2026-11-27 12:26:34'),(217,5,'xym009621','GAME','LINK_UPDATE',23,'game=23 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/582160\", \"externalId\": \"582160\"}','2026-11-27 12:26:34'),(218,5,'xym009621','GAME','LINK_UPDATE',24,'game=24 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/3035570\", \"externalId\": \"3035570\"}','2026-11-27 12:26:34'),(219,5,'xym009621','GAME','LINK_UPDATE',25,'game=25 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/289650\", \"externalId\": \"289650\"}','2026-11-27 12:26:34'),(220,5,'xym009621','GAME','LINK_UPDATE',26,'game=26 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/1659040\", \"externalId\": \"1659040\"}','2026-11-27 12:26:34'),(221,5,'xym009621','GAME','LINK_UPDATE',27,'game=27 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/203140\", \"externalId\": \"203140\"}','2026-11-27 12:26:34'),(222,5,'xym009621','GAME','LINK_UPDATE',28,'game=28 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/6860\", \"externalId\": \"6860\"}','2026-11-27 12:26:34'),(223,5,'xym009621','GAME','LINK_UPDATE',29,'game=29 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/247430\", \"externalId\": \"247430\"}','2026-11-27 12:26:34'),(224,5,'xym009621','GAME','LINK_UPDATE',30,'game=30 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/6900\", \"externalId\": \"6900\"}','2026-11-27 12:26:34'),(225,5,'xym009621','GAME','LINK_UPDATE',31,'game=31 platform=XIAOHEIHE/CN','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/6850\", \"externalId\": \"6850\"}','2026-11-27 12:26:34'),(226,5,'xym009621','GAME','LINK_UPDATE',1,'game=1 platform=XIAOHEIHE/CN（修复空链接）','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/1091500\", \"externalId\": \"1091500\"}','2026-11-27 12:28:07'),(227,5,'xym009621','GAME','LINK_UPDATE',2,'game=2 platform=XIAOHEIHE/CN（修复空链接）','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/814380\"}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/814380\", \"externalId\": \"814380\"}','2026-11-27 12:28:07'),(228,5,'xym009621','GAME','LINK_UPDATE',8,'game=8 platform=XIAOHEIHE/CN（修复空链接）','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/1245620\", \"externalId\": \"1245620\"}','2026-11-27 12:28:08'),(229,5,'xym009621','GAME','LINK_UPDATE',9,'game=9 platform=XIAOHEIHE/CN（修复空链接）','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/289070\"}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/289070\", \"externalId\": \"289070\"}','2026-11-27 12:28:08'),(230,5,'xym009621','GAME','LINK_UPDATE',12,'game=12 platform=XIAOHEIHE/CN（修复空链接）','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/1426210\", \"externalId\": \"1426210\"}','2026-11-27 12:28:08'),(231,5,'xym009621','GAME','LINK_UPDATE',13,'game=13 platform=XIAOHEIHE/CN（修复空链接）','{}','{\"storeUrl\": \"https://www.xiaoheihe.cn/app/topic/game/pc/3240220\", \"externalId\": \"3240220\"}','2026-11-27 12:28:08'),(232,5,'xym009621','GAME_TYPE','CREATE',5,'冒险',NULL,'{\"id\": 5, \"code\": \"ADVENTURE\", \"name\": \"冒险\", \"sortNo\": 4, \"createTime\": \"2026-11-27T14:15:02.070426\", \"updateTime\": \"2026-11-27T14:15:02.070426\"}','2026-11-27 14:15:02'),(233,5,'xym009621','GAME_TYPE','CREATE',6,'开放世界',NULL,'{\"id\": 6, \"code\": \"OPEN_WORLD\", \"name\": \"开放世界\", \"sortNo\": 5, \"createTime\": \"2026-11-27T14:15:02.1518056\", \"updateTime\": \"2026-11-27T14:15:02.1518056\"}','2026-11-27 14:15:02'),(234,2,'testuser','GAME','UPDATE',31,'Hitman 2: Silent Assassin','{\"id\": 31, \"name\": \"Hitman 2: Silent Assassin\", \"nameEn\": \"Hitman 2: Silent Assassin\", \"status\": 1, \"summary\": \"Enter the realm of a retired assassin, forced back into action by treason. You may be a hired killer but you still have a sense of loyalty and justice. Visit the dark recesses of a world corrupted by crime, greed, degradation and dishonor. And a past that catches up with you.\", \"coverUrl\": \"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/6850/header.jpg?t=1732293077\", \"createTime\": \"2026-11-27T10:06:13\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T10:06:13\", \"releaseDate\": \"2007-03-15\", \"releaseYear\": 2007}','{\"id\": 31, \"name\": \"Hitman 2: Silent Assassin\", \"nameEn\": \"Hitman 2: Silent Assassin\", \"status\": 1, \"summary\": \"Enter the realm of a retired assassin, forced back into action by treason. You may be a hired killer but you still have a sense of loyalty and justice. Visit the dark recesses of a world corrupted by crime, greed, degradation and dishonor. And a past that catches up with you.\", \"coverUrl\": \"https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/6850/header.jpg?t=1732293077\", \"createTime\": \"2026-11-27T10:06:13\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T10:06:13\", \"releaseDate\": \"2007-03-15\", \"releaseYear\": 2007}','2026-11-27 14:15:30'),(235,5,'xym009621','GAME','UPDATE',1,'赛博朋克2077','{\"id\": 1, \"name\": \"赛博朋克2077\", \"nameEn\": \"Cyberpunk 2077\", \"status\": 1, \"coverUrl\": \"https://cdn.akamai.steamstatic.com/steam/apps/1091500/header.jpg\", \"createTime\": \"2026-11-19T17:38:09\", \"gameTypeId\": 2, \"sortWeight\": 0, \"updateTime\": \"2026-11-21T20:21:57\", \"releaseDate\": \"2020-12-09\", \"releaseYear\": 2020}','{\"id\": 1, \"name\": \"赛博朋克2077\", \"nameEn\": \"Cyberpunk 2077\", \"status\": 1, \"coverUrl\": \"https://cdn.akamai.steamstatic.com/steam/apps/1091500/header.jpg\", \"createTime\": \"2026-11-19T17:38:09\", \"gameTypeId\": 1, \"sortWeight\": 0, \"updateTime\": \"2026-11-27T14:15:50\", \"releaseDate\": \"2020-12-09\", \"releaseYear\": 2020}','2026-11-27 14:15:51');
/*!40000 ALTER TABLE `admin_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `alert_rule`
--

DROP TABLE IF EXISTS `alert_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alert_rule` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '逻辑关联 sys_user.id',
  `game_id` bigint unsigned NOT NULL COMMENT '逻辑关联 game.id',
  `platform_id` bigint unsigned DEFAULT NULL COMMENT 'NULL 表示全平台任一',
  `type` tinyint NOT NULL COMMENT '1目标价 2跌至史低 3任意降价（AC-10.1）',
  `target_price` decimal(10,2) DEFAULT NULL COMMENT '目标价，type=1 必填（AC-10.1 非法值拒绝）',
  `baseline_price` decimal(10,2) NOT NULL COMMENT '基准价=设置时最低人民币价（D8/AC-10.7）',
  `baseline_time` datetime NOT NULL COMMENT '基准取值时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1启用 0暂停（AC-10.5）',
  `last_fired_price` decimal(10,2) DEFAULT NULL COMMENT '上次触发价（AC-10.3 去重锚点）',
  `last_fired_time` datetime DEFAULT NULL COMMENT '上次触发时间（AC-10.3）',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_game_type` (`game_id`,`type`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='降价提醒规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alert_rule`
--

LOCK TABLES `alert_rule` WRITE;
/*!40000 ALTER TABLE `alert_rule` DISABLE KEYS */;
INSERT INTO `alert_rule` VALUES (4,5,1,1,1,70.00,143.93,'2026-11-23 09:19:46',1,61.92,'2026-11-23 09:33:16',0,'2026-11-23 09:19:46','2026-11-23 09:33:36'),(5,2,2,11,1,134.00,268.00,'2026-11-27 09:28:00',1,129.00,'2026-11-27 09:28:51',1,'2026-11-27 09:28:00','2026-11-27 10:21:16');
/*!40000 ALTER TABLE `alert_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collect_task_log`
--

DROP TABLE IF EXISTS `collect_task_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `collect_task_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `platform_id` bigint unsigned NOT NULL COMMENT '采集平台',
  `batch_no` varchar(64) NOT NULL COMMENT '批次号（AC-12.5 关联 price_record.source_ref）',
  `task_type` tinyint NOT NULL DEFAULT '1' COMMENT '1全量 2增量',
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `total` int NOT NULL DEFAULT '0' COMMENT '处理条数',
  `success_count` int NOT NULL DEFAULT '0',
  `fail_count` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0运行中 1成功 2部分失败 3失败',
  `error_msg` varchar(1000) DEFAULT NULL COMMENT '失败原因（AC-12.3 告警）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch` (`batch_no`),
  KEY `idx_platform_time` (`platform_id`,`start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采集任务日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collect_task_log`
--

LOCK TABLES `collect_task_log` WRITE;
/*!40000 ALTER TABLE `collect_task_log` DISABLE KEYS */;
INSERT INTO `collect_task_log` VALUES (14,1,'STEAM-US-20261123092043-3064',1,'2026-11-23 09:20:44','2026-11-23 09:20:44',4,2,0,1,NULL,'2026-11-23 09:20:44'),(15,4,'STEAM-HK-20261123092052-6999',1,'2026-11-23 09:20:52','2026-11-23 09:20:52',1,1,0,1,NULL,'2026-11-23 09:20:52'),(16,3,'XBOX-US-20261123092101-4164',1,'2026-11-23 09:21:02','2026-11-23 09:21:02',0,0,0,1,NULL,'2026-11-23 09:21:02'),(17,1,'STEAM-US-20261123092439-7011',1,'2026-11-23 09:24:39','2026-11-23 09:24:39',4,4,0,1,NULL,'2026-11-23 09:24:39'),(18,1,'STEAM-US-20261123092503-8661',1,'2026-11-23 09:25:04','2026-11-23 09:25:04',4,4,0,1,NULL,'2026-11-23 09:25:04'),(19,1,'STEAM-US-20261123092521-4029',1,'2026-11-23 09:25:21','2026-11-23 09:25:21',4,0,0,1,NULL,'2026-11-23 09:25:21'),(20,1,'STEAM-US-20261123092527-7367',1,'2026-11-23 09:25:28','2026-11-23 09:25:28',0,0,0,1,NULL,'2026-11-23 09:25:28'),(21,1,'STEAM-US-20261123092531-9796',1,'2026-11-23 09:25:31','2026-11-23 09:25:31',0,0,0,3,'模拟解析失败：上游返回内容非 JSON（<html>403 Forbidden</html>）','2026-11-23 09:25:31'),(22,1,'STEAM-US-20261123092538-7653',1,'2026-11-23 09:25:39','2026-11-23 09:25:39',4,4,0,1,NULL,'2026-11-23 09:25:39'),(23,1,'STEAM-US-20261123092540-4313',1,'2026-11-23 09:25:40','2026-11-23 09:25:40',4,4,0,1,NULL,'2026-11-23 09:25:40'),(24,1,'STEAM-US-20261123092540-9399',1,'2026-11-23 09:25:41','2026-11-23 09:25:41',4,4,0,1,NULL,'2026-11-23 09:25:41'),(25,1,'STEAM-US-20261123092542-3111',1,'2026-11-23 09:25:43','2026-11-23 09:25:43',4,4,0,1,NULL,'2026-11-23 09:25:43'),(26,1,'STEAM-US-20261123092636-1359',1,'2026-11-23 09:26:36','2026-11-23 09:26:36',4,4,0,1,NULL,'2026-11-23 09:26:36'),(27,1,'STEAM-US-20261123093212-7654',1,'2026-11-23 09:32:12','2026-11-23 09:32:12',4,0,0,1,NULL,'2026-11-23 09:32:12'),(28,1,'STEAM-US-20261123093244-4594',1,'2026-11-23 09:32:45','2026-11-23 09:32:45',4,4,0,1,NULL,'2026-11-23 09:32:45'),(29,1,'STEAM-US-20261123095910-5730',1,'2026-11-23 09:59:10','2026-11-23 09:59:10',4,0,0,1,NULL,'2026-11-23 09:59:10'),(30,1,'STEAM-US-20261123100336-8200',1,'2026-11-23 10:03:36','2026-11-23 10:03:36',4,0,0,1,NULL,'2026-11-23 10:03:36'),(31,1,'STEAM-US-20261123100343-8224',1,'2026-11-23 10:03:43','2026-11-23 10:03:43',0,0,0,3,'模拟解析失败：上游返回内容非 JSON（<html>403 Forbidden</html>）','2026-11-23 10:03:43'),(32,1,'STEAM-US-20261123100349-5442',1,'2026-11-23 10:03:49','2026-11-23 10:03:49',4,0,0,1,NULL,'2026-11-23 10:03:49'),(33,1,'STEAM-US-20261123100354-8638',1,'2026-11-23 10:03:54','2026-11-23 10:03:54',0,0,0,1,NULL,'2026-11-23 10:03:54'),(34,1,'STEAM-US-20261123100359-9711',1,'2026-11-23 10:04:00','2026-11-23 10:04:00',4,4,0,1,NULL,'2026-11-23 10:04:00'),(35,1,'STEAM-US-20261123110101-3567',1,'2026-11-23 11:01:01','2026-11-23 11:01:01',4,0,0,1,NULL,'2026-11-23 11:01:01'),(36,1,'STEAM-US-20261123110104-8242',1,'2026-11-23 11:01:05','2026-11-23 11:01:05',4,4,0,1,NULL,'2026-11-23 11:01:05'),(37,4,'STEAM-HK-20261123110146-3978',1,'2026-11-23 11:01:46','2026-11-23 11:01:46',1,0,0,1,NULL,'2026-11-23 11:01:46'),(38,3,'XBOX-US-20261123110151-9695',1,'2026-11-23 11:01:51','2026-11-23 11:01:51',0,0,0,1,NULL,'2026-11-23 11:01:51'),(39,1,'STEAM-US-20261123110155-5803',1,'2026-11-23 11:01:55','2026-11-23 11:01:56',4,0,0,1,NULL,'2026-11-23 11:01:55'),(40,1,'STEAM-US-20261123114459-4753',1,'2026-11-23 11:45:00','2026-11-23 11:45:00',4,0,0,1,NULL,'2026-11-23 11:45:00'),(41,1,'STEAM-US-20261123114546-2541',1,'2026-11-23 11:45:46','2026-11-23 11:45:46',4,0,0,1,NULL,'2026-11-23 11:45:46'),(42,1,'STEAM-US-20261123115209-5897',1,'2026-11-23 11:52:09','2026-11-23 11:52:10',0,0,0,1,NULL,'2026-11-23 11:52:09'),(43,1,'STEAM-US-20261123115239-9963',1,'2026-11-23 11:52:40','2026-11-23 11:52:40',0,0,0,1,NULL,'2026-11-23 11:52:40'),(44,1,'STEAM-US-20261123122503-2288',1,'2026-11-23 12:25:04','2026-11-23 12:25:07',4,1,0,1,NULL,'2026-11-23 12:25:04'),(45,4,'STEAM-HK-20261123122640-6763',1,'2026-11-23 12:26:41','2026-11-23 12:26:42',1,0,0,1,NULL,'2026-11-23 12:26:41'),(46,9,'STEAM-CN-20261123124928-3451',1,'2026-11-23 12:49:28','2026-11-23 12:49:28',0,0,0,1,NULL,'2026-11-23 12:49:28'),(47,9,'STEAM-CN-20261123130151-1986',1,'2026-11-23 13:01:52','2026-11-23 13:01:59',5,5,0,1,NULL,'2026-11-23 13:01:52'),(48,10,'EPIC-CN-20261123130230-6704',1,'2026-11-23 13:02:31','2026-11-23 13:02:31',0,0,0,3,'本批次未取到任何价格（检查网络/证书/币种，详见日志）','2026-11-23 13:02:31'),(49,9,'STEAM-CN-20261123130808-1795',1,'2026-11-23 13:08:09','2026-11-23 13:08:16',6,1,0,1,NULL,'2026-11-23 13:08:09'),(50,9,'STEAM-CN-20261123131740-7039',1,'2026-11-23 13:17:41','2026-11-23 13:17:48',6,0,0,1,NULL,'2026-11-23 13:17:41'),(51,9,'STEAM-CN-20261123164926-5180',1,'2026-11-23 16:49:26','2026-11-23 16:49:31',6,0,0,1,NULL,'2026-11-23 16:49:26'),(52,10,'EPIC-CN-20261123170142-8333',1,'2026-11-23 17:01:43','2026-11-23 17:01:43',0,0,0,3,'本批次未取到任何价格（检查网络/证书/币种，详见日志）','2026-11-23 17:01:43'),(53,9,'STEAM-CN-20261123170215-9835',1,'2026-11-23 17:02:16','2026-11-23 17:02:19',6,0,0,1,NULL,'2026-11-23 17:02:16'),(54,10,'EPIC-CN-20261123170231-5077',1,'2026-11-23 17:02:31','2026-11-23 17:02:31',0,0,0,3,'本批次未取到任何价格（检查网络/证书/币种，详见日志）','2026-11-23 17:02:31'),(55,11,'XIAOHEIHE-CN-20261123171028-3281',1,'2026-11-23 17:10:29','2026-11-23 17:10:29',0,0,0,3,'该平台暂无可用采集器（当前已接入：STEAM,EPIC,XBOX）','2026-11-23 17:10:29'),(56,11,'XIAOHEIHE-CN-20261123171032-5214',1,'2026-11-23 17:10:33','2026-11-23 17:10:33',0,0,0,3,'该平台暂无可用采集器（当前已接入：STEAM,EPIC,XBOX）','2026-11-23 17:10:33'),(57,11,'XIAOHEIHE-CN-20261123174726-6161',1,'2026-11-23 17:47:27','2026-11-23 17:47:27',0,0,0,3,'该平台暂无可用采集器（当前已接入：STEAM,EPIC,XBOX）','2026-11-23 17:47:27'),(58,9,'STEAM-CN-20261124040000-5576',1,'2026-11-24 04:00:00','2026-11-24 04:00:04',6,1,0,1,NULL,'2026-11-24 04:00:00'),(59,10,'EPIC-CN-20261124040003-7804',1,'2026-11-24 04:00:04','2026-11-24 04:00:04',0,0,0,3,'本批次未取到任何价格（检查网络/证书/币种，详见日志）','2026-11-24 04:00:04'),(60,11,'XIAOHEIHE-CN-20261124040003-9771',1,'2026-11-24 04:00:04','2026-11-24 04:00:04',0,0,0,3,'该平台暂无可用采集器（当前已接入：STEAM,EPIC,XBOX）','2026-11-24 04:00:04'),(61,12,'SHANGUO-CN-20261124040003-5871',1,'2026-11-24 04:00:04','2026-11-24 04:00:04',0,0,0,3,'该平台暂无可用采集器（当前已接入：STEAM,EPIC,XBOX）','2026-11-24 04:00:04'),(62,13,'STEAMPY-CN-20261124040003-6004',1,'2026-11-24 04:00:04','2026-11-24 04:00:04',0,0,0,3,'该平台暂无可用采集器（当前已接入：STEAM,EPIC,XBOX）','2026-11-24 04:00:04'),(63,9,'STEAM-CN-20261124142926-7478',1,'2026-11-24 14:29:27','2026-11-24 14:29:34',6,0,0,1,NULL,'2026-11-24 14:29:27'),(64,10,'EPIC-CN-20261124143107-6282',1,'2026-11-24 14:31:07','2026-11-24 14:31:07',0,0,0,3,'本批次未取到任何价格（检查网络/证书/币种，详见日志）','2026-11-24 14:31:07'),(65,9,'STEAM-CN-20261124143112-7280',1,'2026-11-24 14:31:12','2026-11-24 14:31:17',6,0,0,1,NULL,'2026-11-24 14:31:12'),(66,9,'STEAM-CN-20261124143844-5035',1,'2026-11-24 14:38:44','2026-11-24 14:38:49',6,5,0,1,NULL,'2026-11-24 14:38:44'),(67,9,'STEAM-CN-20261124155453-8987',1,'2026-11-24 15:54:54','2026-11-24 15:54:59',6,0,0,1,NULL,'2026-11-24 15:54:54'),(68,9,'STEAM-CN-20261125091811-5981',1,'2026-11-25 09:18:11','2026-11-25 09:18:16',6,6,0,1,NULL,'2026-11-25 09:18:11'),(69,9,'STEAM-CN-20261127090727-8651',1,'2026-11-27 09:07:28','2026-11-27 09:07:32',6,5,0,1,NULL,'2026-11-27 09:07:28'),(70,9,'STEAM-CN-20261127094038-2808',1,'2026-11-27 09:40:38','2026-11-27 09:40:42',6,0,0,1,NULL,'2026-11-27 09:40:38'),(71,9,'STEAM-CN-20261127094252-5966',1,'2026-11-27 09:42:52','2026-11-27 09:42:56',7,1,0,1,NULL,'2026-11-27 09:42:52'),(72,9,'STEAM-CN-20261127094409-6758',1,'2026-11-27 09:44:10','2026-11-27 09:44:14',8,1,0,1,NULL,'2026-11-27 09:44:10'),(73,9,'STEAM-CN-20261127100633-9390',1,'2026-11-27 10:06:34','2026-11-27 10:06:44',22,14,0,1,NULL,'2026-11-27 10:06:34'),(74,9,'STEAM-CN-20261127100647-2486',1,'2026-11-27 10:06:47','2026-11-27 10:06:58',22,0,0,1,NULL,'2026-11-27 10:06:47'),(75,9,'STEAM-CN-20261127100807-7268',1,'2026-11-27 10:08:07','2026-11-27 10:08:17',22,0,0,1,NULL,'2026-11-27 10:08:07'),(76,9,'STEAM-CN-20261127101124-9339',1,'2026-11-27 10:11:24','2026-11-27 10:11:35',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 10:11:24'),(77,9,'STEAM-CN-20261127101344-3780',1,'2026-11-27 10:13:45','2026-11-27 10:13:55',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 10:13:45'),(78,10,'EPIC-CN-20261127110705-3731',1,'2026-11-27 11:07:05','2026-11-27 11:07:05',0,0,0,3,'本批次未取到任何价格（检查网络/证书/币种，详见日志）','2026-11-27 11:07:05'),(79,10,'EPIC-CN-20261127111402-6350',1,'2026-11-27 11:14:02','2026-11-27 11:15:41',0,0,0,3,'本批次未取到任何价格（检查网络/证书/币种，详见日志）','2026-11-27 11:14:02'),(80,10,'EPIC-CN-20261127111741-5111',1,'2026-11-27 11:17:41','2026-11-27 11:17:54',4,3,0,1,'其中 1 条价格无变化已跳过（同日去重）','2026-11-27 11:17:41'),(81,10,'EPIC-CN-20261127111944-7917',1,'2026-11-27 11:19:44','2026-11-27 11:19:59',4,0,0,1,'本次未新增记录：4 条今日已有同价记录（同日去重），价格无变化','2026-11-27 11:19:44'),(82,9,'STEAM-CN-20261127111959-3421',1,'2026-11-27 11:19:59','2026-11-27 11:20:13',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 11:19:59'),(83,10,'EPIC-CN-20261127112155-8627',1,'2026-11-27 11:21:55','2026-11-27 11:22:06',4,0,0,1,'本次未新增记录：4 条今日已有同价记录（同日去重），价格无变化','2026-11-27 11:21:55'),(84,10,'EPIC-CN-20261127112421-5311',1,'2026-11-27 11:24:21','2026-11-27 11:24:36',4,0,0,1,'本次未新增记录：4 条今日已有同价记录（同日去重），价格无变化','2026-11-27 11:24:21'),(85,9,'STEAM-CN-20261127115423-3827',1,'2026-11-27 11:54:23','2026-11-27 11:54:23',0,0,0,3,'该平台暂无可用采集器（当前已接入：EPIC,STEAM,XBOX）','2026-11-27 11:54:23'),(86,9,'STEAM-CN-20261127115501-7165',1,'2026-11-27 11:55:01','2026-11-27 11:55:12',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 11:55:01'),(87,9,'STEAM-CN-20261127115657-5937',1,'2026-11-27 11:56:57','2026-11-27 11:57:09',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 11:56:57'),(88,9,'STEAM-CN-20261127115838-3861',1,'2026-11-27 11:58:39','2026-11-27 11:58:49',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 11:58:39'),(89,11,'XIAOHEIHE-CN-20261127123225-7978',1,'2026-11-27 12:32:25','2026-11-27 12:32:59',12,11,0,1,'其中 1 条价格无变化已跳过（同日去重）','2026-11-27 12:32:25'),(90,11,'XIAOHEIHE-CN-20261127123636-3728',1,'2026-11-27 12:36:37','2026-11-27 12:37:15',22,9,0,1,'其中 13 条价格无变化已跳过（同日去重）','2026-11-27 12:36:37'),(91,9,'STEAM-CN-20261127123730-1612',1,'2026-11-27 12:37:30','2026-11-27 12:37:43',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 12:37:30'),(92,10,'EPIC-CN-20261127123742-9554',1,'2026-11-27 12:37:43','2026-11-27 12:37:53',4,0,0,1,'本次未新增记录：4 条今日已有同价记录（同日去重），价格无变化','2026-11-27 12:37:43'),(93,11,'XIAOHEIHE-CN-20261127123752-7585',1,'2026-11-27 12:37:53','2026-11-27 12:38:31',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 12:37:53'),(94,9,'STEAM-CN-20261127145734-2966',1,'2026-11-27 14:57:35','2026-11-27 14:57:49',22,0,0,1,'本次未新增记录：22 条今日已有同价记录（同日去重），价格无变化','2026-11-27 14:57:35');
/*!40000 ALTER TABLE `collect_task_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exchange_rate_snapshot`
--

DROP TABLE IF EXISTS `exchange_rate_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exchange_rate_snapshot` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `base_currency` char(3) NOT NULL COMMENT '原币，如 USD',
  `quote_currency` char(3) NOT NULL DEFAULT 'CNY' COMMENT '目标币',
  `rate` decimal(12,6) NOT NULL COMMENT '1 单位原币 = rate 人民币',
  `provider` varchar(64) NOT NULL COMMENT '汇率服务商标识',
  `fetched_at` datetime NOT NULL COMMENT '拉取时间（AC-4.7 沿用最近成功快照）',
  `success` tinyint NOT NULL DEFAULT '1' COMMENT '1成功 0失败',
  `error_msg` varchar(200) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_base_quote_time` (`base_currency`,`quote_currency`,`fetched_at`),
  KEY `idx_base_fetched` (`base_currency`,`fetched_at`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='汇率快照';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exchange_rate_snapshot`
--

LOCK TABLES `exchange_rate_snapshot` WRITE;
/*!40000 ALTER TABLE `exchange_rate_snapshot` DISABLE KEYS */;
INSERT INTO `exchange_rate_snapshot` VALUES (1,'USD','CNY',7.200000,'seed','2026-11-20 11:04:19',1,NULL,'2026-11-20 11:04:19'),(2,'HKD','CNY',0.920000,'seed','2026-11-20 11:04:19',1,NULL,'2026-11-20 11:04:19'),(3,'JPY','CNY',0.048000,'seed','2026-11-20 11:04:19',1,NULL,'2026-11-20 11:04:19');
/*!40000 ALTER TABLE `exchange_rate_snapshot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '逻辑关联 sys_user.id（AC-9.4 隔离）',
  `game_id` bigint unsigned NOT NULL COMMENT '逻辑关联 game.id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间（AC-9.2 展示）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_game` (`user_id`,`game_id`),
  KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game`
--

DROP TABLE IF EXISTS `game`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) NOT NULL COMMENT '中文名（去空格后的唯一键）',
  `name_en` varchar(200) DEFAULT NULL COMMENT '英文名，搜索用',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图',
  `summary` varchar(1000) DEFAULT NULL COMMENT '简介',
  `game_type_id` bigint unsigned NOT NULL COMMENT '类型，逻辑关联 game_type.id',
  `release_year` smallint DEFAULT NULL COMMENT '发行年份（Q7 若加年份筛选则用）',
  `release_date` date DEFAULT NULL COMMENT '精确发行日期（Steam 校验补齐）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1上架 0下架（AC-11.3）',
  `sort_weight` int NOT NULL DEFAULT '0' COMMENT '列表默认排序权重',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`) COMMENT 'AC-11.4 重复新增拦截',
  KEY `idx_type_status` (`game_type_id`,`status`),
  KEY `idx_status_weight` (`status`,`sort_weight`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='游戏主数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game`
--

LOCK TABLES `game` WRITE;
/*!40000 ALTER TABLE `game` DISABLE KEYS */;
INSERT INTO `game` VALUES (1,'赛博朋克2077','Cyberpunk 2077','https://cdn.akamai.steamstatic.com/steam/apps/1091500/header.jpg',NULL,1,2020,'2020-12-09',1,0,'2026-11-19 17:38:09','2026-11-27 14:15:50'),(2,'只狼：影逝二度','Sekiro: Shadows Die Twice','https://cdn.akamai.steamstatic.com/steam/apps/814380/header.jpg',NULL,1,2019,'2019-03-21',1,0,'2026-11-19 17:38:09','2026-11-21 20:21:57'),(8,'艾尔登法环','ELDEN RING','https://cdn.akamai.steamstatic.com/steam/apps/1245620/header.jpg',NULL,2,2022,'2022-02-24',1,0,'2026-11-20 10:12:36','2026-11-21 20:21:57'),(9,'文明六','Sid Meier\'s Civilization VI','https://cdn.akamai.steamstatic.com/steam/apps/289070/header.jpg',NULL,3,2016,'2016-10-20',1,0,'2026-11-20 10:12:36','2026-11-21 20:21:57'),(12,'双人成行','It Takes Two - Friend\'s Pass','https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/1426210/header_schinese.jpg?t=1777395376','游玩《双人成行》，踏上生命中最疯狂的旅程，这是一款别开生面的平台冒险游戏，完全为合作模式而设计。利用好友通行证*邀请一位好友免费游玩，共同体验多种多样的乐趣，享受颠覆性的玩法挑战。扮演相互看不顺眼的科迪和小梅夫妇，这两个人被魔咒变成了玩偶。他们一起被困在一个奇幻世界里，每个角落都隐藏着意想不到的东西，他们不得不一起克服挑战，同时挽救他们破裂的关系。\n\n在每个新关卡中，掌握角色独特且与对方息息相关的能力。一定要互相帮助，克服大量出乎意料的障碍，共度搞笑欢乐的时刻。用力踢流氓松鼠毛茸茸的尾巴，绕过一条内裤，在热闹的夜店里开舞会，坐雪橇穿过神奇的雪花玻璃球。一个真挚又搞笑的故事在游戏中徐徐展开，交织成独特的隐喻体验。\n\n《双人成行》由业界合作游戏领导者且屡获殊荣的工作室 Hazelight 开发。他们即将带您踏上狂野而奇妙的旅程，只有一件事是确定的：二人同心，其利断金。',2,2021,'2021-03-25',1,0,'2026-11-23 09:31:49','2026-11-23 09:31:49'),(13,'Grand Theft Auto V 增强版','Grand Theft Auto V Enhanced','https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/3240220/header_schinese.jpg?t=1781187782','一个初涉江湖的街头新丁、一个洗手多年的银行劫匪和一个丧心病狂的杀人狂魔被卷入地下世界、美国政府与娱乐业最为恐怖、最为疯狂的元素之中时，他们必须齐心协力，接连完成一系列的惊天劫案，才能在这个冷血无情的城市中苟延残喘。他们谁也不能相信，尤其是身边的同伙。\n\n现在 PC 平台的玩家可以使用一次性迁移将 GTA 5 故事模式进度和 GTA 在线模式角色和进度从传承版的 GTA 5 迁移到 GTA 5 增强版。',2,2025,'2025-03-04',1,0,'2026-11-23 13:07:48','2026-11-27 11:54:23'),(14,'怪物猎人：世界','Monster Hunter: World','https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/582010/header.jpg?t=1791439347','新的生命之地。狩猎, 就是本能! 在人气作品「Monster Hunter: World」里, 地形和环境生物, 甚至魔物的生态系统都能被利用, 用一切手段挑战巨大的魔物。在新建构的「Monster Hunter: World」中, 可以体验到你一直期盼的极致猎人生活。',1,2018,'2018-08-08',1,0,'2026-11-27 09:40:28','2026-11-27 09:42:09'),(15,'怪物猎人：崛起','MONSTER HUNTER RISE','https://shared.st.dl.eccdnx.com/store_item_assets/steam/apps/1446780/header.jpg?t=1792548454','蓬勃跃动的狩猎本能！\n为狩猎掀起全新风潮、无拘无束尽情跃动的动作；可随心所欲四下驰骋的全新原野；以及能带来未知的兴奋与惊喜的全新怪物。\n前所未有的狩猎体验正在《MONSTER HUNTER RISE》中等候各位猎人的到来！',1,2022,'2022-01-12',1,0,'2026-11-27 09:43:47','2026-11-27 09:45:00'),(18,'Monster Hunter Wilds','Monster Hunter Wilds','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2246340/header.jpg?t=1787549555','狂野凶猛的大自然，袭来。 时刻都在动态变化的原野。 这是个关于生活在具有两面性的世界中的怪物与人们的故事。 进化的狩猎动作，寻求连续不断的沉浸感，究极的狩猎体验正等待你的到来。',1,2025,'2025-02-27',1,0,'2026-11-27 10:04:43','2026-11-27 10:04:43'),(19,'Monster Hunter Stories 3: 命运双龙','Monster Hunter Stories 3: Twisted Reflection','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2852190/427b69c83e34738f437eedb94607ed79847b294e/header_schinese.jpg?t=1790900261','“Monster Hunter”系列RPG第3弹！ 双龙降世，命运于此刻交织。 “Monster Hunter Stories”是一个RPG游戏系列， 玩家将会成为与怪物建立羁绊，对其加以培育并与之共存的“怪物骑士”，尽情体验“Monster Hunter”的世界。',1,2026,'2026-03-12',1,0,'2026-11-27 10:04:51','2026-11-27 10:04:51'),(20,'刺客信条：黑旗 记忆重置','Assassin\'s Creed Black Flag Resynced','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3751950/059af3f3f015295935e685bd25fa0f0abf105ea9/header_alt_assets_0_schinese.jpg?t=1795534981','经典单人海盗冒险之旅现已回归。在这部忠实还原且经过优化的重制版中，你将扮演爱德华·肯威，在海盗的黄金时代扬帆加勒比海。本作拥有惊艳的视觉效果、经过升级的玩法以及全新的内容。',1,2026,'2026-07-09',1,0,'2026-11-27 10:05:16','2026-11-27 10:05:16'),(21,'刺客信条：影','Assassin’s Creed Shadows','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3159330/header_schinese.jpg?t=1792778493','在日本战国背景下，体验史诗般的动作冒险故事！变身夺命忍者刺客，化为强大传奇武士，在一片混沌的时代，进入美丽的开放世界进行探索！',1,2025,'2025-03-19',1,0,'2026-11-27 10:05:17','2026-11-27 10:05:17'),(22,'刺客信条：英灵殿','Assassin\'s Creed Valhalla','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/2208920/0c0d3d8bf4e67c9872829659d2714b0334f71ded/header_alt_assets_0_schinese.jpg?t=1795528347','《刺客信条：英灵殿》是一款动作冒险游戏，您将在其中扮演一位从小在征战与荣耀的光辉故事中成长起来的维京战士。在英格兰的黑暗时代背景中探索美丽多变的开放世界。劫掠您的敌人，发展您的居民地，在英灵殿的众神间赢得自己的一席之地。',1,2022,'2022-12-06',1,0,'2026-11-27 10:05:19','2026-11-27 10:05:19'),(23,'刺客信条：起源','Assassin\'s Creed® Origins','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/582160/header.jpg?t=1793886930','在这款动作冒险游戏中，尽情探索古埃及。挑战强敌、揭穿阴谋，探索刺客兄弟会的起源故事。',1,2017,'2017-10-26',1,0,'2026-11-27 10:05:22','2026-11-27 10:05:22'),(24,'刺客信条：幻景','Assassin\'s Creed Mirage','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/3035570/0a5f6e1f3cc4cc9215752fd930a09b89e69d3331/header_schinese.jpg?t=1792777978','在这款动作冒险游戏中，化身致命的刺客大师。以巴辛姆的视角，探索九世纪巴格达的街头巷尾。加入无形者伸张正义，追猎你的目标。',1,2024,'2024-10-17',1,0,'2026-11-27 10:05:24','2026-11-27 10:05:24'),(25,'刺客信条：大革命','Assassin\'s Creed® Unity','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/289650/header.jpg?t=1786632648','巴黎，1789 年。在这款动作冒险游戏中，以阿尔诺·多里安的视角，亲历法国大革命的风云。化身刺客，为拯救祖国，揭穿革命幕后黑手的面纱。',1,2014,'2014-11-12',1,0,'2026-11-27 10:05:25','2026-11-27 10:05:25'),(26,'HITMAN World of Assassination','HITMAN World of Assassination','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/1659040/c62446d5c95f583a296d7f1b99045a61e2f81dea/header.jpg?t=1795703629','欢迎来到终极暗杀世界。《HITMAN暗杀世界》汇集了《HITMAN》、《HITMAN 2》和《HITMAN 3》的精华内容，包括主要战役、契约模式、升级契约、行踪不定的目标街机模式以及由roguelike类型启发而来的HITMAN：Freelancer游戏模式。',1,2022,'2022-01-20',1,0,'2026-11-27 10:06:05','2026-11-27 10:06:05'),(27,'Hitman: Absolution™','Hitman: Absolution™','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/203140/header.jpg?t=1734461136','The original assassin is back! Betrayed by the Agency and hunted by the police, Agent 47 finds himself pursuing redemption in a corrupt and twisted world.',1,2012,'2012-11-19',1,0,'2026-11-27 10:06:06','2026-11-27 10:06:06'),(28,'Hitman: Blood Money','Hitman: Blood Money','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/6860/header.jpg?t=1732283123','Money Talks. Silence Pays. Prepare to Make a Killing. When assassins from Agent 47\'s contract agency, The ICA, are eliminated in a series of hits, it seems a larger, more powerful agency has entered the fray. Sensing he may be a target, 47 travels to America, and prepares to make a killing.',1,2007,'2007-03-15',1,0,'2026-11-27 10:06:09','2026-11-27 10:06:09'),(29,'Hitman: Contracts','Hitman: Contracts','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/247430/header.jpg?t=1744238518','Enter the world of a HITMAN - a world of crime, sin and greed. Delve into the mind of Agent 47 and encounter his greatest adversaries, completing the work that made him so brutally efficient and shaped him as an assassin. Eliminate your targets by any means necessary. There are no rules... only the contract, signed in blood.',1,2014,'2014-01-21',1,0,'2026-11-27 10:06:10','2026-11-27 10:06:10'),(30,'Hitman: Codename 47','Hitman: Codename 47','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/6900/header.jpg?t=1732283914','As the enigmatic Hitman, you must use stealth and tactical problem solving to enter, execute and exit your assignment with minimum attention and maximum effectiveness. For a price, you have access to the most devious devices, but how you use them will determine if you retire as a millionaire or get permanently retired.',1,2007,'2007-03-15',1,0,'2026-11-27 10:06:11','2026-11-27 10:06:11'),(31,'Hitman 2: Silent Assassin','Hitman 2: Silent Assassin','https://shared.akamai.steamstatic.com/store_item_assets/steam/apps/6850/header.jpg?t=1732293077','Enter the realm of a retired assassin, forced back into action by treason. You may be a hired killer but you still have a sense of loyalty and justice. Visit the dark recesses of a world corrupted by crime, greed, degradation and dishonor. And a past that catches up with you.',1,2007,'2007-03-15',1,0,'2026-11-27 10:06:13','2026-11-27 10:06:13');
/*!40000 ALTER TABLE `game` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game_platform`
--

DROP TABLE IF EXISTS `game_platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_platform` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `game_id` bigint unsigned NOT NULL COMMENT '逻辑关联 game.id',
  `platform_id` bigint unsigned NOT NULL COMMENT '逻辑关联 platform.id',
  `external_id` varchar(100) DEFAULT NULL COMMENT '平台侧商品ID，如 Steam appid',
  `store_url` varchar(500) DEFAULT NULL COMMENT '最终商店链接，优先于模板生成（AC-7.3 可后台改）',
  `listed` tinyint NOT NULL DEFAULT '1' COMMENT '1该平台已上架 0未上架（AC-4.3）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_game_platform` (`game_id`,`platform_id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='游戏平台关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_platform`
--

LOCK TABLES `game_platform` WRITE;
/*!40000 ALTER TABLE `game_platform` DISABLE KEYS */;
INSERT INTO `game_platform` VALUES (1,1,1,'1091500',NULL,1,'2026-11-21 12:34:04','2026-11-21 12:57:13'),(2,2,1,'814380',NULL,1,'2026-11-21 12:34:04','2026-11-21 12:34:04'),(3,1,4,'1091500',NULL,1,'2026-11-21 13:58:40','2026-11-21 13:58:40'),(4,9,1,'289070',NULL,1,'2026-11-21 13:58:55','2026-11-21 13:58:55'),(5,8,1,'1245620',NULL,1,'2026-11-21 13:58:55','2026-11-21 13:58:55'),(7,1,9,'1091500','https://store.steampowered.com/app/1091500?cc=cn',1,'2026-11-23 13:01:53','2026-11-23 13:01:53'),(8,2,9,'814380','https://store.steampowered.com/app/814380?cc=cn',1,'2026-11-23 13:01:54','2026-11-23 13:01:54'),(9,8,9,'1245620','https://store.steampowered.com/app/1245620?cc=cn',1,'2026-11-23 13:01:55','2026-11-23 13:01:55'),(10,9,9,'289070',NULL,1,'2026-11-23 13:01:55','2026-11-23 13:25:37'),(11,12,9,'1426210','https://store.steampowered.com/app/1426210?cc=cn',1,'2026-11-23 13:01:56','2026-11-23 13:01:56'),(12,13,9,'3240220','https://store.steampowered.com/app/3240220?cc=cn',1,'2026-11-23 13:08:12','2026-11-23 13:08:12'),(13,13,10,'grand-theft-auto-v',NULL,1,'2026-11-23 16:51:38','2026-11-27 11:12:35'),(14,12,10,'it-takes-two',NULL,1,'2026-11-23 16:51:44','2026-11-27 11:12:35'),(15,9,10,'sid-meiers-civilization-vi','https://store.epicgames.com/p/sid-meiers-civilization-vi',1,'2026-11-23 16:51:47','2026-11-27 11:12:35'),(18,1,10,'cyberpunk-2077',NULL,1,'2026-11-23 16:51:58','2026-11-27 11:12:35'),(19,13,11,'3240220','https://www.xiaoheihe.cn/app/topic/game/pc/3240220',1,'2026-11-23 17:00:59','2026-11-23 17:00:59'),(20,13,12,NULL,NULL,1,'2026-11-23 17:00:59','2026-11-23 17:00:59'),(21,13,13,NULL,NULL,1,'2026-11-23 17:01:00','2026-11-23 17:01:00'),(22,12,11,'1426210','https://www.xiaoheihe.cn/app/topic/game/pc/1426210',1,'2026-11-23 17:01:06','2026-11-23 17:01:06'),(23,12,12,NULL,NULL,1,'2026-11-23 17:01:07','2026-11-23 17:01:07'),(24,12,13,NULL,NULL,1,'2026-11-23 17:01:07','2026-11-23 17:01:07'),(25,9,13,NULL,NULL,1,'2026-11-23 17:01:11','2026-11-23 17:01:11'),(26,9,12,NULL,NULL,1,'2026-11-23 17:01:12','2026-11-23 17:01:12'),(27,9,11,'289070','https://www.xiaoheihe.cn/app/topic/game/pc/289070',1,'2026-11-23 17:01:12','2026-11-23 17:01:12'),(28,8,13,NULL,NULL,1,'2026-11-23 17:01:16','2026-11-23 17:01:16'),(29,8,12,NULL,NULL,1,'2026-11-23 17:01:17','2026-11-23 17:01:17'),(30,8,11,'1245620','https://www.xiaoheihe.cn/app/topic/game/pc/1245620',1,'2026-11-23 17:01:17','2026-11-23 17:01:17'),(31,2,13,NULL,NULL,1,'2026-11-23 17:01:21','2026-11-23 17:01:21'),(32,2,12,NULL,NULL,1,'2026-11-23 17:01:21','2026-11-23 17:01:21'),(33,2,11,'814380','https://www.xiaoheihe.cn/app/topic/game/pc/814380',1,'2026-11-23 17:01:22','2026-11-23 17:01:22'),(34,1,13,NULL,NULL,1,'2026-11-23 17:01:26','2026-11-23 17:01:26'),(35,1,12,NULL,NULL,1,'2026-11-23 17:01:27','2026-11-23 17:01:27'),(36,1,11,'1091500','https://www.xiaoheihe.cn/app/topic/game/pc/1091500',1,'2026-11-23 17:01:27','2026-11-23 17:01:27'),(37,14,9,'582010','https://store.steampowered.com/app/582010?cc=cn',1,'2026-11-27 09:42:53','2026-11-27 09:42:53'),(38,15,9,'1446780',NULL,1,'2026-11-27 09:44:03','2026-11-27 09:44:03'),(41,18,9,'2246340','https://store.steampowered.com/app/2246340?cc=cn',1,'2026-11-27 10:04:43','2026-11-27 10:04:43'),(42,19,9,'2852190','https://store.steampowered.com/app/2852190?cc=cn',1,'2026-11-27 10:04:51','2026-11-27 10:04:51'),(43,20,9,'3751950','https://store.steampowered.com/app/3751950?cc=cn',1,'2026-11-27 10:05:16','2026-11-27 10:05:16'),(44,21,9,'3159330','https://store.steampowered.com/app/3159330?cc=cn',1,'2026-11-27 10:05:17','2026-11-27 10:05:17'),(45,22,9,'2208920','https://store.steampowered.com/app/2208920?cc=cn',1,'2026-11-27 10:05:19','2026-11-27 10:05:19'),(46,23,9,'582160','https://store.steampowered.com/app/582160?cc=cn',1,'2026-11-27 10:05:22','2026-11-27 10:05:22'),(47,24,9,'3035570','https://store.steampowered.com/app/3035570?cc=cn',1,'2026-11-27 10:05:24','2026-11-27 10:05:24'),(48,25,9,'289650','https://store.steampowered.com/app/289650?cc=cn',1,'2026-11-27 10:05:25','2026-11-27 10:05:25'),(49,26,9,'1659040','https://store.steampowered.com/app/1659040?cc=cn',1,'2026-11-27 10:06:05','2026-11-27 10:06:05'),(50,27,9,'203140','https://store.steampowered.com/app/203140?cc=cn',1,'2026-11-27 10:06:06','2026-11-27 10:06:06'),(51,28,9,'6860','https://store.steampowered.com/app/6860?cc=cn',1,'2026-11-27 10:06:09','2026-11-27 10:06:09'),(52,29,9,'247430','https://store.steampowered.com/app/247430?cc=cn',1,'2026-11-27 10:06:10','2026-11-27 10:06:10'),(53,30,9,'6900','https://store.steampowered.com/app/6900?cc=cn',1,'2026-11-27 10:06:11','2026-11-27 10:06:11'),(54,31,9,'6850','https://store.steampowered.com/app/6850?cc=cn',1,'2026-11-27 10:06:13','2026-11-27 10:06:13'),(56,14,11,'582010','https://www.xiaoheihe.cn/app/topic/game/pc/582010',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(57,15,11,'1446780','https://www.xiaoheihe.cn/app/topic/game/pc/1446780',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(58,18,11,'2246340','https://www.xiaoheihe.cn/app/topic/game/pc/2246340',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(59,19,11,'2852190','https://www.xiaoheihe.cn/app/topic/game/pc/2852190',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(60,20,11,'3751950','https://www.xiaoheihe.cn/app/topic/game/pc/3751950',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(61,21,11,'3159330','https://www.xiaoheihe.cn/app/topic/game/pc/3159330',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(62,22,11,'2208920','https://www.xiaoheihe.cn/app/topic/game/pc/2208920',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(63,23,11,'582160','https://www.xiaoheihe.cn/app/topic/game/pc/582160',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(64,24,11,'3035570','https://www.xiaoheihe.cn/app/topic/game/pc/3035570',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(65,25,11,'289650','https://www.xiaoheihe.cn/app/topic/game/pc/289650',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(66,26,11,'1659040','https://www.xiaoheihe.cn/app/topic/game/pc/1659040',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(67,27,11,'203140','https://www.xiaoheihe.cn/app/topic/game/pc/203140',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(68,28,11,'6860','https://www.xiaoheihe.cn/app/topic/game/pc/6860',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(69,29,11,'247430','https://www.xiaoheihe.cn/app/topic/game/pc/247430',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(70,30,11,'6900','https://www.xiaoheihe.cn/app/topic/game/pc/6900',1,'2026-11-27 12:26:34','2026-11-27 12:26:34'),(71,31,11,'6850','https://www.xiaoheihe.cn/app/topic/game/pc/6850',1,'2026-11-27 12:26:34','2026-11-27 12:26:34');
/*!40000 ALTER TABLE `game_platform` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game_type`
--

DROP TABLE IF EXISTS `game_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_type` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '类型编码，如 ACTION/RPG',
  `name` varchar(32) NOT NULL COMMENT '展示名称，如 动作',
  `sort_no` int NOT NULL DEFAULT '0' COMMENT '排序',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '0正常 1删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='游戏类型字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_type`
--

LOCK TABLES `game_type` WRITE;
/*!40000 ALTER TABLE `game_type` DISABLE KEYS */;
INSERT INTO `game_type` VALUES (1,'ACTION','动作',1,0,'2026-11-19 17:38:09','2026-11-19 17:38:09'),(2,'RPG','角色扮演',2,0,'2026-11-19 17:38:09','2026-11-19 17:38:09'),(3,'STRATEGY','策略',3,0,'2026-11-19 17:38:09','2026-11-19 17:38:09'),(5,'ADVENTURE','冒险',4,0,'2026-11-27 14:15:02','2026-11-27 14:15:02'),(6,'OPEN_WORLD','开放世界',5,0,'2026-11-27 14:15:02','2026-11-27 14:15:02');
/*!40000 ALTER TABLE `game_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `game_type_rel`
--

DROP TABLE IF EXISTS `game_type_rel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `game_type_rel` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `game_id` bigint unsigned NOT NULL,
  `type_id` bigint unsigned NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_game_type` (`game_id`,`type_id`),
  KEY `idx_type` (`type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='游戏-类型多标签关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `game_type_rel`
--

LOCK TABLES `game_type_rel` WRITE;
/*!40000 ALTER TABLE `game_type_rel` DISABLE KEYS */;
INSERT INTO `game_type_rel` VALUES (1,2,1,'2026-11-27 14:09:09'),(2,14,1,'2026-11-27 14:09:09'),(3,15,1,'2026-11-27 14:09:09'),(4,18,1,'2026-11-27 14:09:09'),(5,19,1,'2026-11-27 14:09:09'),(6,20,1,'2026-11-27 14:09:09'),(7,21,1,'2026-11-27 14:09:09'),(8,22,1,'2026-11-27 14:09:09'),(9,23,1,'2026-11-27 14:09:09'),(10,24,1,'2026-11-27 14:09:09'),(11,25,1,'2026-11-27 14:09:09'),(12,26,1,'2026-11-27 14:09:09'),(13,27,1,'2026-11-27 14:09:09'),(14,28,1,'2026-11-27 14:09:09'),(15,29,1,'2026-11-27 14:09:09'),(16,30,1,'2026-11-27 14:09:09'),(19,8,2,'2026-11-27 14:09:09'),(20,12,2,'2026-11-27 14:09:09'),(21,13,2,'2026-11-27 14:09:09'),(22,9,3,'2026-11-27 14:09:09'),(32,31,1,'2026-11-27 14:15:29'),(33,31,3,'2026-11-27 14:15:29'),(34,1,1,'2026-11-27 14:15:50');
/*!40000 ALTER TABLE `game_type_rel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `historical_low`
--

DROP TABLE IF EXISTS `historical_low`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historical_low` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `game_id` bigint unsigned NOT NULL COMMENT '逻辑关联 game.id',
  `platform_id` bigint unsigned NOT NULL COMMENT '逻辑关联 platform.id',
  `low_amount` decimal(10,2) NOT NULL COMMENT '史低原币金额',
  `currency` char(3) NOT NULL COMMENT '史低币种',
  `low_cny` decimal(10,2) DEFAULT NULL COMMENT '史低折算人民币（仅展示）',
  `low_date` date DEFAULT NULL COMMENT '史低发生日期',
  `source` varchar(100) NOT NULL COMMENT '数据源名（AC-5.3/D6 可追溯）',
  `source_ref` varchar(200) DEFAULT NULL COMMENT '第三方记录ID',
  `fetched_at` datetime NOT NULL COMMENT '本次同步时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_game_platform_source` (`game_id`,`platform_id`,`source`),
  KEY `idx_game` (`game_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='官方史低价（第三方授权源）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `historical_low`
--

LOCK TABLES `historical_low` WRITE;
/*!40000 ALTER TABLE `historical_low` DISABLE KEYS */;
INSERT INTO `historical_low` VALUES (1,1,1,9.86,'USD',71.00,'2024-11-27','IsThereAnyDeal（授权测试）','itad-test-001','2026-11-21 15:02:14','2026-11-21 15:02:14','2026-11-21 15:19:25'),(2,1,3,12.99,'USD',93.53,'2024-12-20','IsThereAnyDeal（授权测试）','itad-test-002','2026-11-21 15:22:03','2026-11-21 15:22:03','2026-11-21 15:22:03');
/*!40000 ALTER TABLE `historical_low` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint unsigned NOT NULL COMMENT '接收人',
  `alert_rule_id` bigint unsigned DEFAULT NULL COMMENT '来源规则',
  `game_id` bigint unsigned NOT NULL COMMENT '关联游戏（AC-10.2 可进详情）',
  `platform_id` bigint unsigned DEFAULT NULL COMMENT '触发平台',
  `condition_type` tinyint NOT NULL COMMENT '1目标价 2史低 3任意降价',
  `price_cny` decimal(10,2) NOT NULL COMMENT '触发时人民币价（AC-10.2）',
  `title` varchar(200) NOT NULL,
  `content` varchar(1000) DEFAULT NULL,
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '0未读 1已读（AC-10.6）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_user_time` (`user_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内通知';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (6,5,4,1,1,1,68.83,'《赛博朋克2077》价格降至 ¥68.83','平台：Steam；条件：达到目标价 ¥70.00',1,'2026-11-23 09:27:16','2026-11-23 09:27:26'),(7,5,4,1,1,1,61.92,'《赛博朋克2077》价格降至 ¥61.92','平台：Steam；条件：达到目标价 ¥70.00',1,'2026-11-23 09:33:16','2026-11-23 09:33:26'),(8,2,5,2,11,1,129.00,'《只狼：影逝二度》价格降至 ¥129.00','平台：小黑盒；条件：达到目标价 ¥134.00',1,'2026-11-27 09:28:51','2026-11-27 09:29:10');
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `platform`
--

DROP TABLE IF EXISTS `platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `platform` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(32) NOT NULL COMMENT '平台编码 STEAM/EPIC/PSN/XBOX/NS',
  `name` varchar(64) NOT NULL COMMENT '展示名',
  `region` varchar(16) NOT NULL COMMENT '区域 US/HK/CN/TW…',
  `currency` char(3) NOT NULL COMMENT '该区域结算币种 USD/HKD…',
  `link_template` varchar(500) NOT NULL COMMENT '商店链接模板，占位 {externalId}（AC-7.2）',
  `data_source_type` tinyint NOT NULL COMMENT '1官方API 2授权第三方 3仅手动（AC-12.7 依据）',
  `api_ready` tinyint NOT NULL DEFAULT '0' COMMENT '数据源是否就绪，0未就绪（AC-13.4）',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '1启用 0禁用，禁用不参与最低价（AC-13.2）',
  `remark` varchar(200) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code_region` (`code`,`region`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `platform`
--

LOCK TABLES `platform` WRITE;
/*!40000 ALTER TABLE `platform` DISABLE KEYS */;
INSERT INTO `platform` VALUES (1,'STEAM','Steam','US','USD','https://store.steampowered.com/app/{externalId}',1,1,0,'币种变更测试',1,'2026-11-19 17:38:09','2026-11-23 13:46:38'),(2,'EPIC','Epic Games','US','USD','https://store.epicgames.com/p/{externalId}',1,1,0,'',1,'2026-11-19 17:38:09','2026-11-23 13:46:40'),(3,'XBOX','Xbox','US','USD','https://www.xbox.com/games/store/{externalId}',1,1,0,NULL,1,'2026-11-20 10:12:36','2026-11-23 13:46:41'),(4,'STEAM','Steam (HK)','HK','HKD','https://store.steampowered.com/app/{externalId}?cc=hk',1,1,0,NULL,1,'2026-11-21 13:58:40','2026-11-23 13:46:43'),(9,'STEAM','Steam','CN','CNY','https://store.steampowered.com/app/{externalId}?cc=cn',1,1,1,'',0,'2026-11-23 12:46:52','2026-11-23 12:46:52'),(10,'EPIC','Epic Games','CN','CNY','https://store.epicgames.com/p/{externalId}',2,1,1,'',0,'2026-11-23 12:47:49','2026-11-23 12:47:49'),(11,'XIAOHEIHE','小黑盒','CN','CNY','https://www.xiaoheihe.cn/app/topic/game/pc/{externalId}',2,1,1,'',0,'2026-11-23 16:58:15','2026-11-27 12:31:10'),(12,'SHANGUO','杉果','CN','CNY','https://www.sonkwo.cn/sku/{externalId}',3,0,1,'',0,'2026-11-23 16:59:08','2026-11-27 10:41:24'),(13,'STEAMPY','steampy','CN','CNY','https://steampy.com/home?kw={externalId}',3,0,1,'',0,'2026-11-23 16:59:38','2026-11-27 10:41:24');
/*!40000 ALTER TABLE `platform` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `price_record`
--

DROP TABLE IF EXISTS `price_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `game_id` bigint unsigned NOT NULL COMMENT '逻辑关联 game.id',
  `platform_id` bigint unsigned NOT NULL COMMENT '逻辑关联 platform.id',
  `currency` char(3) NOT NULL COMMENT '原币种',
  `original_amount` decimal(10,2) NOT NULL COMMENT '原币现价（实付）',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原币划线原价，无折扣时等于 original_amount',
  `discount_rate` decimal(5,2) DEFAULT NULL COMMENT '折扣率，40.00 表示减 40%（AC-6.1）',
  `discount_end_time` datetime DEFAULT NULL COMMENT '折扣截止时间，为空表示无截止（AC-6.1）',
  `rate` decimal(12,6) NOT NULL COMMENT '入库时使用的汇率（原币→CNY）',
  `rate_time` datetime NOT NULL COMMENT '汇率快照时间（AC-4.7 展示汇率时间）',
  `cny_amount` decimal(10,2) NOT NULL COMMENT '人民币实付价，入库即固化（AC-4.5/4.6）',
  `source_type` tinyint NOT NULL COMMENT '1手动 2官方API 3第三方授权',
  `source_ref` varchar(200) DEFAULT NULL COMMENT '来源标识：采集批次号/录入备注（AC-12.5）',
  `operator` varchar(64) NOT NULL COMMENT '操作人用户名或采集任务名（AC-12.5）',
  `recorded_at` datetime NOT NULL COMMENT '该价格的生效时间（来源侧时间）',
  `is_current` tinyint DEFAULT NULL COMMENT '1当前生效 NULL历史或待确认',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1生效 2待确认 3已驳回（AC-12.6）',
  `verified_by` varchar(64) DEFAULT NULL COMMENT '待确认队列的审核人',
  `verified_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_current` (`game_id`,`platform_id`,`is_current`) COMMENT 'AC-12.4 同游戏同平台仅一条当前价（NULL 不受限）',
  KEY `idx_gp_time` (`game_id`,`platform_id`,`recorded_at`) COMMENT 'AC-5.2 走势按时间取点',
  KEY `idx_status` (`status`),
  KEY `idx_current_platform` (`is_current`,`platform_id`)
) ENGINE=InnoDB AUTO_INCREMENT=214 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='跨平台价格记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `price_record`
--

LOCK TABLES `price_record` WRITE;
/*!40000 ALTER TABLE `price_record` DISABLE KEYS */;
INSERT INTO `price_record` VALUES (94,1,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-23 13:01:59',298.00,2,'STEAM-CN-20261123130151-1986','collect:STEAM-CN-20261123130151-1986','2026-11-23 13:01:59',NULL,1,NULL,NULL,'2026-11-23 13:01:59','2026-11-24 14:38:49'),(95,2,9,'CNY',268.00,268.00,0.00,NULL,1.000000,'2026-11-23 13:01:59',268.00,2,'STEAM-CN-20261123130151-1986','collect:STEAM-CN-20261123130151-1986','2026-11-23 13:01:59',NULL,1,NULL,NULL,'2026-11-23 13:01:59','2026-11-24 14:38:49'),(96,8,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-23 13:01:59',298.00,2,'STEAM-CN-20261123130151-1986','collect:STEAM-CN-20261123130151-1986','2026-11-23 13:01:59',NULL,1,NULL,NULL,'2026-11-23 13:01:59','2026-11-24 14:38:49'),(97,9,9,'CNY',148.00,148.00,0.00,NULL,1.000000,'2026-11-23 13:01:59',148.00,2,'STEAM-CN-20261123130151-1986','collect:STEAM-CN-20261123130151-1986','2026-11-23 13:01:59',NULL,1,NULL,NULL,'2026-11-23 13:01:59','2026-11-23 13:20:10'),(98,12,9,'CNY',39.60,198.00,80.00,NULL,1.000000,'2026-11-23 13:01:59',39.60,2,'STEAM-CN-20261123130151-1986','collect:STEAM-CN-20261123130151-1986','2026-11-23 13:01:59',NULL,1,NULL,NULL,'2026-11-23 13:01:59','2026-11-24 14:38:49'),(99,13,9,'CNY',149.00,149.00,0.00,NULL,1.000000,'2026-11-23 13:08:16',149.00,2,'STEAM-CN-20261123130808-1795','collect:STEAM-CN-20261123130808-1795','2026-11-23 13:08:16',NULL,1,NULL,NULL,'2026-11-23 13:08:16','2026-11-24 04:00:03'),(100,9,9,'CNY',22.00,220.00,90.00,NULL,1.000000,'2026-11-23 13:17:48',22.00,2,'STEAM-CN-20261123131740-7039','collect:STEAM-CN-20261123131740-7039','2026-11-23 13:17:48',NULL,1,'testuser','2026-11-23 13:20:11','2026-11-23 13:17:48','2026-11-23 13:31:15'),(101,9,9,'CNY',11.00,198.00,95.00,NULL,1.000000,'2026-11-23 13:31:16',11.00,1,'','admin','2026-11-23 13:31:16',NULL,1,NULL,NULL,'2026-11-23 13:31:16','2026-11-23 13:32:30'),(102,9,9,'CNY',11.00,220.00,95.00,NULL,1.000000,'2026-11-23 13:32:31',11.00,1,'','admin','2026-11-23 13:32:31',NULL,1,NULL,NULL,'2026-11-23 13:32:31','2026-11-23 16:49:42'),(103,9,9,'CNY',22.00,220.00,90.00,NULL,1.000000,'2026-11-23 16:49:31',22.00,2,'STEAM-CN-20261123164926-5180','collect:STEAM-CN-20261123164926-5180','2026-11-23 16:49:31',NULL,1,'testuser','2026-11-23 16:49:43','2026-11-23 16:49:31','2026-11-24 14:38:49'),(104,9,10,'CNY',182.00,182.00,0.00,NULL,1.000000,'2026-11-23 17:11:25',182.00,1,'','admin','2026-11-23 17:11:25',NULL,1,NULL,NULL,'2026-11-23 17:11:25','2026-11-24 14:42:43'),(105,9,11,'CNY',22.00,220.00,90.00,'2026-12-03 00:00:00',1.000000,'2026-11-23 17:46:22',22.00,1,'','admin','2026-11-23 17:46:23',NULL,1,NULL,NULL,'2026-11-23 17:46:23','2026-11-24 14:43:11'),(106,13,9,'CNY',98.56,224.00,56.00,NULL,1.000000,'2026-11-24 04:00:04',98.56,2,'STEAM-CN-20261124040000-5576','collect:STEAM-CN-20261124040000-5576','2026-11-24 04:00:04',NULL,1,NULL,NULL,'2026-11-24 04:00:04','2026-11-25 09:18:15'),(107,1,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-24 14:38:49',298.00,2,'STEAM-CN-20261124143844-5035','collect:STEAM-CN-20261124143844-5035','2026-11-24 14:38:49',NULL,1,NULL,NULL,'2026-11-24 14:38:49','2026-11-25 09:18:15'),(108,2,9,'CNY',268.00,268.00,0.00,NULL,1.000000,'2026-11-24 14:38:49',268.00,2,'STEAM-CN-20261124143844-5035','collect:STEAM-CN-20261124143844-5035','2026-11-24 14:38:49',NULL,1,NULL,NULL,'2026-11-24 14:38:49','2026-11-25 09:18:15'),(109,8,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-24 14:38:49',298.00,2,'STEAM-CN-20261124143844-5035','collect:STEAM-CN-20261124143844-5035','2026-11-24 14:38:49',NULL,1,NULL,NULL,'2026-11-24 14:38:49','2026-11-25 09:18:15'),(110,9,9,'CNY',22.00,220.00,90.00,NULL,1.000000,'2026-11-24 14:38:49',22.00,2,'STEAM-CN-20261124143844-5035','collect:STEAM-CN-20261124143844-5035','2026-11-24 14:38:49',NULL,1,NULL,NULL,'2026-11-24 14:38:49','2026-11-25 09:18:15'),(111,12,9,'CNY',39.60,198.00,80.00,NULL,1.000000,'2026-11-24 14:38:49',39.60,2,'STEAM-CN-20261124143844-5035','collect:STEAM-CN-20261124143844-5035','2026-11-24 14:38:49',NULL,1,NULL,NULL,'2026-11-24 14:38:49','2026-11-25 09:18:15'),(112,9,10,'CNY',182.00,182.00,0.00,NULL,1.000000,'2026-11-24 14:42:44',182.00,1,'','admin','2026-11-24 14:42:44',NULL,1,NULL,NULL,'2026-11-24 14:42:44','2026-11-25 09:20:22'),(113,9,11,'CNY',22.00,220.00,90.00,'2027-11-03 00:00:00',1.000000,'2026-11-24 14:43:12',22.00,1,'','admin','2026-11-24 14:43:12',NULL,1,NULL,NULL,'2026-11-24 14:43:12','2026-11-25 09:19:20'),(114,1,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-25 09:18:16',298.00,2,'STEAM-CN-20261125091811-5981','collect:STEAM-CN-20261125091811-5981','2026-11-25 09:18:16',NULL,1,NULL,NULL,'2026-11-25 09:18:16','2026-11-27 09:07:55'),(115,2,9,'CNY',268.00,268.00,0.00,NULL,1.000000,'2026-11-25 09:18:16',268.00,2,'STEAM-CN-20261125091811-5981','collect:STEAM-CN-20261125091811-5981','2026-11-25 09:18:16',NULL,1,NULL,NULL,'2026-11-25 09:18:16','2026-11-27 09:07:31'),(116,8,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-25 09:18:16',298.00,2,'STEAM-CN-20261125091811-5981','collect:STEAM-CN-20261125091811-5981','2026-11-25 09:18:16',NULL,1,NULL,NULL,'2026-11-25 09:18:16','2026-11-27 09:07:31'),(117,9,9,'CNY',22.00,220.00,90.00,NULL,1.000000,'2026-11-25 09:18:16',22.00,2,'STEAM-CN-20261125091811-5981','collect:STEAM-CN-20261125091811-5981','2026-11-25 09:18:16',NULL,1,NULL,NULL,'2026-11-25 09:18:16','2026-11-27 09:07:31'),(118,12,9,'CNY',39.60,198.00,80.00,NULL,1.000000,'2026-11-25 09:18:16',39.60,2,'STEAM-CN-20261125091811-5981','collect:STEAM-CN-20261125091811-5981','2026-11-25 09:18:16',NULL,1,NULL,NULL,'2026-11-25 09:18:16','2026-11-27 09:07:31'),(119,13,9,'CNY',98.56,224.00,56.00,NULL,1.000000,'2026-11-25 09:18:16',98.56,2,'STEAM-CN-20261125091811-5981','collect:STEAM-CN-20261125091811-5981','2026-11-25 09:18:16',NULL,1,NULL,NULL,'2026-11-25 09:18:16','2026-11-27 09:07:31'),(120,9,11,'CNY',22.00,220.00,90.00,'2026-12-03 00:00:00',1.000000,'2026-11-25 09:19:21',22.00,1,'','admin','2026-11-25 09:19:21',NULL,1,NULL,NULL,'2026-11-25 09:19:21','2026-11-27 09:09:04'),(121,9,10,'CNY',182.00,182.00,0.00,NULL,1.000000,'2026-11-25 09:20:23',182.00,1,'','admin','2026-11-25 09:20:23',NULL,1,NULL,NULL,'2026-11-25 09:20:23','2026-11-27 09:09:52'),(162,1,9,'CNY',89.40,298.00,70.00,NULL,1.000000,'2026-11-27 09:07:31',89.40,2,'STEAM-CN-20261127090727-8651','collect:STEAM-CN-20261127090727-8651','2026-11-27 09:07:31',1,1,'testuser','2026-11-27 09:07:55','2026-11-27 09:07:31','2026-11-27 09:07:55'),(163,2,9,'CNY',268.00,268.00,0.00,NULL,1.000000,'2026-11-27 09:07:31',268.00,2,'STEAM-CN-20261127090727-8651','collect:STEAM-CN-20261127090727-8651','2026-11-27 09:07:31',1,1,NULL,NULL,'2026-11-27 09:07:31','2026-11-27 09:07:31'),(164,8,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-27 09:07:31',298.00,2,'STEAM-CN-20261127090727-8651','collect:STEAM-CN-20261127090727-8651','2026-11-27 09:07:31',1,1,NULL,NULL,'2026-11-27 09:07:31','2026-11-27 09:07:31'),(165,9,9,'CNY',22.00,220.00,90.00,NULL,1.000000,'2026-11-27 09:07:31',22.00,2,'STEAM-CN-20261127090727-8651','collect:STEAM-CN-20261127090727-8651','2026-11-27 09:07:31',1,1,NULL,NULL,'2026-11-27 09:07:31','2026-11-27 09:07:31'),(166,12,9,'CNY',39.60,198.00,80.00,NULL,1.000000,'2026-11-27 09:07:31',39.60,2,'STEAM-CN-20261127090727-8651','collect:STEAM-CN-20261127090727-8651','2026-11-27 09:07:31',1,1,NULL,NULL,'2026-11-27 09:07:31','2026-11-27 09:07:31'),(167,13,9,'CNY',98.56,224.00,56.00,NULL,1.000000,'2026-11-27 09:07:31',98.56,2,'STEAM-CN-20261127090727-8651','collect:STEAM-CN-20261127090727-8651','2026-11-27 09:07:31',1,1,NULL,NULL,'2026-11-27 09:07:31','2026-11-27 09:07:31'),(168,9,11,'CNY',22.00,220.00,90.00,'2026-12-03 00:00:00',1.000000,'2026-11-27 09:09:05',22.00,1,'','admin','2026-11-27 09:09:05',1,1,NULL,NULL,'2026-11-27 09:09:05','2026-11-27 09:09:05'),(169,9,10,'CNY',182.00,182.00,0.00,NULL,1.000000,'2026-11-27 09:09:53',182.00,1,'','admin','2026-11-27 09:09:53',1,1,NULL,NULL,'2026-11-27 09:09:53','2026-11-27 09:09:53'),(170,2,11,'CNY',264.00,264.00,0.00,NULL,1.000000,'2026-11-27 09:25:36',264.00,1,'','admin','2026-11-27 09:25:36',NULL,1,NULL,NULL,'2026-11-27 09:25:36','2026-11-27 09:27:16'),(171,2,11,'CNY',129.00,268.00,52.00,'2026-11-30 00:00:00',1.000000,'2026-11-27 09:27:16',129.00,1,'','admin','2026-11-27 09:27:16',NULL,1,NULL,NULL,'2026-11-27 09:27:16','2026-11-27 09:27:47'),(172,2,11,'CNY',268.00,268.00,0.00,NULL,1.000000,'2026-11-27 09:27:47',268.00,1,'','admin','2026-11-27 09:27:47',NULL,1,NULL,NULL,'2026-11-27 09:27:47','2026-11-27 09:28:24'),(173,2,11,'CNY',129.00,268.00,52.00,'2026-11-30 00:00:00',1.000000,'2026-11-27 09:28:24',129.00,1,'','admin','2026-11-27 09:28:24',NULL,1,NULL,NULL,'2026-11-27 09:28:24','2026-11-27 09:29:27'),(174,2,11,'CNY',268.00,268.00,0.00,NULL,1.000000,'2026-11-27 09:29:27',268.00,1,'','admin','2026-11-27 09:29:27',1,1,NULL,NULL,'2026-11-27 09:29:27','2026-11-27 09:29:27'),(175,14,9,'CNY',148.00,148.00,0.00,NULL,1.000000,'2026-11-27 09:42:56',148.00,2,'STEAM-CN-20261127094252-5966','collect:STEAM-CN-20261127094252-5966','2026-11-27 09:42:56',1,1,NULL,NULL,'2026-11-27 09:42:56','2026-11-27 09:42:56'),(176,15,9,'CNY',198.00,198.00,0.00,NULL,1.000000,'2026-11-27 09:44:14',198.00,2,'STEAM-CN-20261127094409-6758','collect:STEAM-CN-20261127094409-6758','2026-11-27 09:44:14',1,1,NULL,NULL,'2026-11-27 09:44:14','2026-11-27 09:44:14'),(177,18,9,'CNY',198.00,198.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',198.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(178,19,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',298.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(179,20,9,'CNY',248.00,248.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',248.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(180,21,9,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',298.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(181,22,9,'CNY',248.00,248.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',248.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(182,23,9,'CNY',248.00,248.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',248.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(183,24,9,'CNY',208.00,208.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',208.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(184,25,9,'CNY',128.00,128.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',128.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(185,26,9,'CNY',95.20,238.00,60.00,NULL,1.000000,'2026-11-27 10:06:44',95.20,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(186,27,9,'CNY',68.00,68.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',68.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(187,28,9,'CNY',42.00,42.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',42.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(188,29,9,'CNY',39.00,39.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',39.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(189,30,9,'CNY',37.00,37.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',37.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(190,31,9,'CNY',39.00,39.00,0.00,NULL,1.000000,'2026-11-27 10:06:44',39.00,2,'STEAM-CN-20261127100633-9390','collect:STEAM-CN-20261127100633-9390','2026-11-27 10:06:44',1,1,NULL,NULL,'2026-11-27 10:06:44','2026-11-27 10:06:44'),(191,13,10,'CNY',149.00,149.00,0.00,NULL,1.000000,'2026-11-27 11:17:54',149.00,2,'EPIC-CN-20261127111741-5111','collect:EPIC-CN-20261127111741-5111','2026-11-27 11:17:54',1,1,NULL,NULL,'2026-11-27 11:17:54','2026-11-27 11:18:52'),(192,12,10,'CNY',198.00,198.00,0.00,NULL,1.000000,'2026-11-27 11:17:54',198.00,2,'EPIC-CN-20261127111741-5111','collect:EPIC-CN-20261127111741-5111','2026-11-27 11:17:54',1,1,NULL,NULL,'2026-11-27 11:17:54','2026-11-27 11:17:54'),(193,1,10,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-27 11:17:54',298.00,2,'EPIC-CN-20261127111741-5111','collect:EPIC-CN-20261127111741-5111','2026-11-27 11:17:54',1,1,NULL,NULL,'2026-11-27 11:17:54','2026-11-27 11:17:54'),(194,12,11,'CNY',39.60,198.00,80.00,NULL,1.000000,'2026-11-27 12:32:59',39.60,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(195,8,11,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',298.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(196,1,11,'CNY',89.40,298.00,70.00,NULL,1.000000,'2026-11-27 12:32:59',89.40,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(197,14,11,'CNY',148.00,148.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',148.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(198,15,11,'CNY',198.00,198.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',198.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(199,20,11,'CNY',248.00,248.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',248.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(200,21,11,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',298.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(201,22,11,'CNY',248.00,248.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',248.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(202,23,11,'CNY',248.00,248.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',248.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(203,24,11,'CNY',208.00,208.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',208.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(204,25,11,'CNY',128.00,128.00,0.00,NULL,1.000000,'2026-11-27 12:32:59',128.00,2,'XIAOHEIHE-CN-20261127123225-7978','collect:XIAOHEIHE-CN-20261127123225-7978','2026-11-27 12:32:59',1,1,NULL,NULL,'2026-11-27 12:32:59','2026-11-27 12:32:59'),(205,13,11,'CNY',98.60,224.00,56.00,NULL,1.000000,'2026-11-27 12:37:15',98.60,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(206,18,11,'CNY',198.00,198.00,0.00,NULL,1.000000,'2026-11-27 12:37:15',198.00,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(207,19,11,'CNY',298.00,298.00,0.00,NULL,1.000000,'2026-11-27 12:37:15',298.00,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(208,26,11,'CNY',95.20,238.00,60.00,NULL,1.000000,'2026-11-27 12:37:15',95.20,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(209,27,11,'CNY',68.00,68.00,0.00,NULL,1.000000,'2026-11-27 12:37:15',68.00,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(210,28,11,'CNY',42.00,42.00,0.00,NULL,1.000000,'2026-11-27 12:37:15',42.00,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(211,29,11,'CNY',39.00,39.00,0.00,NULL,1.000000,'2026-11-27 12:37:15',39.00,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(212,30,11,'CNY',37.00,37.00,0.00,NULL,1.000000,'2026-11-27 12:37:15',37.00,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15'),(213,31,11,'CNY',39.00,39.00,0.00,NULL,1.000000,'2026-11-27 12:37:15',39.00,2,'XIAOHEIHE-CN-20261127123636-3728','collect:XIAOHEIHE-CN-20261127123636-3728','2026-11-27 12:37:15',1,1,NULL,NULL,'2026-11-27 12:37:15','2026-11-27 12:37:15');
/*!40000 ALTER TABLE `price_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `region_risk`
--

DROP TABLE IF EXISTS `region_risk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `region_risk` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `platform_id` bigint unsigned NOT NULL COMMENT '逻辑关联 platform.id',
  `region` varchar(16) NOT NULL COMMENT '区域',
  `risk_level` tinyint NOT NULL DEFAULT '0' COMMENT '0无风险 1低 2中 3高（AC-6.2）',
  `reason` varchar(500) DEFAULT NULL COMMENT '风险原因（AC-6.2）',
  `source` varchar(200) DEFAULT NULL COMMENT '判定依据来源（AC-6.4）',
  `updated_by` varchar(64) DEFAULT NULL COMMENT '最后修改人（AC-6.4）',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_region` (`platform_id`,`region`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='锁区风险（Q5 未定时按平台+区域维护）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `region_risk`
--

LOCK TABLES `region_risk` WRITE;
/*!40000 ALTER TABLE `region_risk` DISABLE KEYS */;
INSERT INTO `region_risk` VALUES (1,1,'US',1,'需美区账号，部分支付方式受限','人工维护·Steam 商店区域说明','admin',0,'2026-11-21 12:34:04','2026-11-21 12:34:04'),(2,3,'US',3,'该区域常规渠道无法购买','人工维护·Xbox 区域政策','admin',0,'2026-11-21 12:34:04','2026-11-21 12:34:04');
/*!40000 ALTER TABLE `region_risk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(64) NOT NULL COMMENT '登录名，唯一',
  `password_hash` varchar(100) NOT NULL COMMENT 'BCrypt 密文（AC-8.6 不明文）',
  `nickname` varchar(64) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL COMMENT 'Q6 若支持邮箱注册再用',
  `phone` varchar(32) DEFAULT NULL COMMENT 'Q6 若支持手机注册再用',
  `role` tinyint NOT NULL DEFAULT '1' COMMENT '1普通用户 2管理员（AC-11.5）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0禁用',
  `failed_count` int NOT NULL DEFAULT '0' COMMENT '连续登录失败次数（AC-8.6 防爆破）',
  `locked_until` datetime DEFAULT NULL COMMENT '锁定到期时间（AC-8.6）',
  `last_login_time` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (2,'testuser','$2a$10$iAfhnu0cT3yJB3Ur7OeX0.IoIlxx1gLGT2/K63pALnSJtNmPkysOq','testuser',NULL,NULL,2,1,0,NULL,'2026-11-27 17:12:43',0,'2026-11-20 19:45:36','2026-11-27 17:12:43'),(5,'xym009621','$2a$10$hPgnC3H9DuFNSgkPS5QJj.FZ/KuPasXbfbKKT1mbbXRLgtKikWflq','origin',NULL,NULL,1,1,0,NULL,'2026-11-27 14:29:56',0,'2026-11-21 10:28:12','2026-11-27 14:29:56');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_game_min_price`
--

DROP TABLE IF EXISTS `v_game_min_price`;
/*!50001 DROP VIEW IF EXISTS `v_game_min_price`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_game_min_price` AS SELECT 
 1 AS `game_id`,
 1 AS `min_price_cny`,
 1 AS `platform_count`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping routines for database 'game_compara_price'
--

--
-- Final view structure for view `v_game_min_price`
--

/*!50001 DROP VIEW IF EXISTS `v_game_min_price`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_game_min_price` AS select `p`.`game_id` AS `game_id`,min(`p`.`cny_amount`) AS `min_price_cny`,count(distinct `p`.`platform_id`) AS `platform_count` from (`price_record` `p` join `platform` `pf` on(((`pf`.`id` = `p`.`platform_id`) and (`pf`.`enabled` = 1) and (`pf`.`api_ready` = 1) and (`pf`.`deleted` = 0)))) where ((`p`.`is_current` = 1) and (`p`.`status` = 1)) group by `p`.`game_id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-27 18:47:24
