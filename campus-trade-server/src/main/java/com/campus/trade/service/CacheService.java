package com.campus.trade.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CacheService {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, TypeReference<T> type, Supplier<T> loader) {
        try {
            String value = redis.opsForValue().get(key);
            if (value != null) return objectMapper.readValue(value, type);
        } catch (Exception ignored) {}
        T data = loader.get();
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(data), Duration.ofMinutes(10));
        } catch (Exception ignored) {}
        return data;
    }

    public void evictGoods(Long id) {
        try {
            redis.delete("goods:detail:" + id);
            redis.delete("goods:hot");
        } catch (Exception ignored) {}
    }
}
