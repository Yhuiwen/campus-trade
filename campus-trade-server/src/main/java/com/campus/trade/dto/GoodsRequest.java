package com.campus.trade.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GoodsRequest(
        @NotNull Long categoryId,
        @NotBlank @Size(min = 2, max = 100, message = "标题长度为 2-100 字") String title,
        @NotBlank @Size(min = 5, max = 1000, message = "描述长度为 5-1000 字") String description,
        @NotNull @DecimalMin(value = "0.01", message = "售价必须大于 0") BigDecimal price,
        @NotNull @DecimalMin(value = "0.01", message = "原价必须大于 0") BigDecimal originalPrice,
        String imageUrl) {

    @AssertTrue(message = "售价不能超过原价的 1.2 倍")
    public boolean isPriceReasonable() {
        if (price == null || originalPrice == null) return true;
        return price.compareTo(originalPrice.multiply(new BigDecimal("1.2"))) <= 0;
    }
}
