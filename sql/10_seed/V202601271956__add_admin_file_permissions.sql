-- 管理后台权限补丁：文件上传/管理相关权限点
-- 说明：由于 ADMIN 角色在代码中“拥有全部权限点”，这里只需要把权限点写入 sys_permission 即可（无需额外绑定）。

INSERT INTO sys_permission (perm_code, perm_name)
VALUES
  ('file:upload', '文件-上传'),
  ('file:list', '文件-列表'),
  ('file:delete', '文件-删除')
ON DUPLICATE KEY UPDATE
  perm_name = VALUES(perm_name),
  updated_at = CURRENT_TIMESTAMP;

