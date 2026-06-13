package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category")
public class Category {
    private Long id;
    private String name;
    private Integer sortOrder;
}
