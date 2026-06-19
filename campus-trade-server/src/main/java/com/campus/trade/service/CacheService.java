package com.campus.trade.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, TypeReference<T> type, Supplier<T> loader) {
        try {
            String value = redis.opsForValue().get(key);
            if (value != null) return objectMapper.readValue(value, type);
        } catch (Exception e) {
            log.warn("Redis 读取缓存失败，key={}，将回源查询: {}", key, e.getMessage());
        }
        T data = loader.get();
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(data), Duration.ofMinutes(10));
        } catch (Exception e) {
            log.warn("Redis 写入缓存失败，key={}: {}", key, e.getMessage());
        }
        return data;
    }

    public void evictGoods(Long id) {
        try {
            redis.delete("goods:detail:" + id);
            redis.delete("goods:hot");
        } catch (Exception e) {
            log.warn("Redis 清理商品缓存失败，goodsId={}: {}", id, e.getMessage());
        }
    }
}
