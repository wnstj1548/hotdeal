package com.web.hotdeal.commons.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {
    public static final String DEAL_PAGE_CACHE = "dealPages";
    public static final String POPULAR_DEALS_CACHE = "popularDeals";
    public static final String SOURCE_SUMMARY_CACHE = "sourceSummary";
    public static final String SOURCE_FRESHNESS_CACHE = "sourceFreshness";
    public static final String CATEGORY_OPTIONS_CACHE = "categoryOptions";

    private static final Duration DEFAULT_CACHE_TTL = Duration.ofSeconds(30);

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        ObjectMapper redisObjectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(DEFAULT_CACHE_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheConfiguration baseConfiguration
    ) {
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfiguration)
                .withCacheConfiguration(DEAL_PAGE_CACHE, baseConfiguration.entryTtl(Duration.ofSeconds(30)))
                .withCacheConfiguration(POPULAR_DEALS_CACHE, baseConfiguration.entryTtl(Duration.ofSeconds(30)))
                .withCacheConfiguration(SOURCE_SUMMARY_CACHE, baseConfiguration.entryTtl(Duration.ofSeconds(60)))
                .withCacheConfiguration(SOURCE_FRESHNESS_CACHE, baseConfiguration.entryTtl(Duration.ofSeconds(30)))
                .withCacheConfiguration(CATEGORY_OPTIONS_CACHE, baseConfiguration.entryTtl(Duration.ofSeconds(60)))
                .build();
    }
}
