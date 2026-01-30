-- OW 初始化数据补丁：补齐装备（item）属性定义（用于中文显示/下拉选择）
-- 依赖：已执行 `sql/00_schema/V202601271900__create_ow_hero_item_tables.sql`
--
-- 说明：
-- - `ow_stat_def` 既可用于英雄属性定义，也可用于装备属性 keys 的统一字典。
-- - 前端显示与后台下拉选择将优先使用该字典的 label。

INSERT INTO ow_stat_def (
  stat_key, label, unit, is_percent, icon_name, color_class, default_value, sort_order, enabled
)
VALUES
  ('damage',   '伤害',     '%', 1, 'Crosshair', 'text-red-600',  0, 101, 1),
  ('cooldown', '冷却缩减', '%', 1, 'Timer',     'text-cyan-600', 0, 102, 1),
  ('health',   '生命值',   '',  0, 'Heart',     'text-pink-600', 0, 103, 1),
  ('lifesteal','吸血',     '%', 1, 'Droplet',   'text-rose-600', 0, 104, 1)
ON DUPLICATE KEY UPDATE
  label = VALUES(label),
  unit = VALUES(unit),
  is_percent = VALUES(is_percent),
  icon_name = VALUES(icon_name),
  color_class = VALUES(color_class),
  default_value = VALUES(default_value),
  sort_order = VALUES(sort_order),
  enabled = VALUES(enabled),
  updated_at = CURRENT_TIMESTAMP;

