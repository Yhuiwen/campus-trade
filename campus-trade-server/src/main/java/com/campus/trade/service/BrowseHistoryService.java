package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.entity.BrowseHistory;
import com.campus.trade.mapper.BrowseHistoryMapper;
import com.campus.trade.mapper.GoodsMapper;
import com.campus.trade.vo.GoodsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BrowseHistoryService {
    private final BrowseHistoryMapper historyMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsService goodsService;

    public void record(Long userId, Long goodsId) {
        BrowseHistory history = historyMapper.selectOne(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getGoodsId, goodsId));
        if (history == null) {
            history = new BrowseHistory();
            history.setUserId(userId);
            history.setGoodsId(goodsId);
        }
        history.setCreateTime(LocalDateTime.now());
        if (history.getId() == null) {
            try {
                historyMapper.insert(history);
            } catch (DuplicateKeyException ignored) {
                historyMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .eq(BrowseHistory::getGoodsId, goodsId)
                        .set(BrowseHistory::getCreateTime, LocalDateTime.now()));
            }
        } else {
            historyMapper.updateById(history);
        }
    }

    public List<GoodsVo> my(Long userId) {
        return historyMapper.selectList(new LambdaQueryWrapper<BrowseHistory>()
                        .eq(BrowseHistory::getUserId, userId)
                        .orderByDesc(BrowseHistory::getCreateTime)
                        .last("LIMIT 50"))
                .stream()
                .map(BrowseHistory::getGoodsId)
                .map(goodsMapper::selectById)
                .filter(java.util.Objects::nonNull)
                .map(goodsService::toVo)
                .toList();
    }
}
