-- OW「军械库社区」：装备清单（分享）表
-- 依赖：已执行 OW 基础表 `sql/00_schema/V202601271900__create_ow_hero_item_tables.sql`
-- 执行库：MySQL 8.x
-- 字符集：utf8mb4

CREATE TABLE IF NOT EXISTS ow_loadout (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  hero_code            VARCHAR(64)  NOT NULL,
  title                VARCHAR(128) NOT NULL,
  description          VARCHAR(512) NULL,
  items_json           JSON         NOT NULL,
  created_by_account_id BIGINT UNSIGNED NOT NULL,
  created_by_name      VARCHAR(64)  NOT NULL,
  status               VARCHAR(16)  NOT NULL DEFAULT 'PUBLISHED',
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ow_loadout_hero_created_at (hero_code, created_at),
  KEY idx_ow_loadout_status_created_at (status, created_at),
  KEY idx_ow_loadout_account_created_at (created_by_account_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

