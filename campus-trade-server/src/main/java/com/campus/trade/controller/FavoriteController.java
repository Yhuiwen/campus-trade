package com.campus.trade.controller;

import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.CurrentUser;
import com.campus.trade.service.FavoriteService;
import com.campus.trade.vo.GoodsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    @PostMapping("/{goodsId}")
    public ApiResponse<Void> add(@PathVariable Long goodsId) {
        favoriteService.add(CurrentUser.id(), goodsId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{goodsId}")
    public ApiResponse<Void> remove(@PathVariable Long goodsId) {
        favoriteService.remove(CurrentUser.id(), goodsId);
        return ApiResponse.success();
    }

    @GetMapping("/my")
    public ApiResponse<List<GoodsVo>> my() {
        return ApiResponse.success(favoriteService.my(CurrentUser.id()));
    }

    @GetMapping("/check/{goodsId}")
    public ApiResponse<Map<String, Boolean>> check(@PathVariable Long goodsId) {
        return ApiResponse.success(Map.of("favorited", favoriteService.check(CurrentUser.id(), goodsId)));
    }
}
