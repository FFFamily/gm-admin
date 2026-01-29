# 后台「用户体系」功能清单（基础版）

> 目标：实现一套最小可用的后台用户体系：可登录、可管理用户、可基于角色/权限控制接口访问。
>
> 执行规则：每完成一项就将对应 checkbox 改为 `[x]`。

## 1. 数据库与数据模型

- [x] 设计并创建基础表：
  - [x] `sys_user`（username、password_hash、display_name、enabled、last_login_at、created_at、updated_at）
  - [x] `sys_role`（role_code、role_name、created_at、updated_at）
  - [x] `sys_permission`（perm_code、perm_name、created_at、updated_at）
  - [x] `sys_user_role`
  - [x] `sys_role_permission`
  - [x] `sys_login_log`（username、success、reason、ip、ua、created_at）
  - [x] `sys_audit_log`（actor_user_id、action、target_type、target_id、detail_json、result、ip、ua、request_id、created_at）
- [x] 初始化数据（seed）：
  - [x] 内置角色：`ADMIN`
  - [x] 内置权限点：覆盖本清单涉及的接口（见第 7 节）
  - [x] 初始化管理员账号（首次启动可用）

## 2. 认证（Authentication）

- [x] 用户名 + 密码登录接口
  - [x] 密码使用 BCrypt 校验
  - [x] 登录成功签发 Sa-Token token
  - [x] 登录失败不暴露“用户是否存在”
  - [x] 记录登录日志（成功/失败）
- [x] Sa-Token 登录态校验
  - [x] 解析 token，建立当前用户上下文
  - [x] token 过期校验
- [x] 退出登录
  - [x] 基础版：前端丢弃 token（不做服务端黑名单）

## 3. 授权（Authorization）

- [x] RBAC 权限校验
  - [x] 支持为接口配置所需 `perm_code`
  - [x] 无权限返回 403
  - [x] `ADMIN` 默认放行（拥有全部权限或跳过权限校验）

## 4. 用户管理（后台）

- [x] 用户列表：分页 + 关键字搜索（username/display_name）+ 状态筛选（enabled）
- [x] 用户详情：基本信息 + 角色
- [x] 新增用户：设置 username、display_name、初始密码、enabled、角色
- [x] 编辑用户：修改 display_name、enabled、角色
- [x] 重置密码：管理员输入并确认新密码进行重置（记录审计日志）

## 5. 角色与权限管理（后台）

- [x] 权限点列表：查询 `sys_permission`
- [x] 角色管理：创建/编辑/删除角色（`ADMIN` 不允许删除）
- [x] 角色绑定权限点：维护 `sys_role_permission`
- [x] 用户绑定角色：维护 `sys_user_role`

## 6. 审计日志（基础）

- [x] 关键操作写入 `sys_audit_log`
  - [x] 用户：新增/编辑/启用禁用/重置密码
  - [x] 角色：创建/编辑/删除/绑定权限
  - [x] 用户-角色绑定变更
- [x] 审计日志列表：分页 + 条件筛选（actor、action、target、时间范围）

## 7. 权限点（最小集合）

- [x] `auth:login`：登录
- [x] `user:list` / `user:read` / `user:create` / `user:update` / `user:reset_password`
- [x] `role:list` / `role:read` / `role:create` / `role:update` / `role:delete` / `role:bind_permissions`
- [x] `permission:list`
- [x] `audit:list`

## 8. SQL 文件落地规范（实现约束）

- [x] 所有可执行 SQL 放在仓库根目录 `sql/` 并分类存放（分类方案在技术栈文档中确认后执行）
- [x] 每次新增/变更表结构，都新增一份可回放的版本化脚本（禁止直接改旧脚本）
