package com.campus.trade.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.agent.vo.RiskAnalyzeVo;
import com.campus.trade.common.BusinessException;
import com.campus.trade.entity.Goods;
import com.campus.trade.entity.Review;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.mapper.ReviewMapper;
import com.campus.trade.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskAnalyzeService {
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final ReviewMapper reviewMapper;
    private final PriceAdvisorService priceAdvisorService;

    public RiskAnalyzeVo analyze(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        User seller = userMapper.selectById(goods.getSellerId());
        List<String> reasons = new ArrayList<>();
        int score = 0;

        if (seller == null || seller.getCreditScore() < 60) {
            score += 40;
            reasons.add("卖家信用分低于 60，建议优先线下验货");
        } else if (seller.getCreditScore() < 80) {
            score += 18;
            reasons.add("卖家信用分偏低，交易前应核实商品情况");
        }

        BigDecimal average = priceAdvisorService.categoryAverage(goods.getCategoryId());
        boolean unusuallyLow = average != null
                && goods.getPrice().compareTo(average.multiply(new BigDecimal("0.50"))) < 0;
        if (unusuallyLow) {
            score += 25;
            reasons.add("价格低于同类在售均价的 50%，需警惕异常低价");
        }
        if (goods.getImageUrl() == null || goods.getImageUrl().isBlank()) {
            score += 15;
            reasons.add("商品缺少实物图片");
        }
        if (goods.getDescription() == null || goods.getDescription().trim().length() < 20) {
            score += 15;
            reasons.add("商品描述少于 20 字，关键信息可能不完整");
        }
        boolean justPublished = goods.getCreateTime() != null
                && goods.getCreateTime().isAfter(LocalDateTime.now().minusHours(24));
        if (justPublished && unusuallyLow) {
            score += 15;
            reasons.add("商品刚发布且价格异常低，建议确认卖家身份和商品来源");
        }

        long badReviews = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetUserId, goods.getSellerId())
                .le(Review::getRating, 2));
        if (badReviews >= 3) {
            score += 25;
            reasons.add("卖家存在较多低分评价（" + badReviews + " 条）");
        } else if (badReviews > 0) {
            score += 8;
            reasons.add("卖家存在 " + badReviews + " 条低分评价，建议查看评价内容");
        }

        score = Math.min(100, score);
        String level = score >= 55 ? "HIGH" : score >= 25 ? "MEDIUM" : "LOW";
        if (reasons.isEmpty()) reasons.add("暂未发现明显异常，仍建议当面验货后交易");
        String advice = switch (level) {
            case "HIGH" -> "风险较高，不建议直接付款；请线下验货并核实卖家身份。";
            case "MEDIUM" -> "存在部分风险信号，建议补充沟通、查看实物后再决定。";
            default -> "当前风险较低，可在确认实物、价格和交易地点后购买。";
        };
        return new RiskAnalyzeVo(level, score, reasons, advice);
    }
}
