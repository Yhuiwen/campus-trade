package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.dto.StatDtos.DateValue;
import com.campus.trade.dto.StatDtos.NameValue;
import com.campus.trade.dto.StatDtos.Overview;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.Goods;
import com.campus.trade.entity.TradeOrder;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.mapper.TradeOrderMapper;
import com.campus.trade.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatService {
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final TradeOrderMapper orderMapper;
    private final CategoryMapper categoryMapper;

    public Overview overview() {
        List<TradeOrder> completed = orderMapper.selectList(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getStatus, "COMPLETED"));
        BigDecimal totalAmount = completed.stream()
                .map(TradeOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Overview(userMapper.selectCount(null), goodsMapper.selectCount(null),
                orderMapper.selectCount(null), totalAmount);
    }

    public List<NameValue> goodsStatus() {
        Map<String, Long> counts = goodsMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(Goods::getStatus, Collectors.counting()));
        return List.of("PENDING", "ON_SALE", "SOLD", "OFF_SHELF", "REJECTED").stream()
                .map(status -> new NameValue(status, counts.getOrDefault(status, 0L)))
                .toList();
    }

    public List<NameValue> categoryGoods() {
        Map<Long, Long> counts = goodsMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(Goods::getCategoryId, Collectors.counting()));
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder))
                .stream()
                .map(category -> new NameValue(category.getName(), counts.getOrDefault(category.getId(), 0L)))
                .toList();
    }

    public List<DateValue> orderTrend() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        Map<LocalDate, Long> counts = orderMapper.selectList(new LambdaQueryWrapper<TradeOrder>()
                        .ge(TradeOrder::getCreateTime, start.atStartOfDay())
                        .lt(TradeOrder::getCreateTime, today.plusDays(1).atStartOfDay()))
                .stream()
                .collect(Collectors.groupingBy(order -> order.getCreateTime().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Long> ordered = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            ordered.put(date, counts.getOrDefault(date, 0L));
        }
        return ordered.entrySet().stream()
                .map(entry -> new DateValue(entry.getKey().toString(), entry.getValue()))
                .toList();
    }
}
