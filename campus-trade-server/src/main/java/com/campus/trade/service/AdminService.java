package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.entity.Goods;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;

    public List<User> users() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));
    }

    public void userStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        if ("ADMIN".equals(user.getRole()) && "BANNED".equals(status)) throw new BusinessException("不能封禁管理员");
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    public List<Goods> pending() {
        return goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, "PENDING").orderByAsc(Goods::getCreateTime));
    }

    public List<Goods> goods() {
        return goodsMapper.selectList(new LambdaQueryWrapper<Goods>().orderByDesc(Goods::getCreateTime));
    }

    public void goodsStatus(Long id, String status) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) throw new BusinessException("商品不存在");
        goodsService.changeStatus(goods, status);
    }
}
