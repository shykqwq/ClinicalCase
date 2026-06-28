# 后端云服务器部署说明

本项目 P0 推荐先用一台云服务器部署：

- NestJS API
- PostgreSQL
- Docker Compose

真实患者身份信息会经过公网传输，正式使用建议绑定域名并启用 HTTPS，或至少只在可信网络/VPN 内访问。

## 1. 服务器准备

推荐服务器：

- Ubuntu 22.04/24.04
- 2 核 4G 起步
- 系统盘 40G 起步
- 安全组开放 `3001/tcp`

安装 Docker：

```bash
sudo apt update
sudo apt install -y ca-certificates curl git
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
```

退出 SSH 后重新登录，让 docker 用户组生效。

## 2. 上传项目

方式一：用 Git 拉代码：

```bash
git clone <你的仓库地址> ClinicalCase
cd ClinicalCase/backend
```

方式二：用压缩包上传整个 `ClinicalCase` 项目，然后进入：

```bash
cd ClinicalCase/backend
```

## 3. 配置生产环境变量

```bash
cp .env.production.example .env.production
nano .env.production
```

必须修改这些值：

```text
POSTGRES_PASSWORD=一个强数据库密码
DATABASE_URL=postgresql://clinical_case:同一个强数据库密码@postgres:5432/clinical_case
JWT_ACCESS_SECRET=一个很长的随机字符串
JWT_REFRESH_SECRET=另一个很长的随机字符串
SEED_ADMIN_PASSWORD=首次管理员密码
```

生成随机密钥可用：

```bash
openssl rand -hex 32
```

## 4. 启动后端

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

查看日志：

```bash
docker compose -f docker-compose.prod.yml logs -f api
```

第一次启动会自动：

- 等待 PostgreSQL 就绪
- 初始化数据库表
- 创建默认科室和管理员账号
- 启动 NestJS API

后续重启不会重复建表。

## 5. 验证接口

假设服务器公网 IP 是 `1.2.3.4`：

```bash
curl http://1.2.3.4:3001/api/v1/health
```

应该返回：

```json
{"status":"ok","timestamp":"..."}
```

登录测试：

```bash
curl -X POST http://1.2.3.4:3001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"你的 SEED_ADMIN_PASSWORD"}'
```

## 6. Android 连接云端 API

构建 APK 时传入服务器地址：

```bash
cd ../android
./gradlew assembleDebug -PAPI_BASE_URL=http://1.2.3.4:3001/api/v1/
```

Windows PowerShell：

```powershell
cd E:\TRAE_projrcts\ClinicalCase\android
.\gradlew.bat assembleDebug -PAPI_BASE_URL=http://1.2.3.4:3001/api/v1/
```

如果使用域名和 HTTPS，则改成：

```text
https://api.your-domain.com/api/v1/
```

## 7. 常用运维命令

重启：

```bash
docker compose -f docker-compose.prod.yml restart api
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

