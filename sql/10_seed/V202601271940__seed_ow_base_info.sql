-- OW（角斗领域/军械库模拟器）初始化数据：基础配置 + 英雄属性定义
-- 依赖：已执行 `sql/00_schema/V202601271900__create_ow_hero_item_tables.sql`

-- 0) 初始金币（对应前端：web/gm-web-ui/src/data/ow/baseInfo.js -> initialGold）
INSERT INTO ow_config (config_key, config_value)
VALUES ('initial_gold', JSON_OBJECT('value', 80000))
ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value),
  updated_at = CURRENT_TIMESTAMP;

-- 1) 英雄属性定义（对应前端：web/gm-web-ui/src/data/ow/baseInfo.js -> heroStatConfigs）
INSERT INTO ow_stat_def (
  stat_key, label, unit, is_percent, icon_name, color_class, default_value, sort_order, enabled
)
VALUES
  ('weaponStrength',     '武器强度',     '', 0, 'Sword',      'text-blue-600',   0,  1, 1),
  ('skillStrength',      '技能强度',     '', 0, 'Sparkles',   'text-purple-600', 0,  2, 1),
  ('attackSpeed',        '攻击速度',     '', 0, 'Gauge',      'text-orange-600', 0,  3, 1),
  ('cooldownReduction',  '冷却缩减',     '', 0, 'Shield',     'text-cyan-600',   0,  4, 1),
  ('maxAmmo',            '最大弹药',     '', 0, 'Circle',     'text-yellow-600', 0,  5, 1),
  ('weaponLifesteal',    '武器生命偷取', '', 0, 'Droplet',    'text-pink-600',   0,  6, 1),
  ('skillLifesteal',     '技能生命偷取', '', 0, 'Heart',      'text-red-600',    0,  7, 1),
  ('moveSpeed',          '移动速度',     '', 0, 'Zap',        'text-green-600',  0,  8, 1),
  ('reloadSpeed',        '装填速度',     '', 0, 'RefreshCw',  'text-indigo-600', 0,  9, 1),
  ('meleeDamage',        '近身伤害',     '', 0, 'Hand',       'text-amber-600',  0, 10, 1),
  ('criticalDamage',     '暴击伤害',     '', 0, 'Target',     'text-rose-600',   0, 11, 1)
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

