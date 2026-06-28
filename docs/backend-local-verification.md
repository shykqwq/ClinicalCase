# NestJS 后端本地验证指南

日期：2026-06-15  
适用目录：`backend`

## 1. 前置条件

- Docker Desktop 已启动。
- 已执行 `npm.cmd install --cache .\.npm-cache`。
- 已执行 `npm.cmd run prisma:generate`。
- `.env` 中的 `DATABASE_URL` 保持默认：

```env
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/clinical_case
```

## 2. 启动 PostgreSQL

```powershell
cd E:\TRAE_projrcts\ClinicalCase\backend
npm.cmd run db:docker:up
```

如果提示 Docker daemon 无法连接，请先打开 Docker Desktop，等待左下角显示运行中。

## 3. 执行数据库迁移

```powershell
docker exec -i clinical-case-postgres psql -U postgres -d clinical_case -f /migrations/001_init_p0.sql
```

## 4. 生成管理员账号

```powershell
npm.cmd run db:seed
```

默认账号：

```text
username: admin
password: ChangeMe123
```

## 5. 启动后端服务

```powershell
npm.cmd run start:dev
```

默认地址：

```text
http://localhost:3000/api/v1
```

## 6. 登录测试

PowerShell：

```powershell
$body = @{
  username = "admin"
  password = "ChangeMe123"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:3000/api/v1/auth/login" `
  -ContentType "application/json" `
  -Body $body
```

成功后应返回 `accessToken`、`refreshToken` 和用户信息。

