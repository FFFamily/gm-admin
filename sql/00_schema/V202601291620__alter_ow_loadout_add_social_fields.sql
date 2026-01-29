-- OW（军械库社区）表结构补丁：增加互动/运营字段
-- 依赖：已执行 `sql/00_schema/V202601291430__create_ow_loadout_tables.sql`
-- 说明：手工执行；脚本可重复执行（通过 information_schema 判断列是否存在）。

-- view_count
SET @col := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND column_name = 'view_count'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE ow_loadout ADD COLUMN view_count INT UNSIGNED NOT NULL DEFAULT 0 AFTER status',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- like_count
SET @col := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND column_name = 'like_count'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE ow_loadout ADD COLUMN like_count INT UNSIGNED NOT NULL DEFAULT 0 AFTER view_count',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- favorite_count
SET @col := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND column_name = 'favorite_count'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE ow_loadout ADD COLUMN favorite_count INT UNSIGNED NOT NULL DEFAULT 0 AFTER like_count',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- is_featured
SET @col := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND column_name = 'is_featured'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE ow_loadout ADD COLUMN is_featured TINYINT(1) NOT NULL DEFAULT 0 AFTER favorite_count',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- featured_at
SET @col := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND column_name = 'featured_at'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE ow_loadout ADD COLUMN featured_at DATETIME NULL AFTER is_featured',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- is_pinned
SET @col := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND column_name = 'is_pinned'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE ow_loadout ADD COLUMN is_pinned TINYINT(1) NOT NULL DEFAULT 0 AFTER featured_at',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- pinned_at
SET @col := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND column_name = 'pinned_at'
);
SET @sql := IF(@col = 0,
  'ALTER TABLE ow_loadout ADD COLUMN pinned_at DATETIME NULL AFTER is_pinned',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- indexes for sorting
SET @idx := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND index_name = 'idx_ow_loadout_status_pinned_featured_id'
);
SET @sql := IF(@idx = 0,
  'CREATE INDEX idx_ow_loadout_status_pinned_featured_id ON ow_loadout(status, is_pinned, is_featured, id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'ow_loadout'
    AND index_name = 'idx_ow_loadout_status_view_id'
);
SET @sql := IF(@idx = 0,
  'CREATE INDEX idx_ow_loadout_status_view_id ON ow_loadout(status, view_count, id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

