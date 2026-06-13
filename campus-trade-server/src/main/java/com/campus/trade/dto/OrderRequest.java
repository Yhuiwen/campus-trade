package com.campus.trade.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(@NotNull Long goodsId) {}
