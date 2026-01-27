-- Add admin permissions for managing front-end accounts (app_account)
-- Idempotent: safe to run multiple times.

-- 1) Insert permission codes
INSERT INTO sys_permission (perm_code, perm_name) VALUES
  ('account:list', '前台用户列表'),
  ('account:read', '前台用户详情'),
  ('account:update', '前台用户编辑'),
  ('account:reset_password', '前台用户重置密码')
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 2) Grant to ADMIN role
INSERT INTO sys_role_permission (role_id, perm_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.perm_code IN (
  'account:list',
  'account:read',
  'account:update',
  'account:reset_password'
)
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE perm_id = perm_id;

