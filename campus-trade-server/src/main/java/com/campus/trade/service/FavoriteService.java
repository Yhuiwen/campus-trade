package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.entity.Favorite;
import com.campus.trade.entity.Goods;
import com.campus.trade.mapper.FavoriteMapper;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.vo.GoodsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;

    public void add(Long userId, Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) throw new BusinessException("商品不存在");
        if (check(userId, goodsId)) return;
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setCreateTime(LocalDateTime.now());
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException ignored) {
            // Concurrent duplicate requests are idempotent.
        }
    }

    public void remove(Long userId, Long goodsId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId));
    }

    public boolean check(Long userId, Long goodsId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId)) > 0;
    }

    public List<GoodsVo> my(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));
        return favorites.stream()
                .map(Favorite::getGoodsId)
                .map(goodsMapper::selectById)
                .filter(java.util.Objects::nonNull)
                .map(goods -> {
                    GoodsVo vo = goodsService.toVo(goods);
                    vo.setFavorited(true);
                    return vo;
                }).toList();
    }
}
