package com.nourishos.authority.service.recommendation;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nourishos.authority.config.RedisConfig;
import com.nourishos.authority.service.recommendation.RecommendationCacheService.RankResponse;
import com.nourishos.authority.service.recommendation.RecommendationCacheService.RankedMeal;
import com.nourishos.authority.service.recommendation.RecommendationCacheService.ScoreBreakdown;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationCacheServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    private RecommendationCacheService service;

    private final UUID householdId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new RecommendationCacheService(redisTemplate, objectMapper, restTemplate);
    }

    @Test
    void cacheHitReturnsCachedResponseWithoutCallingFastApi() throws Exception {
        String cacheKey = RedisConfig.REC_CACHE_PREFIX + householdId;
        ScoreBreakdown breakdown = new ScoreBreakdown(0.8, 0.7, 0.6, 0.9, 0.75);
        RankedMeal meal = new RankedMeal(UUID.randomUUID(), "Grilled Chicken", 0.78, breakdown);
        RankResponse cached = new RankResponse(householdId, List.of(meal));
        String cachedJson = objectMapper.writeValueAsString(cached);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(cacheKey)).thenReturn(cachedJson);

        RankResponse result = service.getRankedMeals(householdId, null, 10);

        assertNotNull(result);
        assertEquals(1, result.ranked().size());
        assertEquals("Grilled Chicken", result.ranked().get(0).mealName());
        assertEquals(householdId, result.householdId());

        // FastAPI should NOT have been called
        verifyNoInteractions(restTemplate);
    }

    @Test
    void cacheMissCallsFastApiAndCachesResult() throws Exception {
        String cacheKey = RedisConfig.REC_CACHE_PREFIX + householdId;

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(cacheKey)).thenReturn(null);

        ScoreBreakdown breakdown = new ScoreBreakdown(0.8, 0.7, 0.6, 0.9, 0.75);
        RankedMeal meal = new RankedMeal(UUID.randomUUID(), "Pasta Primavera", 0.82, breakdown);
        RankResponse fastApiResponse = new RankResponse(householdId, List.of(meal));
        String responseJson = objectMapper.writeValueAsString(fastApiResponse);

        when(restTemplate.postForObject(contains("/recommendation/rank"), any(), eq(String.class)))
                .thenReturn(responseJson);

        RankResponse result = service.getRankedMeals(householdId, null, 10);

        assertNotNull(result);
        assertEquals(1, result.ranked().size());
        assertEquals("Pasta Primavera", result.ranked().get(0).mealName());

        // Verify cache was written
        verify(valueOps).set(eq(cacheKey), anyString(), eq(RedisConfig.REC_CACHE_TTL));
    }

    @Test
    void cacheReadFailureFallsToFastApi() throws Exception {
        String cacheKey = RedisConfig.REC_CACHE_PREFIX + householdId;

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(cacheKey)).thenThrow(new RuntimeException("Redis down"));

        RankResponse fastApiResponse = new RankResponse(householdId, List.of());
        String responseJson = objectMapper.writeValueAsString(fastApiResponse);

        when(restTemplate.postForObject(contains("/recommendation/rank"), any(), eq(String.class)))
                .thenReturn(responseJson);

        RankResponse result = service.getRankedMeals(householdId, null, 10);

        assertNotNull(result);
        assertEquals(householdId, result.householdId());
    }

    @Test
    void fastApiFailureReturnsEmptyRankedList() throws Exception {
        String cacheKey = RedisConfig.REC_CACHE_PREFIX + householdId;

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(cacheKey)).thenReturn(null);
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("FastAPI unreachable"));

        RankResponse result = service.getRankedMeals(householdId, null, 10);

        assertNotNull(result);
        assertEquals(householdId, result.householdId());
        assertTrue(result.ranked().isEmpty());
    }

    @Test
    void cachedPayloadDeserialisesIntoValidResponse() throws Exception {
        ScoreBreakdown breakdown = new ScoreBreakdown(0.5, 0.6, 0.7, 0.8, 0.9);
        RankedMeal meal1 = new RankedMeal(UUID.randomUUID(), "Stir Fry", 0.75, breakdown);
        RankedMeal meal2 = new RankedMeal(UUID.randomUUID(), "Salad Bowl", 0.68, breakdown);
        RankResponse original = new RankResponse(householdId, List.of(meal1, meal2));

        String json = objectMapper.writeValueAsString(original);
        RankResponse deserialised = objectMapper.readValue(json, RankResponse.class);

        assertEquals(2, deserialised.ranked().size());
        assertEquals("Stir Fry", deserialised.ranked().get(0).mealName());
        assertEquals(0.75, deserialised.ranked().get(0).compositeScore(), 0.001);
    }
}
