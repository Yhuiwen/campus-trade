package com.campus.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record GoodsRequest(
        @NotNull Long categoryId,
        @NotBlank String title,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotNull @DecimalMin("0.01") BigDecimal originalPrice,
        String imageUrl) {}
