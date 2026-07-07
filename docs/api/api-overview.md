# 接口说明概览

## 1. 接口风格

项目采用 RESTful API，前端通过 Axios 调用后端接口。后端统一使用 `/api` 前缀，返回结构为：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

登录后前端会携带 JWT 访问受保护接口。`SecurityConfig` 中配置了公开接口、管理员接口和登录后访问接口：

- 公开接口：登录、注册、图片静态访问、商品 GET 接口、分类列表、用户评价列表。
- 管理员接口：`/api/admin/**`，要求 `ADMIN` 角色。
- 其他接口：要求登录。

## 2. 认证接口

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | 用户注册，注册后默认角色为 `USER` | 公开 |
| `POST` | `/api/auth/login` | 用户登录，返回 JWT 和用户信息 | 公开 |
| `GET` | `/api/auth/me` | 获取当前登录用户信息 | 登录用户 |

当前代码没有单独的后端退出登录接口，前端可通过清理本地 token 完成退出。

## 3. 商品接口

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `GET` | `/api/goods/page` | 分页查询在售商品，支持 `current`、`size`、`keyword`、`categoryId` | 公开 |
| `GET` | `/api/goods/hot` | 查询热门商品，按浏览量排序，Redis 缓存 | 公开 |
| `GET` | `/api/goods/{id}` | 查询商品详情；登录用户访问时会记录浏览历史和收藏状态 | 公开 |
| `GET` | `/api/goods/my` | 查询我发布的商品 | 登录用户 |
| `POST` | `/api/goods` | 发布商品，初始状态为 `PENDING` | 登录用户 |
| `PUT` | `/api/goods/{id}` | 编辑自己的商品，编辑后重新进入 `PENDING` | 商品发布者 |
| `PUT` | `/api/goods/{id}/off-shelf` | 下架自己的商品，已售出商品不能下架 | 商品发布者 |
| `GET` | `/api/categories` | 查询商品分类 | 公开 |
| `POST` | `/api/files/upload` | 上传 jpg/jpeg/png 图片，返回可访问路径 | 登录用户 |

## 4. 订单接口

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `POST` | `/api/orders` | 买家创建订单，不能购买自己的商品，商品必须为 `ON_SALE` | 登录用户 |
| `GET` | `/api/orders/buy` | 查询我购买的订单 | 登录用户 |
| `GET` | `/api/orders/sell` | 查询我出售的订单 | 登录用户 |
| `PUT` | `/api/orders/{id}/cancel` | 买家取消 `CREATED` 状态订单，商品恢复为 `ON_SALE` | 订单买家 |
| `PUT` | `/api/orders/{id}/complete` | 卖家确认 `CREATED` 状态订单完成，商品变为 `SOLD` | 订单卖家 |

## 5. 评价接口

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `POST` | `/api/reviews` | 买家对已完成订单评价卖家，一笔订单只能评价一次 | 订单买家 |
| `GET` | `/api/reviews/user/{userId}` | 查询某个用户收到的评价 | 公开 |

评价星级会影响卖家信用分：5 星 +2，4 星 +1，3 星不变，2 星 -2，1 星 -5，最终分数限制在 0-150。

## 6. 收藏与浏览历史接口

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `POST` | `/api/favorites/{goodsId}` | 收藏商品，重复收藏通过唯一索引保证幂等 | 登录用户 |
| `DELETE` | `/api/favorites/{goodsId}` | 取消收藏 | 登录用户 |
| `GET` | `/api/favorites/my` | 查询我的收藏列表 | 登录用户 |
| `GET` | `/api/favorites/check/{goodsId}` | 查询当前用户是否收藏某商品 | 登录用户 |
| `GET` | `/api/history/my` | 查询我的浏览历史 | 登录用户 |

浏览历史由商品详情接口触发记录，重复浏览同一商品会更新时间。

## 7. 管理后台接口

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `GET` | `/api/admin/users` | 查询用户列表 | ADMIN |
| `PUT` | `/api/admin/users/{id}/ban` | 封禁用户，代码中禁止封禁管理员 | ADMIN |
| `PUT` | `/api/admin/users/{id}/unban` | 解封用户 | ADMIN |
| `GET` | `/api/admin/goods/pending` | 查询待审核商品 | ADMIN |
| `GET` | `/api/admin/goods` | 查询全部商品 | ADMIN |
| `PUT` | `/api/admin/goods/{id}/approve` | 审核通过商品，状态变为 `ON_SALE` | ADMIN |
| `PUT` | `/api/admin/goods/{id}/reject` | 驳回商品，状态变为 `REJECTED` | ADMIN |
| `PUT` | `/api/admin/goods/{id}/off-shelf` | 管理员违规下架商品，状态变为 `OFF_SHELF` | ADMIN |
| `GET` | `/api/admin/stat/overview` | 统计用户数、商品数、订单数、成交金额 | ADMIN |
| `GET` | `/api/admin/stat/goods-status` | 商品状态分布 | ADMIN |
| `GET` | `/api/admin/stat/category-goods` | 分类商品数量 | ADMIN |
| `GET` | `/api/admin/stat/order-trend` | 最近 7 日订单趋势 | ADMIN |

## 8. 智能交易助手接口

当前 Agent 是本地规则版实现，不依赖外部大模型 API。所有 `/api/agent/**` 接口都要求登录。

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| `POST` | `/api/agent/publish-advice` | 根据标题、描述、分类、价格等生成发布建议 | 登录用户 |
| `POST` | `/api/agent/price-advice` | 根据原价、成色、分类和平台同类商品给出价格区间 | 登录用户 |
| `GET` | `/api/agent/goods/{goodsId}/risk` | 分析商品交易风险 | 登录用户 |
| `GET` | `/api/agent/recommend` | 根据收藏和浏览历史生成个性化推荐 | 登录用户 |
| `POST` | `/api/agent/chat` | 规则问答，支持购买判断、价格判断、分类推荐等问题 | 登录用户 |
