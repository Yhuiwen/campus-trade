# 数据库设计说明

## 1. 设计目标

本项目围绕校园二手交易场景设计数据模型，核心对象包括用户、商品、分类、订单、评价、收藏、浏览记录和后台审核。数据库需要支撑用户注册登录、商品发布审核、买家下单、卖家确认完成、买家评价卖家、信用分变化、收藏与浏览历史沉淀，以及后台数据统计。

## 2. 核心表说明

表结构来自 `sql/init.sql`。

| 表名 | 作用 | 关键字段 | 说明 |
| --- | --- | --- | --- |
| `users` | 用户表 | `id`、`username`、`password`、`role`、`credit_score`、`status` | 存储普通用户和管理员；`role` 支持 `USER`、`ADMIN`，`credit_score` 限制在 0-150 |
| `category` | 商品分类表 | `id`、`name`、`sort_order` | 存储数码电子、教材资料、生活用品等分类 |
| `goods` | 商品表 | `id`、`seller_id`、`category_id`、`title`、`price`、`original_price`、`image_url`、`status`、`view_count` | 商品发布、审核、搜索、详情和状态流转的核心表 |
| `favorite` | 收藏表 | `id`、`user_id`、`goods_id`、`create_time` | 使用 `uk_favorite_user_goods` 保证同一用户不能重复收藏同一商品 |
| `browse_history` | 浏览历史表 | `id`、`user_id`、`goods_id`、`create_time` | 使用 `uk_history_user_goods` 保证一名用户对同一商品只保留一条记录，重复浏览更新时间 |
| `trade_order` | 订单表 | `id`、`order_no`、`goods_id`、`buyer_id`、`seller_id`、`amount`、`status` | 记录买家、卖家、商品、金额和订单状态 |
| `review` | 评价表 | `id`、`order_id`、`goods_id`、`reviewer_id`、`target_user_id`、`rating`、`content` | 一笔订单只能评价一次，评分范围为 1-5 |

## 3. 核心关系

- 一个用户可以发布多个商品：`goods.seller_id` 关联 `users.id`。
- 一个商品属于一个分类：`goods.category_id` 关联 `category.id`。
- 一个用户可以买多个订单：`trade_order.buyer_id` 关联 `users.id`。
- 一个用户也可以作为卖家收到多个订单：`trade_order.seller_id` 关联 `users.id`。
- 一笔订单绑定一个商品：`trade_order.goods_id` 关联 `goods.id`。
- 一笔完成订单最多产生一条评价：`review.order_id` 是唯一键，并关联 `trade_order.id`。
- 收藏和浏览历史都通过用户与商品的组合唯一索引保证幂等写入。
- 信用分存储在 `users.credit_score`，由评价结果触发更新。

## 4. 商品状态流转

代码和 SQL 中使用的商品状态包括：

```text
PENDING -> ON_SALE -> LOCKED -> SOLD
       \-> REJECTED
       \-> OFF_SHELF
```

- `PENDING`：用户发布或编辑商品后进入待审核状态。
- `ON_SALE`：管理员审核通过后进入在售状态，可被浏览和下单。
- `LOCKED`：买家创建订单后，商品通过条件更新从 `ON_SALE` 变为交易中，避免重复购买。
- `SOLD`：卖家确认订单完成后，商品从 `LOCKED` 变为已售出。
- `OFF_SHELF`：卖家或管理员下架商品。
- `REJECTED`：管理员驳回待审核商品。

订单状态包括：

```text
CREATED -> CANCELLED
CREATED -> COMPLETED
```

- `CREATED`：买家下单后生成。
- `CANCELLED`：买家取消订单后生成，商品会从 `LOCKED` 恢复为 `ON_SALE`。
- `COMPLETED`：卖家确认完成后生成，商品会变为 `SOLD`。

## 5. 设计亮点

- 商品状态和订单状态分离，避免把审核、交易和订单生命周期混在一张状态字段里。
- 下单时通过 `UPDATE goods SET status = 'LOCKED' WHERE id = ? AND status = 'ON_SALE'` 控制并发购买。
- 收藏和浏览历史使用组合唯一索引，保证重复操作不会产生脏数据。
- 浏览历史重复访问时更新时间，可用于推荐和最近浏览排序。
- 信用分由评价星级驱动，并通过代码限制在 0-150。
- 后台审核通过 `PENDING`、`ON_SALE`、`REJECTED`、`OFF_SHELF` 等状态保证内容质量。
- Redis 只缓存热门商品和商品详情，不替代 MySQL 的最终数据存储。
