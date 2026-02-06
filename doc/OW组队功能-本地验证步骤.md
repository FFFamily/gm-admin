# OW 组队功能：本地验证步骤

> 日期：2026-02-05

## 1) 执行 SQL（手工）

按 `sql/README.md` 约定执行（按文件名排序）：

- `sql/00_schema/V202602051500__create_ow_lfg_tables.sql`

说明：

- 该脚本新增 4 张表：`ow_lfg_team`、`ow_lfg_team_member`、`ow_lfg_team_join_request`、`ow_lfg_report`

## 2) 启动后端

编译（可选）：

```bash
./mvnw -pl gm-api -am -DskipTests compile
```

启动（根据你现有方式运行 `gm-api` 即可）。

## 3) 启动前端

```bash
cd web/gm-web-ui
npm run dev
```

## 4) 冒烟用例（核心链路）

1. 打开组队大厅：`/ow/lfg`
2. 登录前台账号：`/account/login`（无账号先 `/account/register`）
3. 创建队伍：`/ow/lfg/new`（创建成功后跳转邀请页）
4. 分享邀请页：`/ow/lfg/t/:inviteCode`（复制链接）
5. 另一账号加入/申请加入：
   - 公开队伍：点击“立即加入”
   - 需审批队伍：点击“申请加入”，队长在 `/ow/lfg/teams/:id` 同意/拒绝
6. 成员查看联系方式：进入 `/ow/lfg/teams/:id`（仅成员可见 `contact`）
7. 我的队伍：`/account/teams`（可进入队伍页、退出队伍）

## 5) 接口速查

- Public：
  - `GET /api/ow/lfg/teams`
  - `GET /api/ow/lfg/t/{inviteCode}`
- Account：
  - `POST /api/ow/lfg/teams`
  - `GET /api/ow/lfg/teams/{id}`
  - `GET /api/ow/lfg/teams/{id}/my-state`
  - `POST /api/ow/lfg/teams/{id}/join`
  - `POST /api/ow/lfg/teams/{id}/join-requests`
  - `POST /api/ow/lfg/join-requests/{id}/cancel`
  - `POST /api/ow/lfg/join-requests/{id}/approve`
  - `POST /api/ow/lfg/join-requests/{id}/reject`
  - `POST /api/ow/lfg/teams/{id}/members/{memberId}/kick`
  - `POST /api/ow/lfg/teams/{id}/leave`
  - `POST /api/ow/lfg/teams/{id}/close`
  - `POST /api/ow/lfg/teams/{id}/disband`
  - `GET /api/ow/lfg/my/teams`
  - `POST /api/ow/lfg/reports`

