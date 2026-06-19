package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.entity.Goods;
import com.campus.trade.entity.Review;
import com.campus.trade.entity.TradeOrder;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.mapper.ReviewMapper;
import com.campus.trade.mapper.TradeOrderMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.vo.OrderVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final TradeOrderMapper orderMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final ReviewMapper reviewMapper;
    private final CacheService cacheService;

    @Transactional
    public TradeOrder create(Long goodsId, Long buyerId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        if (goods.getSellerId().equals(buyerId)) throw new BusinessException("不能购买自己的商品");
        if (!"ON_SALE".equals(goods.getStatus())) throw new BusinessException("商品当前不可购买");
        if (goodsMapper.lockIfOnSale(goodsId) == 0) throw new BusinessException("商品当前不可购买");
        TradeOrder order = new TradeOrder();
        order.setOrderNo("CT" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase());
        order.setGoodsId(goodsId);
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getSellerId());
        order.setAmount(goods.getPrice());
        order.setStatus("CREATED");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        cacheService.evictGoods(goodsId);
        return order;
    }

    public List<OrderVo> buy(Long userId) {
        return list(new LambdaQueryWrapper<TradeOrder>().eq(TradeOrder::getBuyerId, userId));
    }

    public List<OrderVo> sell(Long userId) {
        return list(new LambdaQueryWrapper<TradeOrder>().eq(TradeOrder::getSellerId, userId));
    }

    @Transactional
    public void cancel(Long id, Long userId) {
        TradeOrder order = get(id);
        if (!order.getBuyerId().equals(userId)) throw new BusinessException(403, "只有买家可以取消订单");
        ensureCreated(order);
        order.setStatus("CANCELLED");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        goodsMapper.restoreIfLocked(order.getGoodsId());
        cacheService.evictGoods(order.getGoodsId());
    }

    @Transactional
    public void complete(Long id, Long userId) {
        TradeOrder order = get(id);
        if (!order.getSellerId().equals(userId)) throw new BusinessException(403, "只有卖家可以确认完成");
        ensureCreated(order);
        order.setStatus("COMPLETED");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        if (goodsMapper.soldIfLocked(order.getGoodsId()) == 0) {
            throw new BusinessException("商品状态异常，无法完成订单");
        }
        cacheService.evictGoods(order.getGoodsId());
    }

    public TradeOrder get(Long id) {
        TradeOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        return order;
    }

    private void ensureCreated(TradeOrder order) {
        if (!"CREATED".equals(order.getStatus())) throw new BusinessException("订单当前状态不可操作");
    }

    private List<OrderVo> list(LambdaQueryWrapper<TradeOrder> query) {
        List<TradeOrder> orders = orderMapper.selectList(query.orderByDesc(TradeOrder::getCreateTime));
        if (orders.isEmpty()) return List.of();

        Set<Long> goodsIds = orders.stream().map(TradeOrder::getGoodsId).collect(Collectors.toSet());
        Set<Long> userIds = new HashSet<>();
        orders.forEach(order -> {
            userIds.add(order.getBuyerId());
            userIds.add(order.getSellerId());
        });
        Set<Long> orderIds = orders.stream().map(TradeOrder::getId).collect(Collectors.toSet());

        Map<Long, Goods> goodsMap = goodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, Function.identity()));
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Set<Long> reviewedOrderIds = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                        .in(Review::getOrderId, orderIds))
                .stream().map(Review::getOrderId).collect(Collectors.toSet());

        return orders.stream().map(order -> {
            OrderVo vo = new OrderVo();
            BeanUtils.copyProperties(order, vo);
            Goods goods = goodsMap.get(order.getGoodsId());
            User buyer = userMap.get(order.getBuyerId());
            User seller = userMap.get(order.getSellerId());
            if (goods != null) {
                vo.setGoodsTitle(goods.getTitle());
                vo.setGoodsImageUrl(goods.getImageUrl());
            }
            if (buyer != null) vo.setBuyerNickname(buyer.getNickname());
            if (seller != null) vo.setSellerNickname(seller.getNickname());
            vo.setReviewed(reviewedOrderIds.contains(order.getId()));
            return vo;
        }).toList();
    }
}
