# 乳腺外科临床病例库与科研随访 App NestJS 后端开发方案（P0 版）

版本：v0.1  
日期：2026-06-15  
后端技术栈：NestJS + PostgreSQL + Prisma + JWT + 对象存储

## 1. 技术选型

- 框架：NestJS
- 语言：TypeScript
- 数据库：PostgreSQL
- ORM：Prisma
- 认证：JWT access token + refresh token
- 权限：RBAC，按角色控制接口能力
- 附件：对象存储，后端签发短期上传/预览 URL
- API 风格：RESTful API
- 文档：Swagger/OpenAPI，P0 后续补充

## 2. 后端目录结构

```text
backend/
  src/
    main.ts
    app.module.ts
    common/
    prisma/
      prisma.module.ts
      prisma.service.ts
    modules/
      auth/
      users/
      patients/
      cases/
      attachments/
      tags/
      followups/
      dashboard/
      audit/
  prisma/
    schema.prisma
  database/
    migrations/
      001_init_p0.sql
```

## 3. P0 模块职责

| 模块 | 职责 |
| --- | --- |
| AuthModule | 登录、刷新令牌、退出、当前用户 |
| UsersModule | 科室成员管理 |
| PatientsModule | 患者建档、查询、编辑、脱敏 |
| CasesModule | 乳腺专病病例、归档、科研筛选 |
| AttachmentsModule | 上传授权、附件元数据、预览 URL |
| TagsModule | 科研标签、病例标签绑定 |
| FollowupsModule | 随访计划、随访记录、逾期和待随访 |
| DashboardModule | 首页概览 |
| AuditModule | 操作日志 |
| PrismaModule | 数据库访问 |

## 4. 安全策略

- 所有业务接口默认需要 JWT。
- 管理员专属接口使用角色守卫。
- 患者敏感信息由后端根据角色脱敏后返回。
- 附件不暴露永久公开 URL。
- 密码只保存哈希值。
- 关键操作写入 `audit_logs`。

## 5. 开发顺序

1. 初始化 NestJS 工程和 Prisma。
2. 建立 PostgreSQL 初始化迁移。
3. 实现 Auth、Users。
4. 实现 Patients。
5. 实现 Cases 与科研筛选。
6. 实现 Attachments 对象存储签名。
7. 实现 Tags。
8. 实现 Followups。
9. 实现 Dashboard 与 Audit。
10. 补充 Swagger、单元测试和接口集成测试。

