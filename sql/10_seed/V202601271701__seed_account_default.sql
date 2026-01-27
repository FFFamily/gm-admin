-- 前台 Account（基础版）初始化数据
--
-- 默认账号（用于本地验证）：
--   username: user1
--   password: User@123456
--
-- 注意：该密码为固定默认密码，仅用于开发/测试环境；上线前请自行修改并重新生成 hash。

-- BCrypt hash for "User@123456" (generated offline)
INSERT INTO app_account (username, password_hash, display_name, enabled)
VALUES ('user1', '$2y$10$8JyENGHnZiWKng3sCLXMB.WhqIixy3wVRaY6ocUWk35cd3qFyBECa', 'User One', 1)
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  enabled = VALUES(enabled);

