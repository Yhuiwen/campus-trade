package com.campus.trade.agent.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PublishAdviceRequest(
        String title,
        String description,
        @NotNull(message = "商品分类不能为空") Long categoryId,
        @DecimalMin(value = "0.01", message = "原价必须大于0") BigDecimal originalPrice,
        @DecimalMin(value = "0.01", message = "期望价格必须大于0") BigDecimal expectedPrice) {}
