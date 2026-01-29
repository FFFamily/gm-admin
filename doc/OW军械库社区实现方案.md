# OW「军械库社区」改造实现方案

> 目标：将现有「角斗领域：军械库模拟器」改造成一个「军械库社区」：用户可按英雄浏览他人分享的装备清单；登录后可创建并公开分享自己的装备清单。
>
> 约束：MySQL 8 + MyBatis-Plus + Sa-Token；SQL 手工执行；不使用 Redis。

## 1. 角色与登录态

- 游客：可浏览清单列表/详情（公开内容）
- 前台账号（Account）：可创建装备清单
- 后台管理员（Admin）：可选（后续）管理/下架违规清单

说明：
- 创建清单需要“前台账号”登录态（要求 loginId 为 `ACC_*`），避免后台 token 误用。

## 2. 前端页面（建议路由）

### 2.1 社区入口

- `/ow`：军械库社区列表页（默认入口，不再直接进入模拟器）
  - 顶部筛选：英雄下拉（全部/某英雄）、关键字（标题/描述）、排序（最新）
  - 中间列表：一行一条“装备清单卡片”
    - 标题
    - 描述摘要
    - 英雄信息（heroName/heroCode）
    - 作者（displayName/username，可选）
    - 创建时间
  - 操作：查看详情、创建清单（未登录跳转 `/account/login`）

### 2.2 清单详情

- `/ow/loadouts/:id`：装备清单详情页
  - 展示：标题、描述、英雄、装备列表（最多 6）
  - 计算展示（可选但推荐）：总价、累加后的属性（复用模拟器的计算逻辑/展示组件）

### 2.3 创建清单

- `/ow/loadouts/new`：创建清单页（需要前台账号登录）
  - 选择英雄（必选）
  - 输入标题/描述
  - 选择装备（最多 6）：候选装备来自后端（全局 + 该英雄专属）
  - 点击“发布”后返回详情页

> 兼容策略：保留原模拟器能力作为“创建清单页内部的配装区域”，即：创建页 = 选择英雄 + 军械库挑选 + 结果发布。

## 3. 数据结构设计（MySQL）

### 3.1 核心表：装备清单（帖子）

新表：`ow_loadout`

字段建议（最小必需）：
- `id` BIGINT PK
- `hero_code` VARCHAR(64)（关联 `ow_hero.hero_code`）
- `title` VARCHAR(128)
- `description` VARCHAR(512)（可选）
- `items_json` JSON（数组，最多 6 个 item_code：`["item_0001","item_0007"]`）
- `created_by_account_id` BIGINT（关联 `app_account.id`）
- `created_by_name` VARCHAR(64)（作者名快照，展示用，可避免每次 join）
- `status` VARCHAR(16)（`PUBLISHED`；预留后续 `HIDDEN`）
- `created_at` / `updated_at`

索引建议：
- `(hero_code, created_at)`
- `(status, created_at)`
- `created_by_account_id`

> 备注：`items_json` 用 item_code，而不是 item_id，避免环境/seed 变化导致引用失效；后端展示时再按 item_code 查 `ow_item`。

### 3.2 可选扩展（先不做）

- 评论：`ow_loadout_comment`
- 版本快照：`items_snapshot_json`（在装备配置经常变动时更可靠）

## 4. 后端接口设计（/api/ow/community/*）

### 4.1 公共：列表

- `GET /api/ow/community/loadouts`
- Query：
  - `page`/`size`
  - `heroCode`（可选；为空表示全部）
  - `keyword`（可选；匹配 title/description）
  - `sort`（可选：`newest`/`hot`/`views`/`likes`/`favorites`）
  - `featured`（可选：true 表示仅看精选）
- 返回：`PageResult<LoadoutSummaryDto>`
  - `id`
  - `heroCode`/`heroName`
  - `title`
  - `description`
  - `authorName`
  - `createdAt`
  - `viewCount`/`likeCount`/`favoriteCount`
  - `featured`/`pinned`
  - `itemsPreview`（最多 6）
  - `totalPrice` + `topStats` + `categoryCounts`（用于列表页信息密度）

### 4.2 公共：详情

- `GET /api/ow/community/loadouts/{id}`
- 返回：`LoadoutDetailDto`
  - summary 字段 +
  - `items`：装备明细（根据 item_code 查询 `ow_item`，返回 itemName/quality/category/price/stats/imgUrl/imgKey）
  - `totalPrice`（后端计算）
  - `statTotals`（可选：按你当前模拟器的逻辑做累加，便于前端直接展示）
  - `viewCount`/`likeCount`/`favoriteCount`
  - `featured`/`pinned`

### 4.3 需要前台账号登录：创建

- `POST /api/ow/community/loadouts`
- Body：
  - `heroCode`
  - `title`
  - `description`（可选）
  - `itemCodes` string[]（最多 6）
- 校验：
  - 必须为前台账号登录（`ACC_*`）
  - `heroCode` 必须存在且 enabled
  - `itemCodes` 中每个 item 必须存在、enabled，且满足“全局/英雄专属可用性”规则
- 返回：`{ id }` 或 `LoadoutDetailDto`

### 4.4 可选（先不做，但建议预留）

- `DELETE /api/ow/community/loadouts/{id}`：作者删除（软删/下架）
- `PUT /api/ow/community/loadouts/{id}`：作者编辑（避免内容错误）

### 4.5 需要前台账号登录：点赞/收藏

- `PUT /api/ow/community/loadouts/{id}/like` / `DELETE /api/ow/community/loadouts/{id}/like`
- `PUT /api/ow/community/loadouts/{id}/favorite` / `DELETE /api/ow/community/loadouts/{id}/favorite`
- `GET /api/ow/community/loadouts/{id}/my-state`

### 4.6 公共：英雄清单数

- `GET /api/ow/community/heroes`

## 5. 后端实现落点（模块划分）

- `gm-ow`：新增 community 领域
  - entity：`OwLoadout`
  - mapper：`OwLoadoutMapper`（分页/筛选）
  - service：
    - `OwCommunityQueryService`（列表/详情）
    - `OwCommunityWriteService`（创建）
  - dto：
    - `LoadoutSummaryDto` / `LoadoutDetailDto` / `LoadoutCreateRequest`
- `gm-api`：新增 Controller
  - `OwCommunityController`：`/api/ow/community/loadouts`
- `gm-user`：复用前台账号表（`app_account`）用于创建者信息
- `gm-config`：Sa-Token 拦截策略需要调整
  - 现状：`/api/ow/**` 被排除在登录校验之外
  - 目标：仅放行“浏览接口”，创建接口必须登录
  - 方案建议：
    1) 将社区写接口路径单独放到 `/api/ow/community/**`，并从 notMatch 中移除（让全局拦截生效）
    2) 或者更精细：在 SaRouter 中按路径单独 check（读接口放行、写接口 checkLogin）

## 6. 前端实现要点（web/gm-web-ui）

### 6.1 数据请求

- 公共读：使用 `httpPublic`（无需 token）
- 需要前台账号登录的操作：使用 `httpAccount`（Header `satoken`）

### 6.2 复用现有模拟器组件

当前模拟器的关键点：
- 已有英雄、装备的后端化接口（`/api/ow/heroes`、`/api/ow/items`）
- 已有“选择英雄 -> 加载装备 -> 购买/累加属性”的 UI/Store 逻辑

改造建议：
- 社区列表/详情为“内容展示”
- 创建清单页内部复用“模拟器选装备”交互，但最终不是“玩一局”，而是“发布清单”

## 7. SQL 脚本规划（手工执行）

已新增脚本：

- 建表：`sql/00_schema/V202601291430__create_ow_loadout_tables.sql`（创建 `ow_loadout`）
- 示例数据（可选）：`sql/10_seed/V202601291500__seed_ow_loadout_sample.sql`
- 互动字段补丁：`sql/00_schema/V202601291620__alter_ow_loadout_add_social_fields.sql`
- 点赞/收藏表：`sql/00_schema/V202601291621__create_ow_loadout_like_favorite_tables.sql`
- 后台权限点（清单治理）：`sql/10_seed/V202601291635__add_admin_ow_loadout_permissions.sql`

可选（未来做后台治理时再补）：

- `sql/10_seed/VyyyyMMddHHmm__add_ow_community_permissions.sql`
  - 例：`ow:community:list` / `ow:community:delete` / `ow:community:hide`

> 前台创建接口不走 RBAC 权限点，只校验前台账号登录即可。

## 8. 最小落地步骤（推荐顺序）

1) DB：新增 `ow_loadout` 表脚本并执行
2) 后端：实现 3 个核心接口（列表/详情/创建），完成“英雄/装备可用性校验”
3) 前端：新增社区列表页 `/ow`、详情页 `/ow/loadouts/:id`、创建页 `/ow/loadouts/new`
4) 迁移入口：首页“军械库模拟器”按钮改为进入社区（保留模拟器在创建页内部）
