package com.nourishos.authority.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    public static final String EXEC_SESSION_PREFIX = "exec:session:";
    public static final String EXEC_INTERVENTION_PREFIX = "exec:intervention:";
    public static final String REC_CACHE_PREFIX = "rec:cache:";
    public static final Duration REC_CACHE_TTL = Duration.ofMinutes(5);

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
