package com.campus.trade.agent.vo;

import java.util.List;

public record AgentChatVo(
        String answer,
        List<String> suggestions,
        List<RecommendGoodsVo> relatedGoods) {}
