package com.campus.trade.dto;

import java.math.BigDecimal;

public final class StatDtos {
    private StatDtos() {}

    public record Overview(long userCount, long goodsCount, long orderCount, BigDecimal totalAmount) {}
    public record NameValue(String name, long value) {}
    public record DateValue(String date, long value) {}
}
