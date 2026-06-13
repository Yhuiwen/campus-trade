package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.ReviewRequest;
import com.campus.trade.entity.Review;
import com.campus.trade.entity.TradeOrder;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.ReviewMapper;
import com.campus.trade.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final OrderService orderService;
    private static final Map<Integer, Integer> CREDIT_CHANGE = Map.of(5, 2, 4, 1, 3, 0, 2, -2, 1, -5);

    @Transactional
    public void create(ReviewRequest request, Long reviewerId) {
        TradeOrder order = orderService.get(request.orderId());
        if (!order.getBuyerId().equals(reviewerId)) throw new BusinessException(403, "只有买家可以评价");
        if (!"COMPLETED".equals(order.getStatus())) throw new BusinessException("订单完成后才能评价");
        if (reviewMapper.selectCount(new LambdaQueryWrapper<Review>().eq(Review::getOrderId, order.getId())) > 0) {
            throw new BusinessException("该订单已评价");
        }
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setGoodsId(order.getGoodsId());
        review.setReviewerId(reviewerId);
        review.setTargetUserId(order.getSellerId());
        review.setRating(request.rating());
        review.setContent(request.content());
        review.setCreateTime(LocalDateTime.now());
        reviewMapper.insert(review);

        User seller = userMapper.selectById(order.getSellerId());
        seller.setCreditScore(Math.max(0, Math.min(150,
                seller.getCreditScore() + CREDIT_CHANGE.get(request.rating()))));
        seller.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(seller);
    }

    public List<Review> byUser(Long userId) {
        return reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetUserId, userId).orderByDesc(Review::getCreateTime));
    }
}
