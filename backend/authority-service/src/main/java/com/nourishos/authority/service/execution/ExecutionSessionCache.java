package com.nourishos.authority.service.execution;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.config.RedisConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExecutionSessionCache {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void writeSession(UUID executionId, int currentStepIndex, String status, String startedAt) {
        String key = RedisConfig.EXEC_SESSION_PREFIX + executionId;
        String value = toJson(new SessionData(currentStepIndex, status, startedAt));
        redisTemplate.opsForValue().set(key, value);
    }

    public void updateStepIndex(UUID executionId, int newStepIndex, String status) {
        String key = RedisConfig.EXEC_SESSION_PREFIX + executionId;
        String existing = redisTemplate.opsForValue().get(key);
        if (existing != null) {
            try {
                SessionData data = objectMapper.readValue(existing, SessionData.class);
                String updated = toJson(new SessionData(newStepIndex, status, data.startedAt()));
                redisTemplate.opsForValue().set(key, updated);
            } catch (JsonProcessingException e) {
                // overwrite with new data
                redisTemplate.opsForValue().set(key, toJson(new SessionData(newStepIndex, status, null)));
            }
        }
    }

    public void deleteSession(UUID executionId) {
        redisTemplate.delete(RedisConfig.EXEC_SESSION_PREFIX + executionId);
    }

    public String getSession(UUID executionId) {
        return redisTemplate.opsForValue().get(RedisConfig.EXEC_SESSION_PREFIX + executionId);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public record SessionData(int stepIndex, String status, String startedAt) {}
}
