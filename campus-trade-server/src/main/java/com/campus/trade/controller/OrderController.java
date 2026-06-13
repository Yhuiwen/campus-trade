package com.campus.trade.controller;

import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.CurrentUser;
import com.campus.trade.dto.OrderRequest;
import com.campus.trade.entity.TradeOrder;
import com.campus.trade.service.OrderService;
import com.campus.trade.vo.OrderVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ApiResponse<TradeOrder> create(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.create(request.goodsId(), CurrentUser.id()));
    }

    @GetMapping("/buy")
    public ApiResponse<List<OrderVo>> buy() {
        return ApiResponse.success(orderService.buy(CurrentUser.id()));
    }

    @GetMapping("/sell")
    public ApiResponse<List<OrderVo>> sell() {
        return ApiResponse.success(orderService.sell(CurrentUser.id()));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id, CurrentUser.id());
        return ApiResponse.success();
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id) {
        orderService.complete(id, CurrentUser.id());
        return ApiResponse.success();
    }
}
