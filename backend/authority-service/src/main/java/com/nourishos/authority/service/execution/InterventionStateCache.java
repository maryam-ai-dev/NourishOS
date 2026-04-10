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
public class InterventionStateCache {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void writeIntervention(UUID executionId, UUID interventionId, UUID stepId,
                                   String interventionType, String message) {
        String key = RedisConfig.EXEC_INTERVENTION_PREFIX + executionId;
        String value = toJson(new InterventionData(
                interventionId.toString(), stepId.toString(), interventionType, message, "PENDING"));
        redisTemplate.opsForValue().set(key, value);
    }

    public void deleteIntervention(UUID executionId) {
        redisTemplate.delete(RedisConfig.EXEC_INTERVENTION_PREFIX + executionId);
    }

    public String getIntervention(UUID executionId) {
        return redisTemplate.opsForValue().get(RedisConfig.EXEC_INTERVENTION_PREFIX + executionId);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    public record InterventionData(String interventionId, String stepId,
                                    String interventionType, String message, String status) {}
}
