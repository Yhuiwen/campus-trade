package com.campus.trade.agent.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentChatRequest(
        @NotBlank(message = "问题不能为空") String question,
        Long goodsId) {}
