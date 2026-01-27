# OW（角斗领域/军械库模拟器）英雄与装备后台配置方案（补充需求版）

> 本文是在 `doc/OW死数据后端化方案.md` 的基础上，补充“先选英雄→再选装备”“英雄初始属性可配置”“装备可绑定英雄专属”“装备属性/名称/图片可配置”等需求的落地方案。

## 1. 新增业务逻辑（需求澄清后的流程）

1) 用户进入 OW 页面
2) 必须先选择一个英雄（Hero）
3) 选中英雄后：
   - 初始化金币（可全局默认，也可英雄覆盖）
   - 初始化英雄基础属性（由后台配置）
   - 军械库仅展示：通用装备 + 该英雄专属装备
4) 购买装备：扣金币 → 放入装备栏（最多 6）→ 累加属性

## 2. 需要后台可配置的对象

### 2.1 英雄（Hero）

后台需要能维护：
- 英雄列表（可选择哪些英雄）
- 英雄基础信息：名称、编码、是否启用、排序
- 英雄初始属性（按属性 key → 数值）
- （可选）英雄初始金币覆盖值
- （可选）英雄头像/立绘图片

### 2.2 装备（Item）

后台需要能维护：
- 装备基础信息：名称、编码、价格、品质、分类、是否上架、排序
- 装备图片（你后续再补充上传/配置）
- 装备属性加成（按属性 key → 数值）
- 装备适用范围：
  - 通用装备（所有英雄可见）
  - 英雄专属装备（绑定若干英雄）

## 3. 数据结构分析（从现有前端死数据迁移到后台）

### 3.1 现有前端数据（现状）

- `web/gm-web-ui/src/data/ow/baseInfo.js`
  - `initialGold`
  - `heroStatConfigs`（属性定义：key/label/icon/color/unit/default）
- `web/gm-web-ui/src/data/storeItems.js`
  - `storeItems`（装备列表：name/price/quality/category/img/stats）

现状缺口：
- 没有“英雄”实体与选择逻辑
- `storeItems` 没有“可用英雄范围”字段
- `stats` 的 key 与 `heroStatConfigs.key` 口径不一致（存在 `damage/health/lifesteal/cooldown` 等）

### 3.2 属性 key 口径（强烈建议先统一）

建议把“属性 key”统一成一套后台可配置的字典（stat defs），并要求：
- 英雄初始属性只允许使用这些 key
- 装备 stats 也只允许使用这些 key

否则会出现：装备加成生效了，但英雄面板不展示（当前前端就存在此问题）。

## 4. 推荐的数据库模型（MySQL 8）

> 设计目标：先能落地，后续可扩展（更多英雄、更多装备、更多属性、版本化）。

### 4.1 属性定义（已有建议，沿用）

表：`ow_stat_def`
- `id` BIGINT PK
- `stat_key` VARCHAR(64) UNIQUE
- `label` VARCHAR(64)
- `unit` VARCHAR(16)
- `is_percent` TINYINT(1)
- `icon_name` VARCHAR(64)
- `color_class` VARCHAR(64)
- `default_value` INT
- `sort_order` INT
- `enabled` TINYINT(1)
- `created_at` / `updated_at`

### 4.2 英雄表

表：`ow_hero`
- `id` BIGINT PK（自增）
- `hero_code` VARCHAR(64) UNIQUE（稳定编码，用于接口/前端缓存）
- `hero_name` VARCHAR(64)
- `description` VARCHAR(255) NULL
- `avatar_url` VARCHAR(255) NULL（图片后续再做上传/配置）
- `initial_gold` INT NULL（为空则用全局默认）
- `base_stats_json` JSON NOT NULL（例：`{"weaponStrength":0,"cooldownReduction":0}`）
- `enabled` TINYINT(1) NOT NULL DEFAULT 1
- `sort_order` INT NOT NULL DEFAULT 0
- `created_at` / `updated_at`

说明：
- `base_stats_json` 的 key 必须来自 `ow_stat_def.stat_key`
- 可用一个后台校验（保存时校验）保证口径一致

### 4.3 装备表

表：`ow_item`
- `id` BIGINT PK（自增）
- `item_code` VARCHAR(64) UNIQUE（稳定编码；不要复用前端的重复 id）
- `item_name` VARCHAR(64)
- `price` INT
- `quality` VARCHAR(16)（green/blue/purple…）
- `category` VARCHAR(16)（weapon/skill/survival/device…）
- `img_url` VARCHAR(255) NULL（图片后续再补；先允许为空）
- `stats_json` JSON NOT NULL（例：`{"attackSpeed":5}`）
- `is_global` TINYINT(1) NOT NULL DEFAULT 1（1=通用装备；0=英雄专属）
- `enabled` TINYINT(1) NOT NULL DEFAULT 1
- `sort_order` INT NOT NULL DEFAULT 0
- `created_at` / `updated_at`

### 4.4 英雄-专属装备绑定表

表：`ow_hero_item`
- `id` BIGINT PK
- `hero_id` BIGINT FK -> `ow_hero.id`
- `item_id` BIGINT FK -> `ow_item.id`
- UNIQUE(`hero_id`,`item_id`)
- `created_at`

查询逻辑（给某个英雄取可见装备）：
- `ow_item.is_global = 1`
- union `ow_item.is_global = 0` 且存在 `ow_hero_item(hero_id = ?)`

约束建议：
- 当 `is_global = 0` 时，至少需要绑定一个 hero（由后台页面保存时校验）

## 5. API 设计（前台 OW 页面需要）

> MVP 只做读取接口即可；后台维护接口放到管理端再做。

### 5.1 前台读取接口

1) 英雄列表（用于“先选英雄”）
- `GET /api/ow/heroes`
- 返回：`[{ heroCode, heroName, avatarUrl, enabled, sortOrder }]`（只返回需要展示的字段）

2) 英雄详情（用于初始化英雄属性/金币）
- `GET /api/ow/heroes/{heroCode}`
- 返回：
  - `initialGold`（英雄覆盖或全局默认）
  - `baseStats`（base_stats_json）
  - `statDefs`（`ow_stat_def`，用于前端渲染面板：label/unit/icon/color/排序）

3) 当前英雄可用装备列表
- `GET /api/ow/items`
- Query：
  - `heroCode`（必填；决定是否返回专属装备）
  - `category`（可选）
  - `quality`（可选）
- 返回：`[{ itemCode, itemName, price, quality, category, imgUrl, stats }]`

### 5.2 管理后台接口（用于配置英雄/装备）

建议放在 admin 权限下（或沿用你现有 /api 体系但加权限点）：

- 英雄：
  - `GET /api/admin/ow/heroes`（列表）
  - `POST /api/admin/ow/heroes`（新增）
  - `PUT /api/admin/ow/heroes/{id}`（编辑）
  - `PUT /api/admin/ow/heroes/{id}/enable`（启用/禁用，可合并到编辑）
- 装备：
  - `GET /api/admin/ow/items`（列表）
  - `POST /api/admin/ow/items`（新增）
  - `PUT /api/admin/ow/items/{id}`（编辑）
  - `POST /api/admin/ow/items/{id}/bind-heroes`（绑定专属英雄列表；也可合并到编辑）
- 属性字典：
  - `GET /api/admin/ow/stat-defs`
  - `PUT /api/admin/ow/stat-defs`（维护展示口径）

权限点建议（后续接入你现有 RBAC）：
- `ow:hero:list/read/create/update`
- `ow:item:list/read/create/update/bind_heroes`
- `ow:stat_def:list/update`

## 6. 管理后台页面清单（你需要的配置能力）

### 6.1 英雄管理

1) 英雄列表页
- 字段：heroName、heroCode、enabled、sortOrder
- 操作：新增/编辑/启用禁用

2) 英雄编辑页（新增/编辑共用）
- 基础：heroName、heroCode、enabled、sortOrder、initialGold（可选）
- 初始属性配置：
  - UI 建议：从 `ow_stat_def` 拉取属性列表，做“可编辑表格”（key、label、value）
  - 保存时生成 `base_stats_json`
- 图片：avatarUrl（后续你自己补充上传/配置）

### 6.2 装备管理

3) 装备列表页
- 字段：itemName、itemCode、price、quality、category、isGlobal、enabled
- 操作：新增/编辑/上架下架/绑定英雄（若 isGlobal=0）

4) 装备编辑页（新增/编辑共用）
- 基础：itemName、itemCode、price、quality、category、enabled、sortOrder
- 图片：imgUrl（可为空）
- 属性配置（stats_json）：
  - UI 建议：从 `ow_stat_def` 选择 key + 输入数值（支持新增/删除多条）
  - 保存时生成 `stats_json`
- 适用英雄：
  - 勾选“通用装备”（isGlobal=1）则不显示绑定
  - 勾选“英雄专属”（isGlobal=0）则显示英雄多选（绑定 `ow_hero_item`）

## 7. 前端 OW 页面改造建议（满足“先选英雄”）

1) 新增“英雄选择页/弹窗”
- 进入 `/ow` 后先展示 hero chooser
- 选中 hero 后进入现有三列布局（装备栏/军械库/英雄面板）

2) gameStore 初始化改造
- 选择 hero 后：
  - 调用 `GET /api/ow/heroes/{heroCode}` 初始化：
    - gold
    - heroStatConfigs（来自 statDefs）
    - heroStats（baseStats）
  - 调用 `GET /api/ow/items?heroCode=...` 获取 storeItems

3) StoreComponent 的装备列表改造
- 改为使用接口返回的 `storeItems`（而不是 `data/storeItems.js`）
- 图片逻辑：
  - 若后端开始返回 `imgUrl`：直接使用
  - 若后端暂时只返回 `imgKey`：仍可走 `imageMap`（你说后续图片你会后台补充，建议直接走 `imgUrl`）

## 8. 数据迁移/落地顺序（建议）

1) 先把属性口径确定：
- 以 `ow_stat_def` 为准
- 清洗当前 `storeItems.stats` 的 key（避免出现 “加成生效但面板不展示”）

2) 先落“英雄”与“装备”表结构（本方案第 4 节）

3) 做 seed：
- 把现有 `initialGold/heroStatConfigs` 灌入 `ow_config/ow_stat_def`
- 先创建几个英雄（即使属性全 0 也可以）
- 把现有通用装备灌入 `ow_item`（`is_global=1`）

4) 实现前台读取接口（第 5.1）

5) 前端 OW 页面改造为：先选英雄再展示军械库

6) 最后再做管理后台维护页面（第 6 节）

