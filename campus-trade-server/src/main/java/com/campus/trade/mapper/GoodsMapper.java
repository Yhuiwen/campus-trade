package com.campus.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.trade.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface GoodsMapper extends BaseMapper<Goods> {
    @Update("UPDATE goods SET status = 'LOCKED', update_time = NOW() WHERE id = #{id} AND status = 'ON_SALE'")
    int lockIfOnSale(@Param("id") Long id);

    @Update("UPDATE goods SET status = 'ON_SALE', update_time = NOW() WHERE id = #{id} AND status = 'LOCKED'")
    int restoreIfLocked(@Param("id") Long id);

    @Update("UPDATE goods SET status = 'SOLD', update_time = NOW() WHERE id = #{id} AND status = 'LOCKED'")
    int soldIfLocked(@Param("id") Long id);
}
