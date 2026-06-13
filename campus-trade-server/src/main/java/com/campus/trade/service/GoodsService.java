package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.GoodsRequest;
import com.campus.trade.entity.Category;
import com.campus.trade.entity.Goods;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.CategoryMapper;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.vo.GoodsVo;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsService {
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final CacheService cacheService;

    public IPage<GoodsVo> page(long current, long size, String keyword, Long categoryId, Long sellerId) {
        LambdaQueryWrapper<Goods> query = new LambdaQueryWrapper<Goods>()
                .eq(sellerId == null, Goods::getStatus, "ON_SALE")
                .eq(categoryId != null, Goods::getCategoryId, categoryId)
                .eq(sellerId != null, Goods::getSellerId, sellerId)
                .and(keyword != null && !keyword.isBlank(),
                        q -> q.like(Goods::getTitle, keyword).or().like(Goods::getDescription, keyword))
                .orderByDesc(Goods::getCreateTime);
        IPage<Goods> source = goodsMapper.selectPage(new Page<>(current, size), query);
        Page<GoodsVo> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(source.getRecords().stream().map(this::toVo).toList());
        return result;
    }

    public GoodsVo detail(Long id) {
        return cacheService.get("goods:detail:" + id, new TypeReference<>() {}, () -> {
            Goods goods = goodsMapper.selectById(id);
            if (goods == null) throw new BusinessException("商品不存在");
            goods.setViewCount(goods.getViewCount() + 1);
            goodsMapper.updateById(goods);
            return toVo(goods);
        });
    }

    public List<GoodsVo> hot() {
        return cacheService.get("goods:hot", new TypeReference<>() {}, () ->
                goodsMapper.selectList(new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, "ON_SALE")
                                .orderByDesc(Goods::getViewCount).last("LIMIT 8"))
                        .stream().map(this::toVo).toList());
    }

    public Goods create(GoodsRequest request, Long sellerId) {
        ensureCategory(request.categoryId());
        Goods goods = new Goods();
        copy(request, goods);
        goods.setSellerId(sellerId);
        goods.setStatus("PENDING");
        goods.setViewCount(0);
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.insert(goods);
        cacheService.evictGoods(goods.getId());
        return goods;
    }

    public void update(Long id, GoodsRequest request, Long userId) {
        Goods goods = owned(id, userId);
        if ("SOLD".equals(goods.getStatus())) throw new BusinessException("已售出商品不能修改");
        ensureCategory(request.categoryId());
        copy(request, goods);
        goods.setStatus("PENDING");
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
        cacheService.evictGoods(id);
    }

    public void offShelf(Long id, Long userId) {
        Goods goods = owned(id, userId);
        if ("SOLD".equals(goods.getStatus())) throw new BusinessException("已售出商品不能下架");
        changeStatus(goods, "OFF_SHELF");
    }

    public Goods owned(Long id, Long userId) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) throw new BusinessException("商品不存在");
        if (!goods.getSellerId().equals(userId)) throw new BusinessException(403, "只能操作自己的商品");
        return goods;
    }

    public void changeStatus(Goods goods, String status) {
        goods.setStatus(status);
        goods.setUpdateTime(LocalDateTime.now());
        goodsMapper.updateById(goods);
        cacheService.evictGoods(goods.getId());
    }

    private void ensureCategory(Long id) {
        if (categoryMapper.selectById(id) == null) throw new BusinessException("分类不存在");
    }

    private void copy(GoodsRequest request, Goods goods) {
        goods.setCategoryId(request.categoryId());
        goods.setTitle(request.title());
        goods.setDescription(request.description());
        goods.setPrice(request.price());
        goods.setOriginalPrice(request.originalPrice());
        goods.setImageUrl(request.imageUrl());
    }

    public GoodsVo toVo(Goods goods) {
        GoodsVo vo = new GoodsVo();
        BeanUtils.copyProperties(goods, vo);
        User seller = userMapper.selectById(goods.getSellerId());
        Category category = categoryMapper.selectById(goods.getCategoryId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
            vo.setSellerCreditScore(seller.getCreditScore());
        }
        if (category != null) vo.setCategoryName(category.getName());
        return vo;
    }
}
