package com.campus.trade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.CurrentUser;
import com.campus.trade.dto.GoodsRequest;
import com.campus.trade.entity.Goods;
import com.campus.trade.service.GoodsService;
import com.campus.trade.service.BrowseHistoryService;
import com.campus.trade.service.FavoriteService;
import com.campus.trade.vo.GoodsVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {
    private final GoodsService goodsService;
    private final FavoriteService favoriteService;
    private final BrowseHistoryService historyService;

    @GetMapping("/page")
    public ApiResponse<IPage<GoodsVo>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.success(goodsService.page(current, size, keyword, categoryId));
    }

    @GetMapping("/my")
    public ApiResponse<IPage<GoodsVo>> my(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "12") long size) {
        return ApiResponse.success(goodsService.myGoods(current, size, CurrentUser.id()));
    }

    @GetMapping("/hot")
    public ApiResponse<List<GoodsVo>> hot() {
        return ApiResponse.success(goodsService.hot());
    }

    @GetMapping("/{id}")
    public ApiResponse<GoodsVo> detail(@PathVariable Long id) {
        GoodsVo goods = goodsService.detail(id);
        Long userId = CurrentUser.optionalId();
        if (userId != null) {
            goods.setFavorited(favoriteService.check(userId, id));
            historyService.record(userId, id);
        } else {
            goods.setFavorited(false);
        }
        return ApiResponse.success(goods);
    }

    @PostMapping
    public ApiResponse<Goods> create(@Valid @RequestBody GoodsRequest request) {
        return ApiResponse.success(goodsService.create(request, CurrentUser.id()));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody GoodsRequest request) {
        goodsService.update(id, request, CurrentUser.id());
        return ApiResponse.success();
    }

    @PutMapping("/{id}/off-shelf")
    public ApiResponse<Void> offShelf(@PathVariable Long id) {
        goodsService.offShelf(id, CurrentUser.id());
        return ApiResponse.success();
    }
}
