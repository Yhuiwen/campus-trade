package com.campus.trade.agent.vo;

import com.campus.trade.vo.GoodsVo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecommendGoodsVo extends GoodsVo {
    private String recommendationReason;
}
