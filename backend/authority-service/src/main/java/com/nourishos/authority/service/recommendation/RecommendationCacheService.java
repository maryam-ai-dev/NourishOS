package com.nourishos.authority.service.recommendation;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.config.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${nourishos.intelligence-service.url:http://localhost:8000}")
    private String intelligenceServiceUrl;

    /**
     * Read-through cache for recommendation rankings.
     * On cache hit: returns cached JSON without calling FastAPI.
     * On cache miss: calls FastAPI /recommendation/rank, caches result, returns it.
     */
    public RankResponse getRankedMeals(UUID householdId, Double proteinTarget, int maxResults) {
        String cacheKey = RedisConfig.REC_CACHE_PREFIX + householdId;

        // Try cache first
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("Cache hit for rec:cache:{}", householdId);
                return objectMapper.readValue(cached, RankResponse.class);
            }
        } catch (Exception e) {
            log.warn("Cache read failed for {}: {}", cacheKey, e.getMessage());
        }

        // Cache miss — call FastAPI
        log.info("Cache miss for rec:cache:{}, calling FastAPI", householdId);
        RankResponse response = callFastApiRank(householdId, proteinTarget, maxResults);

        // Write to cache (FastAPI also writes, but we ensure it's there)
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, RedisConfig.REC_CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("Cache write failed for {}: {}", cacheKey, e.getMessage());
        }

        return response;
    }

    /**
     * Check if a cached recommendation exists for the household.
     */
    public boolean hasCachedRecommendation(UUID householdId) {
        String cacheKey = RedisConfig.REC_CACHE_PREFIX + householdId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey));
    }

    private RankResponse callFastApiRank(UUID householdId, Double proteinTarget, int maxResults) {
        String url = intelligenceServiceUrl + "/recommendation/rank";

        RankRequest request = new RankRequest(householdId, proteinTarget, maxResults);

        try {
            String responseJson = restTemplate.postForObject(url, request, String.class);
            return objectMapper.readValue(responseJson, RankResponse.class);
        } catch (Exception e) {
            log.error("FastAPI /recommendation/rank call failed: {}", e.getMessage());
            return new RankResponse(householdId, List.of());
        }
    }

    // DTOs matching FastAPI response shape

    public record RankRequest(UUID householdId, Double proteinTarget, int maxResults) {}

    public record RankResponse(UUID householdId, List<RankedMeal> ranked) {
        public RankResponse {
            if (ranked == null) ranked = List.of();
        }
    }

    public record RankedMeal(UUID mealId, String mealName, double compositeScore,
                             ScoreBreakdown scoreBreakdown) {}

    public record ScoreBreakdown(double preferenceFit, double proteinGoal,
                                  double sustainability, double availability,
                                  double reliability) {}
}
