package com.campus.trade.vo;

import com.campus.trade.entity.Goods;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsVo extends Goods {
    private String sellerNickname;
    private Integer sellerCreditScore;
    private String categoryName;
    private Boolean favorited;
}
