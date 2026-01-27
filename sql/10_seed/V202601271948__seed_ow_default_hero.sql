-- OW（角斗领域/军械库模拟器）初始化数据：默认英雄（用于前台“先选英雄”流程）
-- 依赖：已执行 `sql/00_schema/V202601271900__create_ow_hero_item_tables.sql`

INSERT INTO ow_hero (
  hero_code, hero_name, description, avatar_key, avatar_url, initial_gold, base_stats_json, enabled, sort_order
)
VALUES (
  'default',
  '通用英雄',
  '默认英雄（占位）。可在后台新增/编辑真实英雄，并禁用该占位英雄。',
  NULL,
  NULL,
  NULL,
  JSON_OBJECT(
    'weaponStrength', 0,
    'skillStrength', 0,
    'attackSpeed', 0,
    'cooldownReduction', 0,
    'maxAmmo', 0,
    'weaponLifesteal', 0,
    'skillLifesteal', 0,
    'moveSpeed', 0,
    'reloadSpeed', 0,
    'meleeDamage', 0,
    'criticalDamage', 0
  ),
  1,
  1
)
ON DUPLICATE KEY UPDATE
  hero_name = VALUES(hero_name),
  description = VALUES(description),
  avatar_key = VALUES(avatar_key),
  avatar_url = VALUES(avatar_url),
  initial_gold = VALUES(initial_gold),
  base_stats_json = VALUES(base_stats_json),
  enabled = VALUES(enabled),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

