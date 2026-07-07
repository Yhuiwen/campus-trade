# 校园二手交易平台本地运行指南

## 1. 环境要求

- JDK 17 或以上
- Maven 3.8+
- Node.js 18 或以上
- MySQL 8
- Redis 6/7：推荐启动。当前代码会缓存热门商品和商品详情，Redis 不可用时会回源查询数据库，核心业务仍可运行。
- Git

## 2. 项目结构

| 目录 | 说明 |
| --- | --- |
| `campus-trade-server` | Spring Boot 3 后端，包含认证、商品、订单、评价、收藏、浏览历史、后台管理和 Agent 接口 |
| `campus-trade-web` | Vue 3 前端，使用 Vue Router、Pinia、Axios、Element Plus 和 ECharts |
| `sql/init.sql` | MySQL 初始化脚本，创建数据库、表结构和演示数据 |
| `docs` | 项目运行、数据库、接口、测试和面试说明文档 |

## 3. 数据库初始化

`sql/init.sql` 用于初始化本地演示数据库 `campus_trade`，会创建用户、分类、商品、收藏、浏览历史、订单和评价相关表，并写入演示账号和商品数据。

注意：该脚本会执行 `DROP DATABASE IF EXISTS campus_trade`，会重建 `campus_trade` 数据库，执行前请确认本地没有需要保留的数据。

在项目根目录执行：

```powershell
cmd /c "mysql -u root -p --default-character-set=utf8mb4 < .\sql\init.sql"
```

脚本内置演示账号仅用于本地验证，密码均为 `123456`，数据库中保存的是 BCrypt 密文。

## 4. 后端启动

```powershell
cd campus-trade-server
$env:DB_PASSWORD="你的MySQL密码"
mvn spring-boot:run
```

后端默认地址为：

```text
http://localhost:8080
```

常用环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_HOST` | `localhost` | MySQL 主机 |
| `DB_PORT` | `3306` | MySQL 端口 |
| `DB_NAME` | `campus_trade` | 数据库名 |
| `DB_USERNAME` | `root` | 数据库用户 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `REDIS_HOST` | `localhost` | Redis 主机 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `JWT_SECRET` | 开发演示密钥 | 生产环境必须替换为至少 32 个字符的安全密钥 |
| `UPLOAD_DIR` | `uploads` | 图片上传目录 |

## 5. 前端启动

```powershell
cd campus-trade-web
npm.cmd install
npm.cmd run dev
```

前端默认地址为：

```text
http://localhost:5173
```

开发环境下，Vite 会将 `/api` 和 `/uploads` 代理到 `http://localhost:8080`。

## 6. Redis 说明

当前后端通过 `CacheService` 使用 Redis 缓存：

- `goods:hot`：热门商品列表，默认 10 分钟过期。
- `goods:detail:{id}`：商品详情，默认 10 分钟过期。

商品创建、修改、下架、订单创建、订单取消和订单完成时，会主动清理对应商品详情缓存和热门商品缓存。`CacheService` 对 Redis 读写和删除异常做了捕获，Redis 未启动时会记录日志并回源数据库，不影响登录、商品浏览、下单、评价等核心流程。

启动 Redis 示例：

```powershell
redis-server
```

或使用 Docker：

```powershell
docker run -d --name campus-redis -p 6379:6379 redis:7-alpine
```

## 7. 常见问题排查

| 问题 | 处理办法 |
| --- | --- |
| 8080 端口被占用 | 关闭占用进程，或通过 `SERVER_PORT` 指定其他端口，同时修改前端代理目标 |
| 5173 端口被占用 | Vite 会自动提示其他端口，也可以使用 `npm.cmd run dev -- --port 5174` |
| MySQL 密码错误 | 设置 `$env:DB_PASSWORD`，确认 `DB_USERNAME`、`DB_HOST`、`DB_PORT` 正确 |
| 数据库未初始化 | 在项目根目录重新执行 `sql/init.sql`，注意脚本会重建 `campus_trade` |
| 中文乱码 | 确认数据库、连接参数和脚本执行命令均使用 `utf8mb4` |
| npm.ps1 执行策略问题 | 在 PowerShell 使用 `npm.cmd install` 和 `npm.cmd run dev` |
| 前端接口 404 | 确认后端运行在 8080，前端 `.env.development` 中代理目标为 `http://localhost:8080` |
| 图片上传后无法访问 | 确认已登录、文件格式为 jpg/jpeg/png，并访问 `/uploads/{filename}` |
| Redis 未启动 | 项目可降级运行；若需要缓存能力，启动 Redis 后重启后端 |
| JWT 登录后 401 | 清理浏览器 localStorage 后重新登录，确认 `JWT_SECRET` 未在运行中变化 |
| 跨域问题 | 检查 `CORS_ALLOWED_ORIGINS` 是否包含前端地址 |
| Maven 依赖下载失败 | 检查网络和 Maven 仓库配置，可重试 `mvn clean package -DskipTests` |
