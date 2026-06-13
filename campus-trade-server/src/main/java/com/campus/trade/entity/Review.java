package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    private Long id;
    private Long orderId;
    private Long goodsId;
    private Long reviewerId;
    private Long targetUserId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
