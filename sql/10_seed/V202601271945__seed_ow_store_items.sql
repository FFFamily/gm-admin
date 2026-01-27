-- OW（角斗领域/军械库模拟器）初始化数据：军械库（全局）物品
-- 说明：由前端静态数据 `web/gm-web-ui/src/data/storeItems.js` 转写而来
-- 依赖：已执行 `sql/00_schema/V202601271900__create_ow_hero_item_tables.sql`

-- 物品唯一标识使用 item_code（不使用前端静态 id：静态数据存在重复 id）
-- img_key 直接存放前端 imageMap 的 key（后续你会在后台补全图片）

INSERT INTO ow_item (
  item_code, item_name, price, quality, category, img_key, stats_json, is_global, enabled, sort_order
)
VALUES
  ('item_0001', '武器润滑油',     1000, 'green',  'weapon',  '武器润滑油',     JSON_OBJECT('attackSpeed', 5), 1, 1,  1),
  ('item_0002', '等离子转换器',   1000, 'green',  'weapon',  '等离子转换器',   JSON_OBJECT('weaponLifesteal', 10), 1, 1,  2),
  ('item_0003', '补偿器',         1000, 'green',  'weapon',  '补偿器',         JSON_OBJECT('weaponStrength', 5), 1, 1,  3),
  ('item_0004', '弹药储备',       1000, 'green',  'weapon',  '弹药储备',       JSON_OBJECT('maxAmmo', 20), 1, 1,  4),
  ('item_0005', '狂热增幅器',     1000, 'green',  'weapon',  '狂热增幅器',     JSON_OBJECT('cooldown', 5), 1, 1,  5),
  ('item_0006', '平流层信标',     1000, 'green',  'weapon',  '平流层信标',     JSON_OBJECT('cooldown', 5), 1, 1,  6),

  ('item_0007', '市售撞针',       3750, 'blue',   'weapon',  '市售撞针',       JSON_OBJECT('damage', 25), 1, 1,  7),
  ('item_0008', '军火储备',       4000, 'blue',   'weapon',  '军火储备',       JSON_OBJECT('damage', 8), 1, 1,  8),
  ('item_0009', '护盾瓦解器',     4000, 'blue',   'weapon',  '护盾瓦解器',     JSON_OBJECT('damage', 8), 1, 1,  9),
  ('item_0010', '高级纳米生物',   4500, 'blue',   'weapon',  '高级纳米生物',   JSON_OBJECT('damage', 8), 1, 1, 10),
  ('item_0011', '汲血技术',       4500, 'blue',   'weapon',  '汲血技术',       JSON_OBJECT('damage', 8), 1, 1, 11),
  ('item_0012', '极寒冷却剂',     5500, 'blue',   'weapon',  '极寒冷却剂',     JSON_OBJECT('damage', 8), 1, 1, 12),
  ('item_0013', '黑爪改造模块',   6000, 'blue',   'weapon',  '黑爪改造模块',   JSON_OBJECT('damage', 8), 1, 1, 13),
  ('item_0014', '应急芯片',       4500, 'blue',   'weapon',  '应急芯片',       JSON_OBJECT('damage', 8), 1, 1, 14),

  ('item_0015', '代码破译器',     1200, 'purple', 'weapon',  '代码破译器',     JSON_OBJECT('damage', 50), 1, 1, 15),
  ('item_0016', '可回收弹头',     1100, 'purple', 'weapon',  '可回收弹头',     JSON_OBJECT('health', 120), 1, 1, 16),
  ('item_0017', '沃斯卡娅军械',   1000, 'purple', 'weapon',  '沃斯卡娅军械',   JSON_OBJECT('cooldown', 20), 1, 1, 17),
  ('item_0018', '指挥官的弹夹',   1500, 'purple', 'weapon',  '指挥官的弹夹',   JSON_OBJECT('damage', 40, 'lifesteal', 15), 1, 1, 18),
  ('item_0019', '武器干扰弹',     1500, 'purple', 'weapon',  '武器干扰弹',     JSON_OBJECT('damage', 40, 'lifesteal', 15), 1, 1, 19),
  ('item_0020', '助推喷气背包',   1000, 'purple', 'weapon',  '助推喷气背包',   JSON_OBJECT('cooldown', 20), 1, 1, 20),
  ('item_0021', '艾玛莉的解毒剂', 1500, 'purple', 'weapon',  '艾玛莉的解毒剂', JSON_OBJECT('damage', 40, 'lifesteal', 15, 'cooldown', 20), 1, 1, 21),
  ('item_0022', '闪电部队抑制器', 1500, 'purple', 'weapon',  '闪电部队抑制器', JSON_OBJECT('damage', 40, 'lifesteal', 15, 'cooldown', 20), 1, 1, 22),
  ('item_0023', '高强度光子加速器',1500,'purple', 'weapon',  '高强度光子加速器',JSON_OBJECT('damage', 40, 'lifesteal', 15, 'cooldown', 20), 1, 1, 23),
  ('item_0024', '断魂曲',         1500, 'purple', 'weapon',  '断魂曲',         JSON_OBJECT('damage', 40, 'lifesteal', 15, 'cooldown', 20), 1, 1, 24),
  ('item_0025', '蜘蛛之眼',       1500, 'purple', 'weapon',  '蜘蛛之眼',       JSON_OBJECT('damage', 40, 'lifesteal', 15, 'cooldown', 20), 1, 1, 25),
  ('item_0026', '空域克星',       1500, 'purple', 'weapon',  '空域克星',       JSON_OBJECT('damage', 40, 'lifesteal', 15, 'cooldown', 20), 1, 1, 26)
ON DUPLICATE KEY UPDATE
  item_name = VALUES(item_name),
  price = VALUES(price),
  quality = VALUES(quality),
  category = VALUES(category),
  img_key = VALUES(img_key),
  stats_json = VALUES(stats_json),
  is_global = VALUES(is_global),
  enabled = VALUES(enabled),
  sort_order = VALUES(sort_order),
  updated_at = CURRENT_TIMESTAMP;

