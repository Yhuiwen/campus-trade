package com.campus.trade.agent.vo;

import java.math.BigDecimal;

public record PriceAdviceVo(
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal suggestedPrice,
        String reason) {}
