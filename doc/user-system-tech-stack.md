# 后台用户体系：技术栈清单（已确认：MySQL 8 / MyBatis-Plus / Sa-Token / 手工执行 SQL）

> 本文只罗列“为实现 doc/user-system-feature-list.md 所需的最小技术栈/组件选型”。

## 1. 语言与基础框架

- Java：21（当前 `pom.xml` 已设置）
- 构建：Maven（多模块：`gm-api`、`gm-user`）
- Web 框架：Spring Boot 3.x（当前父 POM 为 `spring-boot-starter-parent` 3.5.10-SNAPSHOT）
- Web 组件：`spring-boot-starter-web`

## 2. 认证与授权

- Sa-Token（Spring Boot 3 适配 starter）
  - token 签发/校验、登录态、权限校验
  - 权限校验方式：注解（如 `@SaCheckPermission`）或代码（如 `StpUtil.checkPermission`）
  - token 存储：使用默认内存实现（不接入 Redis）
- 密码哈希：BCrypt
  - 候选 A：Spring Security Crypto（只引 `spring-security-crypto`，不引整套 Security）
  - 候选 B：jBCrypt（不推荐，Spring 生态更统一）

## 3. 数据访问与数据库

- 数据库：MySQL 8
- JDBC：MySQL 驱动（`com.mysql:mysql-connector-j`）+ Spring Boot 默认连接池 HikariCP
- DAO：MyBatis-Plus（`com.baomidou:mybatis-plus-spring-boot3-starter`）
- SQL 执行方式：手工执行（不引入 Flyway/Liquibase）

## 4. 接口层与工程化

- 参数校验：`spring-boot-starter-validation`（Jakarta Validation）
- DTO 映射（可选，但开发更快）：
  - 候选 A：MapStruct
  - 候选 B：手写转换
- API 文档（可选，但便于联调）：springdoc-openapi
- 日志：Spring Boot 默认 Logback

## 5. 测试

- 单元/集成测试：`spring-boot-starter-test`（已在父 POM 中）
- 集成测试数据库（建议）：
  - Testcontainers（MySQL）

## 6. SQL 目录分类方案（对应功能清单第 8 节）

> 手工执行时，按目录与版本号顺序执行即可；每次变更都新增脚本，避免直接改历史脚本。

- `sql/00_schema/`：建表（DDL）
- `sql/10_seed/`：初始化数据（ADMIN 角色、权限点、管理员账号等）
- `sql/90_rollback/`：回滚脚本（如需要）
- 命名建议：`V{yyyyMMddHHmm}__{desc}.sql`（便于排序与回放）

## 7. 已确认的选型

- MySQL 8
- MyBatis-Plus
- Sa-Token
- SQL：手工执行（脚本存放与分类见第 6 节）

## 8. 初始化管理员（固定默认密码）

- 初始化脚本：`sql/10_seed/` 内会创建 `admin` 管理员账号（固定默认密码写在脚本注释中）
- 建议：上线前修改默认密码并重新生成 BCrypt hash，再执行/更新对应 seed 脚本
