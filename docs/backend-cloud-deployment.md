# 后端云服务器部署说明

本项目 P0 推荐先用一台云服务器部署：

- Caddy：公网 HTTPS 入口，自动申请和续期 TLS 证书
- NestJS API：只在 Docker 内网暴露 `3001`
- PostgreSQL：只绑定本机/容器网络

真实患者身份信息会经过公网传输，生产环境必须使用 HTTPS。不要长期使用裸 HTTP。

## 1. 域名准备

HTTPS 需要一个域名，不能稳定依赖裸 IP 申请可信证书。

示例：

```text
api.your-domain.com
```

在域名 DNS 控制台添加 A 记录：

```text
主机记录：api
记录类型：A
记录值：服务器公网 IP，例如 8.130.130.235
```

等待 DNS 生效后，在本机测试：

```bash
ping api.your-domain.com
```

## 2. 服务器安全组

阿里云安全组入方向放行：

```text
TCP 80   0.0.0.0/0
TCP 443  0.0.0.0/0
```

启用 HTTPS 后，不需要公网开放 `3001`。如果之前放行过 `3001`，HTTPS 跑通后建议删除这条规则。

## 3. 拉取代码

```bash
git clone https://github.com/shykqwq/ClinicalCase.git
cd ClinicalCase/backend
```

如果服务器上已经拉过代码：

```bash
cd /home/admin/ClinicalCase
git pull
cd backend
```

## 4. 配置生产环境变量

```bash
cp .env.production.example .env.production
vi .env.production
```

必须修改：

```text
API_DOMAIN=你的接口域名，例如 api.your-domain.com

POSTGRES_PASSWORD=一个强数据库密码
DATABASE_URL=postgresql://clinical_case:同一个强数据库密码@postgres:5432/clinical_case

JWT_ACCESS_SECRET=一个很长的随机字符串
JWT_REFRESH_SECRET=另一个很长的随机字符串

SEED_ADMIN_PASSWORD=首次管理员密码
```

生成随机密钥：

```bash
openssl rand -hex 32
```

## 5. 启动 HTTPS 版服务

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

查看状态：

```bash
docker ps
```

应看到：

```text
clinical-case-caddy
clinical-case-api
clinical-case-postgres
```

查看日志：

```bash
docker compose -f docker-compose.prod.yml logs -f caddy
docker compose -f docker-compose.prod.yml logs -f api
```

## 6. 验证 HTTPS

本机验证容器内 API：

```bash
curl http://127.0.0.1:3001/api/v1/health
```

公网验证 HTTPS：

```bash
curl https://api.your-domain.com/api/v1/health
```

应返回：

```json
{"status":"ok","timestamp":"..."}
```

登录测试：

```bash
curl -X POST https://api.your-domain.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的管理员密码"}'
```

## 7. Android 连接 HTTPS API

Windows PowerShell：

```powershell
cd E:\TRAE_projrcts\ClinicalCase\android
.\gradlew.bat assembleDebug -PAPI_BASE_URL=https://api.your-domain.com/api/v1/
```

生成 APK：

```text
E:\TRAE_projrcts\ClinicalCase\android\app\build\outputs\apk\debug\app-debug.apk
```

## 8. 常用运维命令

重启：

```bash
docker compose -f docker-compose.prod.yml restart
```

停止：

```bash
docker compose -f docker-compose.prod.yml down
```

备份数据库：

```bash
docker exec clinical-case-postgres pg_dump -U clinical_case clinical_case > clinical_case_backup.sql
```

恢复数据库：

```bash
cat clinical_case_backup.sql | docker exec -i clinical-case-postgres psql -U clinical_case -d clinical_case
```
