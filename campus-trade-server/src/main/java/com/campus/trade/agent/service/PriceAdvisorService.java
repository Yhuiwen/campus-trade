package com.campus.trade.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.agent.dto.PriceAdviceRequest;
import com.campus.trade.agent.vo.PriceAdviceVo;
import com.campus.trade.common.BusinessException;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.Goods;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceAdvisorService {
    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;

    public PriceAdviceVo advise(PriceAdviceRequest request) {
        Category category = categoryMapper.selectById(request.categoryId());
        if (category == null) throw new BusinessException("商品分类不存在");

        BigDecimal average = categoryAverage(request.categoryId());
        String description = request.description() == null ? "" : request.description().toLowerCase();
        BigDecimal minRate = new BigDecimal("0.50");
        BigDecimal maxRate = new BigDecimal("0.70");
        String condition = "按普通二手成色估算";

        if (containsAny(description, "全新", "未拆封", "仅拆封")) {
            minRate = new BigDecimal("0.75");
            maxRate = new BigDecimal("0.90");
            condition = "描述显示商品接近全新";
        } else if (containsAny(description, "九成新", "95新", "9成新")) {
            minRate = new BigDecimal("0.60");
            maxRate = new BigDecimal("0.75");
            condition = "描述显示商品约九成新";
        } else if (containsAny(description, "八成新", "8成新")) {
            minRate = new BigDecimal("0.45");
            maxRate = new BigDecimal("0.60");
            condition = "描述显示商品约八成新";
        } else if (containsAny(description, "有划痕", "损坏", "维修", "故障", "磕碰")) {
            minRate = new BigDecimal("0.20");
            maxRate = new BigDecimal("0.40");
            condition = "描述包含瑕疵或维修情况";
        } else if (containsAny(description, "使用一年", "一年以上")) {
            minRate = new BigDecimal("0.35");
            maxRate = new BigDecimal("0.55");
            condition = "商品使用时间较长";
        }

        BigDecimal base = request.originalPrice();
        String reference;
        if (base == null) {
            base = average != null ? average : new BigDecimal("100");
            reference = average != null ? "未填写原价，使用平台同类均价作为参考" : "平台暂无同类样本，使用基础参考价";
        } else {
            reference = "以商品原价为基础";
        }

        BigDecimal min = money(base.multiply(minRate));
        BigDecimal max = money(base.multiply(maxRate));
        if (average != null) {
            BigDecimal upperReference = average.multiply(new BigDecimal("1.20"));
            if (min.compareTo(upperReference) > 0) {
                min = money(min.multiply(new BigDecimal("0.65"))
                        .add(average.multiply(new BigDecimal("0.35"))));
                max = money(max.multiply(new BigDecimal("0.65"))
                        .add(upperReference.multiply(new BigDecimal("0.35"))));
                reference += "，计算结果高于平台同类价格，已按成色价与市场均价加权下调";
            } else if (max.compareTo(average.multiply(new BigDecimal("0.50"))) < 0
                    && !containsAny(description, "有划痕", "损坏", "维修", "故障")) {
                min = money(average.multiply(new BigDecimal("0.55")));
                max = money(average.multiply(new BigDecimal("0.80")));
                reference += "，计算结果明显低于同类价格，已向市场区间修正";
            }
        }
        if (max.compareTo(min) < 0) max = min;
        BigDecimal suggested = money(min.add(max).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP));
        String averageText = average == null ? "暂无足够的同类在售样本"
                : "平台“" + category.getName() + "”在售商品均价约为 ¥" + money(average);
        return new PriceAdviceVo(min, max, suggested,
                condition + "；" + reference + "；" + averageText + "。");
    }

    public BigDecimal categoryAverage(Long categoryId) {
        List<Goods> goods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getCategoryId, categoryId)
                .eq(Goods::getStatus, "ON_SALE"));
        if (goods.isEmpty()) return null;
        BigDecimal sum = goods.stream().map(Goods::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return money(sum.divide(BigDecimal.valueOf(goods.size()), 2, RoundingMode.HALF_UP));
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) if (text.contains(keyword)) return true;
        return false;
    }

    private BigDecimal money(BigDecimal value) {
        return value.max(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
    }
}
