package com.internship.tool.config;


import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    // Default TTL — 10 minutes
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    // Cache names
    public static final String COMPLIANCE_RECORDS_CACHE = "complianceRecords";
    public static final String COMPLIANCE_RECORD_CACHE = "complianceRecord";
    public static final String COMPLIANCE_STATS_CACHE = "complianceStats";

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(
                new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        // Default cache config — 10 min TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Custom TTL per cache
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Records list — 10 min
        cacheConfigs.put(COMPLIANCE_RECORDS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Single record — 10 min
        cacheConfigs.put(COMPLIANCE_RECORD_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(10)));

        // Stats — 5 min (changes more frequently)
        cacheConfigs.put(COMPLIANCE_STATS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}