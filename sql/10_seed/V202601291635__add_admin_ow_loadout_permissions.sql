-- 管理后台权限补丁：OW（军械库社区）清单治理权限点
-- 说明：由于 ADMIN 角色在代码中“拥有全部权限点”，这里只需要把权限点写入 sys_permission 即可（无需额外绑定）。

INSERT INTO sys_permission (perm_code, perm_name)
VALUES
  ('ow:loadout:list', 'OW 清单-列表'),
  ('ow:loadout:read', 'OW 清单-详情'),
  ('ow:loadout:update_status', 'OW 清单-上下架'),
  ('ow:loadout:feature', 'OW 清单-精选'),
  ('ow:loadout:pin', 'OW 清单-置顶')
ON DUPLICATE KEY UPDATE
  perm_name = VALUES(perm_name),
  updated_at = CURRENT_TIMESTAMP;

