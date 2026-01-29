-- OW（军械库社区）互动表：点赞/收藏
-- 依赖：
--   - `sql/00_schema/V202601291430__create_ow_loadout_tables.sql`
--   - `sql/00_schema/V202601271700__create_account_tables.sql`

CREATE TABLE IF NOT EXISTS ow_loadout_like (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  loadout_id BIGINT UNSIGNED NOT NULL,
  account_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_loadout_like_loadout_account (loadout_id, account_id),
  KEY idx_ow_loadout_like_account_created_at (account_id, created_at),
  KEY idx_ow_loadout_like_loadout_created_at (loadout_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_loadout_favorite (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  loadout_id BIGINT UNSIGNED NOT NULL,
  account_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_loadout_fav_loadout_account (loadout_id, account_id),
  KEY idx_ow_loadout_fav_account_created_at (account_id, created_at),
  KEY idx_ow_loadout_fav_loadout_created_at (loadout_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

