package com.campus.trade.controller;

import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.CurrentUser;
import com.campus.trade.service.BrowseHistoryService;
import com.campus.trade.vo.GoodsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class BrowseHistoryController {
    private final BrowseHistoryService historyService;

    @GetMapping("/my")
    public ApiResponse<List<GoodsVo>> my() {
        return ApiResponse.success(historyService.my(CurrentUser.id()));
    }
}
