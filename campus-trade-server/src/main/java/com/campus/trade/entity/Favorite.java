package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite {
    private Long id;
    private Long userId;
    private Long goodsId;
    private LocalDateTime createTime;
}
