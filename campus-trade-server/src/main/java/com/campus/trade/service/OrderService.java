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
import java.util.List;
import java.util.UUID;

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
        if (goods == null || !"ON_SALE".equals(goods.getStatus())) throw new BusinessException("商品当前不可购买");
        if (goods.getSellerId().equals(buyerId)) throw new BusinessException("不能购买自己的商品");
        Long active = orderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getGoodsId, goodsId).eq(TradeOrder::getStatus, "CREATED"));
        if (active > 0) throw new BusinessException("该商品已有待处理订单");
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
    }

    @Transactional
    public void complete(Long id, Long userId) {
        TradeOrder order = get(id);
        if (!order.getSellerId().equals(userId)) throw new BusinessException(403, "只有卖家可以确认完成");
        ensureCreated(order);
        order.setStatus("COMPLETED");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        goods.setStatus("SOLD");
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
        cacheService.evictGoods(goods.getId());
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
        return orderMapper.selectList(query.orderByDesc(TradeOrder::getCreateTime)).stream().map(order -> {
            OrderVo vo = new OrderVo();
            BeanUtils.copyProperties(order, vo);
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            User buyer = userMapper.selectById(order.getBuyerId());
            User seller = userMapper.selectById(order.getSellerId());
            if (goods != null) {
                vo.setGoodsTitle(goods.getTitle());
                vo.setGoodsImageUrl(goods.getImageUrl());
            }
            if (buyer != null) vo.setBuyerNickname(buyer.getNickname());
            if (seller != null) vo.setSellerNickname(seller.getNickname());
            vo.setReviewed(reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                    .eq(Review::getOrderId, order.getId())) > 0);
            return vo;
        }).toList();
    }
}
