package com.campus.trade.vo;

import com.campus.trade.entity.TradeOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVo extends TradeOrder {
    private String goodsTitle;
    private String goodsImageUrl;
    private String buyerNickname;
    private String sellerNickname;
    private Boolean reviewed;
}
