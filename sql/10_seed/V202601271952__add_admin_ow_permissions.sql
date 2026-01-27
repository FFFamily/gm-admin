-- 管理后台权限补丁：OW（角斗领域/军械库模拟器）配置相关权限点
-- 说明：由于 ADMIN 角色在代码中“拥有全部权限点”，这里只需要把权限点写入 sys_permission 即可（无需额外绑定）。

INSERT INTO sys_permission (perm_code, perm_name)
VALUES
  ('ow:hero:list', 'OW 英雄-列表'),
  ('ow:hero:read', 'OW 英雄-详情'),
  ('ow:hero:create', 'OW 英雄-新增'),
  ('ow:hero:update', 'OW 英雄-编辑'),
  ('ow:hero:delete', 'OW 英雄-删除'),

  ('ow:item:list', 'OW 装备-列表'),
  ('ow:item:read', 'OW 装备-详情'),
  ('ow:item:create', 'OW 装备-新增'),
  ('ow:item:update', 'OW 装备-编辑'),
  ('ow:item:delete', 'OW 装备-删除'),
  ('ow:item:bind_heroes', 'OW 装备-绑定英雄'),

  ('ow:stat_def:list', 'OW 属性定义-列表'),
  ('ow:stat_def:update', 'OW 属性定义-编辑'),

  ('ow:config:read', 'OW 配置-读取'),
  ('ow:config:update', 'OW 配置-编辑')
ON DUPLICATE KEY UPDATE
  perm_name = VALUES(perm_name),
  updated_at = CURRENT_TIMESTAMP;

