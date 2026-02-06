-- OW「组队 / 邀请入队」：LFG(looking-for-group) 组队表
-- 依赖：已执行 Account 表 `sql/00_schema/V202601271700__create_account_tables.sql`
-- 执行库：MySQL 8.x
-- 字符集：utf8mb4

CREATE TABLE IF NOT EXISTS ow_lfg_team (
  id                       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title                    VARCHAR(128) NOT NULL,
  mode_code                VARCHAR(32)  NOT NULL,
  platform_code            VARCHAR(16)  NOT NULL,
  allow_crossplay          TINYINT      NOT NULL DEFAULT 0,

  capacity                 INT          NOT NULL,
  member_count             INT          NOT NULL DEFAULT 1,
  auto_approve             TINYINT      NOT NULL DEFAULT 1,

  region_code              VARCHAR(32)  NULL,
  language_code            VARCHAR(32)  NULL,
  voice_required           TINYINT      NOT NULL DEFAULT 1,

  rank_min                 VARCHAR(16)  NULL,
  rank_max                 VARCHAR(16)  NULL,
  need_roles_json          JSON         NULL,
  preferred_hero_codes_json JSON        NULL,
  tags_json                JSON         NULL,

  note                     VARCHAR(512) NULL,
  contact_json             JSON         NULL,

  invite_code              VARCHAR(32)  NOT NULL,
  status                   VARCHAR(16)  NOT NULL DEFAULT 'OPEN',
  expires_at               DATETIME     NOT NULL,

  created_by_account_id    BIGINT UNSIGNED NOT NULL,
  created_by_name          VARCHAR(64)  NOT NULL,

  created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_lfg_team_invite_code (invite_code),
  KEY idx_ow_lfg_team_status_expires_at (status, expires_at),
  KEY idx_ow_lfg_team_creator_created_at (created_by_account_id, created_at),
  KEY idx_ow_lfg_team_mode_created_at (mode_code, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_lfg_team_member (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  team_id       BIGINT UNSIGNED NOT NULL,
  account_id    BIGINT UNSIGNED NOT NULL,
  display_name  VARCHAR(64)  NOT NULL,
  role_tags_json JSON        NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'JOINED',
  joined_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  left_at       DATETIME     NULL,

  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_lfg_member_team_account (team_id, account_id),
  KEY idx_ow_lfg_member_team_status_joined_at (team_id, status, joined_at),
  KEY idx_ow_lfg_member_account_joined_at (account_id, joined_at),
  CONSTRAINT fk_ow_lfg_member_team_id FOREIGN KEY (team_id) REFERENCES ow_lfg_team(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_lfg_team_join_request (
  id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  team_id      BIGINT UNSIGNED NOT NULL,
  account_id   BIGINT UNSIGNED NOT NULL,
  display_name VARCHAR(64)  NOT NULL,
  message      VARCHAR(140) NULL,
  status       VARCHAR(16)  NOT NULL DEFAULT 'PENDING',

  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  UNIQUE KEY uk_ow_lfg_join_req_team_account (team_id, account_id),
  KEY idx_ow_lfg_join_req_team_status_created_at (team_id, status, created_at),
  CONSTRAINT fk_ow_lfg_join_req_team_id FOREIGN KEY (team_id) REFERENCES ow_lfg_team(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ow_lfg_report (
  id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reporter_account_id BIGINT UNSIGNED NOT NULL,
  target_type         VARCHAR(16)  NOT NULL,
  target_id           BIGINT UNSIGNED NOT NULL,
  reason              VARCHAR(64)  NOT NULL,
  detail              VARCHAR(512) NULL,
  snapshot_json       JSON         NULL,

  created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  KEY idx_ow_lfg_report_target_created_at (target_type, target_id, created_at),
  KEY idx_ow_lfg_report_reporter_created_at (reporter_account_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

