package com.campus.trade.controller;

import com.campus.trade.common.ApiResponse;
import com.campus.trade.entity.Goods;
import com.campus.trade.entity.User;
import com.campus.trade.service.AdminService;
import com.campus.trade.service.StatService;
import com.campus.trade.dto.StatDtos.DateValue;
import com.campus.trade.dto.StatDtos.NameValue;
import com.campus.trade.dto.StatDtos.Overview;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final StatService statService;

    @GetMapping("/users")
    public ApiResponse<List<User>> users() {
        return ApiResponse.success(adminService.users());
    }

    @PutMapping("/users/{id}/ban")
    public ApiResponse<Void> ban(@PathVariable Long id) {
        adminService.userStatus(id, "BANNED");
        return ApiResponse.success();
    }

    @PutMapping("/users/{id}/unban")
    public ApiResponse<Void> unban(@PathVariable Long id) {
        adminService.userStatus(id, "NORMAL");
        return ApiResponse.success();
    }

    @GetMapping("/goods/pending")
    public ApiResponse<List<Goods>> pending() {
        return ApiResponse.success(adminService.pending());
    }

    @GetMapping("/goods")
    public ApiResponse<List<Goods>> goods() {
        return ApiResponse.success(adminService.goods());
    }

    @PutMapping("/goods/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        adminService.goodsStatus(id, "ON_SALE");
        return ApiResponse.success();
    }

    @PutMapping("/goods/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        adminService.goodsStatus(id, "REJECTED");
        return ApiResponse.success();
    }

    @PutMapping("/goods/{id}/off-shelf")
    public ApiResponse<Void> offShelf(@PathVariable Long id) {
        adminService.goodsStatus(id, "OFF_SHELF");
        return ApiResponse.success();
    }

    @GetMapping("/stat/overview")
    public ApiResponse<Overview> overview() {
        return ApiResponse.success(statService.overview());
    }

    @GetMapping("/stat/goods-status")
    public ApiResponse<List<NameValue>> goodsStatus() {
        return ApiResponse.success(statService.goodsStatus());
    }

    @GetMapping("/stat/category-goods")
    public ApiResponse<List<NameValue>> categoryGoods() {
        return ApiResponse.success(statService.categoryGoods());
    }

    @GetMapping("/stat/order-trend")
    public ApiResponse<List<DateValue>> orderTrend() {
        return ApiResponse.success(statService.orderTrend());
    }
}
