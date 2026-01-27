-- OW（角斗领域/军械库模拟器）后端化：英雄与装备配置表
-- 执行库：MySQL 8.x
-- 字符集：utf8mb4

CREATE TABLE IF NOT EXISTS ow_stat_def (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  stat_key      VARCHAR(64)  NOT NULL,
  label         VARCHAR(64)  NOT NULL,
  unit          VARCHAR(16)  NOT NULL DEFAULT '',
  is_percent    TINYINT(1)   NOT NULL DEFAULT 0,
  icon_name     VARCHAR(64)  NULL,
  color_class   VARCHAR(64)  NULL,
  default_value INT          NOT NULL DEFAULT 0,
  sort_order    INT          NOT NULL DEFAULT 0,
  enabled       TINYINT(1)   NOT NULL DEFAULT 1,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_stat_def_key (stat_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_config (
  config_key    VARCHAR(64) NOT NULL,
  config_value  JSON        NOT NULL,
  updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_hero (
  id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  hero_code      VARCHAR(64)  NOT NULL,
  hero_name      VARCHAR(64)  NOT NULL,
  description    VARCHAR(255) NULL,
  avatar_key     VARCHAR(128) NULL,
  avatar_url     VARCHAR(255) NULL,
  initial_gold   INT          NULL,
  base_stats_json JSON        NOT NULL,
  enabled        TINYINT(1)   NOT NULL DEFAULT 1,
  sort_order     INT          NOT NULL DEFAULT 0,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_hero_code (hero_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_item (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  item_code   VARCHAR(64)  NOT NULL,
  item_name   VARCHAR(64)  NOT NULL,
  price       INT          NOT NULL DEFAULT 0,
  quality     VARCHAR(16)  NOT NULL,
  category    VARCHAR(16)  NOT NULL,
  img_key     VARCHAR(128) NULL,
  img_url     VARCHAR(255) NULL,
  stats_json  JSON         NOT NULL,
  is_global   TINYINT(1)   NOT NULL DEFAULT 1,
  enabled     TINYINT(1)   NOT NULL DEFAULT 1,
  sort_order  INT          NOT NULL DEFAULT 0,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_item_code (item_code),
  KEY idx_ow_item_category_quality (category, quality),
  KEY idx_ow_item_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_hero_item (
  id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  hero_id    BIGINT UNSIGNED NOT NULL,
  item_id    BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_hero_item (hero_id, item_id),
  KEY idx_ow_hero_item_hero_id (hero_id),
  KEY idx_ow_hero_item_item_id (item_id),
  CONSTRAINT fk_ow_hero_item_hero FOREIGN KEY (hero_id) REFERENCES ow_hero (id) ON DELETE CASCADE,
  CONSTRAINT fk_ow_hero_item_item FOREIGN KEY (item_id) REFERENCES ow_item (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

