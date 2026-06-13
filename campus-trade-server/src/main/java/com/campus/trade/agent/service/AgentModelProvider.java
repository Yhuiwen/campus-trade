package com.campus.trade.agent.service;

import com.campus.trade.agent.dto.AgentChatRequest;
import com.campus.trade.agent.vo.AgentChatVo;

/**
 * Extension point for a future LLM provider. The local rule engine is used
 * whenever no available provider bean is registered.
 */
public interface AgentModelProvider {
    boolean available();
    AgentChatVo chat(AgentChatRequest request, Long userId);
}
