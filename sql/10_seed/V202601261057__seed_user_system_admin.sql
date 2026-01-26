-- 用户体系（基础版）初始化数据
--
-- 默认管理员账号：
--   username: admin
--   password: Admin@123456
--
-- 注意：该密码为固定默认密码，仅用于初始化；上线前请自行修改并重新生成 hash。

-- 1) 内置角色：ADMIN
INSERT INTO sys_role (role_code, role_name)
VALUES ('ADMIN', '超级管理员')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 2) 内置权限点（最小集合）
INSERT INTO sys_permission (perm_code, perm_name) VALUES
  ('auth:login', '登录'),

  ('user:list', '用户列表'),
  ('user:read', '用户详情'),
  ('user:create', '新增用户'),
  ('user:update', '编辑用户'),
  ('user:reset_password', '重置用户密码'),

  ('role:list', '角色列表'),
  ('role:read', '角色详情'),
  ('role:create', '新增角色'),
  ('role:update', '编辑角色'),
  ('role:delete', '删除角色'),
  ('role:bind_permissions', '角色绑定权限'),

  ('permission:list', '权限点列表'),
  ('audit:list', '审计日志列表')
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 3) 初始化管理员账号
-- BCrypt hash for "Admin@123456" (generated offline)
INSERT INTO sys_user (username, password_hash, display_name, enabled)
VALUES ('admin', '$2a$10$8kxlcagjUOSai.GB/fIfyuZOG9BOOh11QVWcvVX0vYAEl09SfWmbS', 'Administrator', 1)
ON DUPLICATE KEY UPDATE
  display_name = VALUES(display_name),
  enabled = VALUES(enabled);

-- 4) 绑定：admin -> ADMIN
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 5) 绑定：ADMIN -> 所有权限点
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE perm_id = perm_id;

