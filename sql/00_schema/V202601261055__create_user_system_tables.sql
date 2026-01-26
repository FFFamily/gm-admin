-- 用户体系（基础版）建表脚本
-- 执行库：MySQL 8.x
-- 字符集：utf8mb4

CREATE TABLE IF NOT EXISTS sys_user (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username        VARCHAR(64)  NOT NULL,
  password_hash   VARCHAR(100) NOT NULL,
  display_name    VARCHAR(64)  NOT NULL DEFAULT '',
  enabled         TINYINT(1)   NOT NULL DEFAULT 1,
  last_login_at   DATETIME     NULL,
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_code   VARCHAR(64)  NOT NULL,
  role_name   VARCHAR(64)  NOT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_permission (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  perm_code   VARCHAR(128) NOT NULL,
  perm_name   VARCHAR(128) NOT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_permission_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  role_id     BIGINT UNSIGNED NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_role (user_id, role_id),
  KEY idx_sys_user_role_user_id (user_id),
  KEY idx_sys_user_role_role_id (role_id),
  CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
  CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_id     BIGINT UNSIGNED NOT NULL,
  perm_id     BIGINT UNSIGNED NOT NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_permission (role_id, perm_id),
  KEY idx_sys_role_permission_role_id (role_id),
  KEY idx_sys_role_permission_perm_id (perm_id),
  CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
  CONSTRAINT fk_sys_role_permission_perm FOREIGN KEY (perm_id) REFERENCES sys_permission (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_login_log (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username    VARCHAR(64)  NOT NULL,
  success     TINYINT(1)   NOT NULL,
  reason      VARCHAR(255) NULL,
  ip          VARCHAR(45)  NULL,
  ua          VARCHAR(255) NULL,
  created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sys_login_log_username_created_at (username, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_audit_log (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_user_id BIGINT UNSIGNED NULL,
  action        VARCHAR(64)  NOT NULL,
  target_type   VARCHAR(64)  NOT NULL,
  target_id     VARCHAR(64)  NULL,
  detail_json   JSON         NULL,
  result        VARCHAR(32)  NOT NULL,
  ip            VARCHAR(45)  NULL,
  ua            VARCHAR(255) NULL,
  request_id    VARCHAR(64)  NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sys_audit_log_actor_created_at (actor_user_id, created_at),
  KEY idx_sys_audit_log_action_created_at (action, created_at),
  KEY idx_sys_audit_log_target_created_at (target_type, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

