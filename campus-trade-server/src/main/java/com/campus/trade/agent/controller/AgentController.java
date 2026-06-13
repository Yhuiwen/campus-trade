package com.campus.trade.agent.controller;

import com.campus.trade.agent.dto.AgentChatRequest;
import com.campus.trade.agent.dto.PriceAdviceRequest;
import com.campus.trade.agent.dto.PublishAdviceRequest;
import com.campus.trade.agent.service.AgentService;
import com.campus.trade.agent.service.PriceAdvisorService;
import com.campus.trade.agent.service.RecommendationService;
import com.campus.trade.agent.service.RiskAnalyzeService;
import com.campus.trade.agent.vo.AgentChatVo;
import com.campus.trade.agent.vo.PriceAdviceVo;
import com.campus.trade.agent.vo.PublishAdviceVo;
import com.campus.trade.agent.vo.RecommendGoodsVo;
import com.campus.trade.agent.vo.RiskAnalyzeVo;
import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;
    private final PriceAdvisorService priceAdvisorService;
    private final RiskAnalyzeService riskAnalyzeService;
    private final RecommendationService recommendationService;

    @PostMapping("/publish-advice")
    public ApiResponse<PublishAdviceVo> publishAdvice(@Valid @RequestBody PublishAdviceRequest request) {
        return ApiResponse.success(agentService.publishAdvice(request));
    }

    @PostMapping("/price-advice")
    public ApiResponse<PriceAdviceVo> priceAdvice(@Valid @RequestBody PriceAdviceRequest request) {
        return ApiResponse.success(priceAdvisorService.advise(request));
    }

    @GetMapping("/goods/{goodsId}/risk")
    public ApiResponse<RiskAnalyzeVo> risk(@PathVariable Long goodsId) {
        return ApiResponse.success(riskAnalyzeService.analyze(goodsId));
    }

    @GetMapping("/recommend")
    public ApiResponse<List<RecommendGoodsVo>> recommend() {
        return ApiResponse.success(recommendationService.recommend(CurrentUser.id()));
    }

    @PostMapping("/chat")
    public ApiResponse<AgentChatVo> chat(@Valid @RequestBody AgentChatRequest request) {
        return ApiResponse.success(agentService.chat(request, CurrentUser.id()));
    }
}
