# 校园二手交易与信用评价平台

## 项目简介

一个基于 Spring Boot 3 与 Vue 3 的前后端分离校园二手交易平台。项目覆盖商品发布与审核、订单交易、信用评价、收藏、浏览历史、Redis 缓存和后台数据看板，适合作为 JavaWeb 课程设计或个人项目展示。

## 技术栈

- 后端：Java 17、Spring Boot 3、Spring Security、MyBatis-Plus、JWT、BCrypt、MySQL、Redis、Maven
- 前端：Vue 3、Vite、Element Plus、Vue Router、Pinia、Axios、ECharts
- 数据库：MySQL 8，数据库与数据表统一使用 `utf8mb4`

## 功能模块

- 用户：注册、登录、JWT 鉴权、USER/ADMIN 角色、账号封禁、信用分
- 商品：发布、图片上传、搜索、分类筛选、分页、编辑、下架、管理员审核
- 订单：买家下单和取消、卖家确认完成、商品自动变为已售出
- 评价：完成订单后买家评价卖家，评分动态影响 0-150 范围信用分
- 收藏：收藏、取消收藏、我的收藏、商品详情收藏状态
- 浏览历史：登录用户访问详情自动记录，相同商品仅更新时间
- 后台管理：用户管理、商品审核、违规下架
- 数据看板：用户/商品/订单/成交额概览，商品状态、分类数量和 7 日订单趋势
- 智能交易助手：发布优化、价格评估、交易风险、个性化推荐和规则问答
- Redis：缓存热门商品和商品详情，商品状态变化时主动清理

## 项目结构

```text
campus-trade-server/   Spring Boot 后端
campus-trade-web/      Vue 3 前端
sql/init.sql           可重复执行的数据库初始化脚本
README.md              安装、启动和项目说明
TESTING.md             手工功能验收清单
```

后端按 `controller`、`service`、`mapper`、`entity`、`dto`、`vo`、`config`、`security`、`common` 分层。

## 环境要求

- JDK 17 或更高版本
- Maven 3.8+
- Node.js 18+
- MySQL 8
- Redis 6/7（推荐，未启动时核心业务仍可运行）

## 完整启动步骤

### 1. 初始化数据库

确认 MySQL 已启动，然后在项目根目录执行：

```powershell
cd E:\JAVAWeb\campus-trade-codex
cmd /c "mysql -u root -p --default-character-set=utf8mb4 < .\sql\init.sql"
```

输入 MySQL root 密码。脚本会删除并重建 `campus_trade`，请先备份需要保留的数据。

如果 Redis 中已有旧商品缓存，可在项目独占 Redis 数据库时执行：

```powershell
redis-cli --scan --pattern "goods:*"
redis-cli FLUSHDB
```

共享 Redis 不要使用 `FLUSHDB`，只删除 `goods:hot` 和 `goods:detail:*` 项目缓存键，或等待默认 10 分钟过期。

### 2. 启动 Redis

本地安装版：

```powershell
redis-server
```

Docker：

```powershell
docker run -d --name campus-redis -p 6379:6379 redis:7-alpine
```

检查连接：

```powershell
redis-cli ping
```

正常结果为 `PONG`。

### 3. 启动后端

默认数据库密码是 `123456`。如果本机密码不同，先设置环境变量：

```powershell
$env:DB_PASSWORD="你的MySQL密码"
cd E:\JAVAWeb\campus-trade-codex\campus-trade-server
mvn clean package -DskipTests
java -jar target\campus-trade-server-1.0.0.jar
```

后端地址：`http://localhost:8080`。

也可以开发模式启动：

```powershell
mvn spring-boot:run
```

### 4. 启动前端

```powershell
cd E:\JAVAWeb\campus-trade-codex\campus-trade-web
npm.cmd install
npm.cmd run dev
```

浏览器访问 `http://localhost:5173`。Vite 会将 `/api` 和 `/uploads` 代理到后端。

## application.yml 配置

配置均支持环境变量覆盖：

| 配置 | 默认值 | 环境变量 |
| --- | --- | --- |
| MySQL 主机 | `localhost` | `DB_HOST` |
| MySQL 端口 | `3306` | `DB_PORT` |
| 数据库名 | `campus_trade` | `DB_NAME` |
| MySQL 用户 | `root` | `DB_USERNAME` |
| MySQL 密码 | `123456` | `DB_PASSWORD` |
| Redis 主机/端口 | `localhost:6379` | `REDIS_HOST` / `REDIS_PORT` |
| Redis 密码 | 空 | `REDIS_PASSWORD` |
| 上传目录 | `uploads` | `UPLOAD_DIR` |
| JWT 密钥 | 仅供演示的开发密钥 | `JWT_SECRET` |
| JWT 有效期 | `86400000` 毫秒 | `JWT_EXPIRATION` |

生产环境必须替换 `JWT_SECRET`。默认 MySQL 密码和 JWT 字符串仅用于本地演示，
不要把真实数据库密码、Redis 密码、Token 或 API Key 提交到代码仓库。

## 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 普通用户 | `student1` | `123456` |
| 普通用户 | `student2` | `123456` |

数据库中存储的是 BCrypt 密文。

## 业务流程

1. 用户注册或登录。
2. 卖家发布商品，商品进入 `PENDING` 状态。
3. 管理员审核通过后，商品变为 `ON_SALE`。
4. 买家浏览、收藏商品并创建订单。
5. 买家可在完成前取消订单；卖家确认交易完成后订单变为 `COMPLETED`，商品变为 `SOLD`。
6. 买家对卖家进行 1-5 星评价，系统同步调整卖家信用分。
7. 管理员通过数据看板查看平台业务数据。

## 信用分规则

| 评分 | 信用分变化 |
| --- | --- |
| 5 星 | +2 |
| 4 星 | +1 |
| 3 星 | 不变 |
| 2 星 | -2 |
| 1 星 | -5 |

信用分始终限制在 0-150。

## 智能交易助手 Agent

当前 Agent 是完全本地运行的规则版本，不要求配置真实大模型 API。规则会动态读取商品、分类、同类价格、卖家信用、评价、收藏和浏览历史。

主要能力：

- 发布建议：优化标题和描述，检查信息完整度并给出建议售价。
- 价格评估：根据成色关键词、原价及同分类在售均价计算价格区间。
- 风险分析：综合卖家信用、异常低价、图片、描述长度、发布时间和差评数量。
- 个性化推荐：优先匹配收藏和浏览分类，排除本人及非在售商品。
- 智能问答：识别购买判断、价格判断、分类推荐、商品定价和信用分问题。

价格规则示例：

- 全新/未拆封：原价的 75%-90%
- 九成新：原价的 60%-75%
- 八成新：原价的 45%-60%
- 有划痕/损坏/维修：原价的 20%-40%
- 使用一年：原价的 35%-55%
- 结果明显偏离平台同类均价时会自动校正

风险分数按规则累加并限制在 0-100：低于 25 为 `LOW`，25-54 为 `MEDIUM`，55 以上为 `HIGH`。

后端预留了 `AgentModelProvider` 扩展接口。以后接入真实大模型时，可新增该接口实现并注册为 Spring Bean；未配置可用 Provider 时继续使用本地规则。

## 主要接口

| 模块 | 接口 |
| --- | --- |
| 认证 | `POST /api/auth/register`、`POST /api/auth/login`、`GET /api/auth/me` |
| 商品 | `GET /api/goods/page`、`GET /api/goods/{id}`、`GET /api/goods/hot`、`POST /api/goods` |
| 订单 | `POST /api/orders`、`GET /api/orders/buy`、`GET /api/orders/sell`、取消、完成 |
| 评价 | `POST /api/reviews`、`GET /api/reviews/user/{userId}` |
| 收藏 | `POST/DELETE /api/favorites/{goodsId}`、`GET /api/favorites/my` |
| 浏览记录 | `GET /api/history/my` |
| 文件 | `POST /api/files/upload`，静态访问路径 `/uploads/{filename}` |
| 管理 | `/api/admin/**`，仅 ADMIN 可访问 |
| Agent | `POST /api/agent/publish-advice`、`POST /api/agent/price-advice` |
| Agent | `GET /api/agent/goods/{goodsId}/risk`、`GET /api/agent/recommend`、`POST /api/agent/chat` |

接口统一返回 `{ "code": 200, "message": "success", "data": ... }`。

所有 `/api/agent/**` 接口都要求登录并携带 JWT。

## 常见问题排查

### MySQL 连接失败

- 确认 MySQL 服务已启动。
- 检查 `DB_HOST`、`DB_PORT`、`DB_USERNAME`、`DB_PASSWORD`。
- 出现 `Access denied` 时通常是用户名或密码错误。
- 出现表不存在时，重新执行 `sql/init.sql`。

### SQL 外键或字段长度报错

`init.sql` 会先删除并重建数据库，并使用 `utf8mb4`。请确保执行的是当前项目中的脚本：

```powershell
cmd /c "mysql -u root -p --default-character-set=utf8mb4 < .\sql\init.sql"
```

### Redis 未启动

热门商品和详情缓存会自动降级，不影响登录、商品、订单等核心业务。启动 Redis 后重启后端即可启用缓存。

数据库重新初始化后若仍看到旧商品状态，请清理 `goods:*` 缓存键或等待缓存过期。

### 前端请求 404

- 确认后端运行在 `8080`、前端运行在 `5173`。
- 开发环境应使用 `npm.cmd run dev`，由 Vite 代理 `/api`。
- 直接部署 `dist` 时，需要在 Web 服务器配置 `/api` 和 `/uploads` 反向代理。

### 图片上传后无法显示

- 必须先登录，且仅支持 `.jpg`、`.jpeg`、`.png`。
- 默认文件保存在后端运行目录的 `uploads`。
- 确认访问 `http://localhost:8080/uploads/文件名` 能返回图片。

### 管理页面无法访问

- 使用 `admin / 123456` 登录。
- 普通用户访问 `/admin` 会被前端重定向，直接调用 `/api/admin/**` 会返回 403。
- 若图表区域为空，检查四个 `/api/admin/stat/**` 接口及浏览器控制台。

### PowerShell 无法执行 npm

使用 `npm.cmd` 代替 `npm`：

```powershell
npm.cmd install
npm.cmd run dev
```

## 简历项目描述

设计并实现基于 Spring Boot 3 与 Vue 3 的校园二手交易与信用评价平台，完成 JWT 无状态认证、商品审核、订单状态流转、交易评价与信用分联动、收藏和浏览历史等核心业务；使用 Redis 缓存热点数据，通过 ECharts 构建后台统计看板，并以 MyBatis-Plus、MySQL 事务和数据唯一索引保证业务一致性。

在平台中增量设计智能交易助手 Agent，以可解释的本地规则引擎实现商品发布优化、动态价格评估、交易风险分析、行为偏好推荐和意图问答，并预留模型 Provider 接口支持后续平滑接入大模型服务。

## 技术亮点

- Spring Security + JWT 实现无状态认证和 USER/ADMIN 分级授权。
- Service 层校验数据归属，防止越权修改商品、订单和评价。
- 订单完成、商品售出和信用分更新采用事务化业务处理。
- Redis 缓存热门商品与详情，商品变更后主动失效，Redis 故障时可降级。
- 收藏与浏览历史使用组合唯一索引和幂等处理避免重复数据。
- 本地图片随机文件名存储，配置静态资源映射供前端访问。
- ECharts 展示商品状态、分类分布和最近 7 天订单趋势。
- 规则 Agent 聚合平台实时数据进行可解释决策，无外部 API 时可独立运行。
- 通过 `AgentModelProvider` 策略扩展点解耦本地规则与未来大模型实现。

完整手工验收流程见 [TESTING.md](TESTING.md)。
