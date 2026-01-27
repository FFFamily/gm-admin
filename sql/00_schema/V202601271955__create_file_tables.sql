-- 文件上传/管理：文件元数据表
-- 执行库：MySQL 8.x
-- 字符集：utf8mb4

CREATE TABLE IF NOT EXISTS sys_file (
  id                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  biz                VARCHAR(64)   NOT NULL,
  original_name      VARCHAR(255)  NOT NULL,
  stored_name        VARCHAR(255)  NOT NULL,
  ext                VARCHAR(32)   NULL,
  content_type       VARCHAR(128)  NULL,
  size_bytes         BIGINT        NOT NULL DEFAULT 0,
  storage_path       VARCHAR(255)  NOT NULL,
  url                VARCHAR(255)  NOT NULL,
  deleted            TINYINT(1)    NOT NULL DEFAULT 0,
  deleted_at         DATETIME      NULL,
  created_by_user_id BIGINT UNSIGNED NULL,
  created_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sys_file_biz_created_at (biz, created_at),
  KEY idx_sys_file_deleted_created_at (deleted, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

