# SQL 脚本说明（手工执行）

本项目不使用 Flyway/Liquibase，所有数据库变更通过手工执行 `sql/` 下的版本化脚本完成。

## 目录约定

- `sql/00_schema/`：建表（DDL）
- `sql/10_seed/`：初始化数据（ADMIN 角色、权限点、管理员账号等）
- `sql/90_rollback/`：回滚脚本（如需要）

## 命名约定

- 建议统一：`V{yyyyMMddHHmm}__{desc}.sql`
- 例：`V202601261130__create_sys_user_tables.sql`

## 执行顺序

1) 先执行 `sql/00_schema/`（按文件名排序）
2) 再执行 `sql/10_seed/`（按文件名排序）
3) 如需回滚，执行对应 `sql/90_rollback/` 脚本

## 默认管理员

初始化脚本中会创建默认管理员账号（详见 `sql/10_seed/` 脚本内注释）。

## 规则

- 禁止直接修改已执行过的历史脚本；任何变更通过新增脚本完成。
- 每次脚本变更后，同步更新 `doc/user-system-feature-list.md` 中对应项的勾选状态。
