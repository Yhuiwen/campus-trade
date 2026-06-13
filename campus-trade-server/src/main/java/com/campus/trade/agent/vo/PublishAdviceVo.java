package com.campus.trade.agent.vo;

import java.math.BigDecimal;
import java.util.List;

public record PublishAdviceVo(
        String optimizedTitle,
        String optimizedDescription,
        BigDecimal suggestedPrice,
        String priceRange,
        List<String> adviceList,
        boolean canPublish) {}
