# OW（角斗领域/军械库模拟器）死数据后端化方案

> 目标：将前端 `src/data` 下的 OW 静态数据迁移到后端（MySQL）存储，通过 API 动态下发，前端不再依赖“写死的 JS 数据”。

## 1. 现状盘点：前端数据来源与结构

### 1.1 数据文件位置

- 英雄属性与初始金币：
  - `web/gm-web-ui/src/data/ow/baseInfo.js`
- 军械库物品列表（商店列表）：
  - `web/gm-web-ui/src/data/storeItems.js`
- 图片资源与映射：
  - 图片：`web/gm-web-ui/src/assets/ow/*.jpg`
  - 映射：`web/gm-web-ui/src/assets/ow/imageMap.js`（根据 jpg 文件名生成 `{ [name]: url }`）

### 1.2 英雄属性配置数据结构（baseInfo.js）

1) `initialGold: number`

2) `heroStatConfigs: Array<HeroStatConfig>`

`HeroStatConfig` 当前字段：

- `key`: string（如 `weaponStrength`、`cooldownReduction`）
- `label`: string（中文展示名）
- `iconName`: string（Lucide 图标名字符串，如 `Sword`）
- `color`: string（Tailwind class，如 `text-blue-600`）
- `unit`: string（单位，目前为空字符串）
- `value`: number（默认值，目前都为 0）

3) `heroInfo: Record<string, number>`

由 `heroStatConfigs` reduce 得到：`{ [key]: value }`，作为英雄初始属性。

### 1.3 军械库物品数据结构（storeItems.js）

`storeItems: Array<StoreItem>`

`StoreItem` 当前字段（来自代码实际使用）：

- `id`: number（注意：当前文件里存在大量重复 id）
- `name`: string
- `price`: number
- `quality`: `'green' | 'blue' | 'purple'`
- `category`: `'weapon' | 'skill' | 'survival' | 'device'`（StoreComponent 中会兜底为 weapon）
- `img`: string（图片名称，不含路径；用于 `imageMap[item.img]`）
- `stats`: `Record<string, number>`（动态属性字典）

已发现的问题（迁移前建议先清洗/统一）：

- `id` 重复：例如多个条目 `id: 3` / `id: 5`，若直接入库会冲突。
- 字段不一致：部分条目把 `cooldown: 20` 写在根对象上，而非 `stats.cooldown`。
- `stats` key 不统一：
  - 英雄面板展示的 key 以 `heroStatConfigs.key` 为准（如 `cooldownReduction`、`weaponLifesteal`）
  - 但物品里出现 `damage`、`health`、`lifesteal`、`cooldown` 等 key；这些会被加到 `heroStats`，但不会出现在 HeroPanel（因为 HeroPanel 只渲染 `heroStatConfigs`）。

### 1.4 OW 页面如何消费这些数据

- `web/gm-web-ui/src/stores/gameStore.js`
  - `gold` 初始化来自 `initialGold`
  - `heroStatConfigs`、`heroStats` 初始化来自 `heroStatConfigs/heroInfo`
  - `storeItems` 来自 `data/storeItems.js` 并导出给组件用
- `web/gm-web-ui/src/views/ow/components/StoreComponent.vue`
  - 按 `category` + `quality` 分组展示
  - 悬浮提示展示 `item.stats` 的 key/value
  - 图片通过 `imageMap[item.img]` 显示

## 2. 后端化的核心拆分：哪些要入库、哪些可继续前端静态

建议把 OW 数据拆成 3 类：

1) **配置类（Config）**：`initialGold`、英雄属性定义（stat defs）
2) **内容类（Content）**：军械库物品（items）及其加成（stats）
3) **资源类（Assets）**：图片文件（jpg）

图片资产有两种方案（二选一即可）：

- 方案 A（最省事，推荐 MVP）：图片仍放前端 `assets/ow/*.jpg`，后端只存 `img_key`（= 文件名），前端继续用 `imageMap` 映射。
- 方案 B（更标准）：图片上传到后端/对象存储，后端返回 `img_url`，前端直接 `<img :src="imgUrl">`。

## 3. 建议的数据模型（MySQL 8）

> 目标：既能快速落地，又能支持后续扩展（新增品质、分类、属性、版本）。

### 3.1 英雄属性定义表（stat defs）

用途：驱动 HeroPanel 的展示（label/icon/color/unit/排序），并约束 items 的 `stats` key。

建议表：`ow_stat_def`

- `id` BIGINT PK
- `stat_key` VARCHAR(64) UNIQUE（对应前端 `key`，如 `weaponStrength`）
- `label` VARCHAR(64)
- `unit` VARCHAR(16)
- `is_percent` TINYINT(1)（是否按百分比展示，用于 tooltip 格式化）
- `icon_name` VARCHAR(64)
- `color_class` VARCHAR(64)
- `default_value` INT
- `sort_order` INT
- `enabled` TINYINT(1)
- `created_at` / `updated_at`

### 3.2 OW 配置表（initialGold 等）

建议表：`ow_config`

- `config_key` VARCHAR(64) PK（如 `initialGold`）
- `config_value` JSON（如 `80000` 或 `{...}`）
- `updated_at`

> 也可以直接把 `initialGold` 放到 `ow_config` 一行；将来扩展其它参数时无需改表。

### 3.3 军械库物品表（items）

建议表：`ow_store_item`

- `id` BIGINT PK（自增）
- `item_code` VARCHAR(64) UNIQUE（建议使用稳定编码：如 `weapon_lubricant`，不要用当前 JS 的重复 id）
- `name` VARCHAR(64)
- `price` INT
- `quality` VARCHAR(16)（green/blue/purple）
- `category` VARCHAR(16)（weapon/skill/survival/device）
- `img_key` VARCHAR(128)（方案 A：文件名；方案 B：可改为 img_url）
- `stats_json` JSON（存 `Record<string, number>`，例如 `{ "weaponStrength": 5 }`）
- `enabled` TINYINT(1)
- `sort_order` INT
- `created_at` / `updated_at`

为什么 `stats_json` 用 JSON：

- 目前 stats key 不固定，且后续可能新增属性；JSON 扩展成本最低。
- 若未来需要对某个属性做筛选/排序/统计，再追加一张“物品-属性明细表”做索引化即可。

## 4. API 设计（前端获取动态数据）

> MVP：只需要 “读取接口”，先把死数据替换掉即可。

### 4.1 前台接口（公开/或需登录都可，看产品）

1) 获取 OW 配置 + 英雄属性定义
- `GET /api/ow/config`
- 返回示例：
```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "initialGold": 80000,
    "heroStatConfigs": [
      { "key": "weaponStrength", "label": "武器强度", "unit": "", "iconName": "Sword", "color": "text-blue-600", "defaultValue": 0, "isPercent": false, "sortOrder": 10 }
    ]
  }
}
```

2) 获取军械库物品列表
- `GET /api/ow/items`
- Query：
  - `category`（可选）
  - `quality`（可选）
- 返回示例：
```json
{
  "code": 0,
  "message": "OK",
  "data": [
    {
      "itemCode": "weapon_lubricant",
      "name": "武器润滑油",
      "price": 1000,
      "quality": "green",
      "category": "weapon",
      "imgKey": "武器润滑油",
      "stats": { "attackSpeed": 5 }
    }
  ]
}
```

### 4.2 后台接口（可选，用于管理配置/物品）

等你要“后台可维护”时再加：

- `POST/PUT/DELETE /api/admin/ow/items`
- `POST/PUT /api/admin/ow/stat-defs`
- `POST/PUT /api/admin/ow/config`

并在权限体系里新增 `ow:*` 权限点。

## 5. 前端改造点（从死数据切到 API）

### 5.1 替换 data 导入

当前：`gameStore.js` 直接 import `storeItems`、`initialGold`、`heroStatConfigs`。

改造后建议：

1) 新增 API 文件：`web/gm-web-ui/src/api/ow.js`
- `apiOwConfig()`
- `apiOwItems()`

2) 新增一个 owStore（Pinia 或 reactive 均可）
- 缓存 `initialGold`、`heroStatConfigs`、`storeItems`
- 提供 `bootstrap()`：页面进入 OW 时拉取并填充

3) `gameStore` 初始化方式调整：
- `gold`、`heroStatConfigs`、`heroStats` 在 `owStore.bootstrap()` 后再初始化/重置
- `storeItems` 由接口返回并注入到 StoreComponent（不再从 `data/storeItems.js` import）

### 5.2 图片处理

方案 A（推荐 MVP）：

- 后端返回 `imgKey`
- 前端继续用 `imageMap[imgKey]` 映射到本地图片 URL
- 不需要改动资源部署逻辑

方案 B：

- 后端返回 `imgUrl`
- 前端直接 `<img :src="imgUrl">`
- 需要：图片上传/静态资源服务/或 OSS/CDN

## 6. 数据迁移与清洗步骤（建议顺序）

1) 先确定“属性 key 口径”
- 以 `heroStatConfigs.key` 为权威口径（推荐）
- 将 `storeItems.stats` 中的 key 对齐（比如把 `cooldown` 改为 `cooldownReduction` 等）
- 或者扩展 `heroStatConfigs` 增加 `damage/health/lifesteal` 等，保证英雄面板能展示

2) 修正 `storeItems` 的结构问题
- 将根字段 `cooldown` 统一移动到 `stats.cooldown`（或对齐后的 key）
- 为每个 item 生成唯一 `item_code`（避免重复 id）

3) 新建 SQL 表 + seed 脚本
- `sql/00_schema/`：建表脚本（ow_stat_def、ow_config、ow_store_item）
- `sql/10_seed/`：把当前 JS 数据灌入

4) 后端新增只读接口
- `GET /api/ow/config`
- `GET /api/ow/items`

5) 前端改造 `gameStore` 与 OW 页面 bootstrap 流程
- OW 页面进入时拉取数据
- 替换原 `import` 的静态数据

## 7. 你可以如何落地（最小可行实现 MVP）

如果你想“最快从死数据变成后端数据”：

1) 先做表 `ow_store_item`（stats 用 JSON），只做 `GET /api/ow/items`
2) 前端把 `storeItems` 改为接口返回
3) `initialGold` 与 `heroStatConfigs` 暂时仍保留前端静态
4) 第二期再把 `initialGold/heroStatConfigs` 也后端化

> 这样能用最小改动把核心“物品列表”从死数据切到后端。

