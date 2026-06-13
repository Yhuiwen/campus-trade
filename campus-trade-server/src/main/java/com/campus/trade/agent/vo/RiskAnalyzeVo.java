package com.campus.trade.agent.vo;

import java.util.List;

public record RiskAnalyzeVo(
        String riskLevel,
        int score,
        List<String> reasons,
        String advice) {}
