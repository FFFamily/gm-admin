-- OW（军械库社区）初始化数据：示例装备清单
-- 依赖：
--   - 已执行 `sql/00_schema/V202601291430__create_ow_loadout_tables.sql`
--   - 已执行 `sql/00_schema/V202601271700__create_account_tables.sql`（用于 app_account）
--   - 已执行 `sql/10_seed/V202601271701__seed_account_default.sql`（用于默认账号 user1）
--   - 已执行 `sql/10_seed/V202601271948__seed_ow_default_hero.sql`（hero_code=default）
--   - 已执行 `sql/10_seed/V202601271945__seed_ow_store_items.sql`（示例 item_code）
--
-- 说明：本脚本仅用于本地演示/联调；可重复执行（使用 NOT EXISTS 防止重复插入）。

-- 1) 示例清单：新手武器流
INSERT INTO ow_loadout (
  hero_code,
  title,
  description,
  items_json,
  created_by_account_id,
  created_by_name,
  status
)
SELECT
  'default',
  '新手武器流（示例）',
  '适合入门的通用武器搭配。',
  JSON_ARRAY('item_0001', 'item_0003', 'item_0007'),
  a.id,
  COALESCE(NULLIF(TRIM(a.display_name), ''), a.username),
  'PUBLISHED'
FROM app_account a
WHERE a.username = 'user1'
  AND NOT EXISTS (
    SELECT 1
    FROM ow_loadout l
    WHERE l.created_by_account_id = a.id
      AND l.title = '新手武器流（示例）'
      AND l.hero_code = 'default'
  );

-- 2) 示例清单：冷却缩减流
INSERT INTO ow_loadout (
  hero_code,
  title,
  description,
  items_json,
  created_by_account_id,
  created_by_name,
  status
)
SELECT
  'default',
  '冷却缩减流（示例）',
  '偏技能释放频率的通用搭配思路。',
  JSON_ARRAY('item_0005', 'item_0006', 'item_0017', 'item_0020'),
  a.id,
  COALESCE(NULLIF(TRIM(a.display_name), ''), a.username),
  'PUBLISHED'
FROM app_account a
WHERE a.username = 'user1'
  AND NOT EXISTS (
    SELECT 1
    FROM ow_loadout l
    WHERE l.created_by_account_id = a.id
      AND l.title = '冷却缩减流（示例）'
      AND l.hero_code = 'default'
  );

