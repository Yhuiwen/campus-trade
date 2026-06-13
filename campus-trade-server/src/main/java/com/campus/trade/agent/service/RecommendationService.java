package com.campus.trade.agent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.agent.vo.RecommendGoodsVo;
import com.campus.trade.entity.BrowseHistory;
import com.campus.trade.entity.Favorite;
import com.campus.trade.entity.Goods;
import com.campus.trade.mapper.BrowseHistoryMapper;
import com.campus.trade.mapper.FavoriteMapper;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final BrowseHistoryMapper historyMapper;
    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;

    public List<RecommendGoodsVo> recommend(Long userId) {
        Map<Long, Integer> categoryWeights = categoryWeights(userId);
        List<Goods> candidates = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, "ON_SALE")
                .ne(Goods::getSellerId, userId));
        return candidates.stream()
                .sorted(Comparator.comparingDouble((Goods goods) -> score(goods, categoryWeights)).reversed())
                .limit(8)
                .map(goods -> toRecommendVo(goods, categoryWeights))
                .toList();
    }

    public List<RecommendGoodsVo> byCategory(Long userId, Long categoryId, int limit) {
        return goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                        .eq(Goods::getStatus, "ON_SALE")
                        .eq(categoryId != null, Goods::getCategoryId, categoryId)
                        .ne(Goods::getSellerId, userId)
                        .orderByDesc(Goods::getViewCount)
                        .orderByDesc(Goods::getCreateTime))
                .stream().limit(limit)
                .map(goods -> {
                    RecommendGoodsVo vo = toRecommendVo(goods, Map.of());
                    vo.setRecommendationReason(categoryId == null ? "平台热门商品" : "同分类热门商品");
                    return vo;
                }).toList();
    }

    private Map<Long, Integer> categoryWeights(Long userId) {
        Map<Long, Integer> weights = new HashMap<>();
        List<Long> historyGoods = historyMapper.selectList(new LambdaQueryWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .orderByDesc(BrowseHistory::getCreateTime)
                        .last("LIMIT 30"))
                .stream().map(BrowseHistory::getGoodsId).toList();
        List<Long> favoriteGoods = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId))
                .stream().map(Favorite::getGoodsId).toList();
        addWeights(historyGoods, weights, 2);
        addWeights(favoriteGoods, weights, 4);
        return weights;
    }

    private void addWeights(List<Long> goodsIds, Map<Long, Integer> weights, int value) {
        if (goodsIds.isEmpty()) return;
        Set<Long> ids = goodsIds.stream().collect(Collectors.toSet());
        goodsMapper.selectBatchIds(ids).stream().filter(Objects::nonNull)
                .forEach(goods -> weights.merge(goods.getCategoryId(), value, Integer::sum));
    }

    private double score(Goods goods, Map<Long, Integer> categoryWeights) {
        double preference = categoryWeights.getOrDefault(goods.getCategoryId(), 0) * 1000.0;
        double popularity = Math.log1p(goods.getViewCount() == null ? 0 : goods.getViewCount()) * 50;
        LocalDateTime time = goods.getCreateTime() == null ? LocalDateTime.now().minusYears(1) : goods.getCreateTime();
        double recency = time.toEpochSecond(ZoneOffset.ofHours(8)) / 100000000.0;
        return preference + popularity + recency;
    }

    private RecommendGoodsVo toRecommendVo(Goods goods, Map<Long, Integer> categoryWeights) {
        RecommendGoodsVo vo = new RecommendGoodsVo();
        BeanUtils.copyProperties(goodsService.toVo(goods), vo);
        vo.setRecommendationReason(categoryWeights.containsKey(goods.getCategoryId())
                ? "根据你的收藏和浏览偏好推荐" : "平台热门商品");
        return vo;
    }
}
