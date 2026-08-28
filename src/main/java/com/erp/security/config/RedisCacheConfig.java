package com.erp.security.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis-backed cache manager, active only when spring.cache.type=redis (dev uses the simple
 * cache instead). TTLs are tiered by data volatility: reference data 24h, profiles 1h, dynamic 30m, temp 5m.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConfig implements CachingConfigurer {

    /**
     * Provide a graceful fallback error handler so that Redis connection
     * failures result in a DB query (cache miss) rather than a 500 error.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisFallbackCacheErrorHandler();
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        
        // Default cache configuration (5 minutes TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()
                    )
                )
                .disableCachingNullValues();

        // Specific TTLs for different cache regions (Rule 16.1: TTL is Mandatory)
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Category 1: Reference Data (24 hours)

        // Rarely changes, safe to cache long-term
        cacheConfigurations.put("permissions", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("roles", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("pages", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // Category 2: User Profiles (1 hour)

        // Moderate change frequency
        cacheConfigurations.put("userProfiles", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("userRoles", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Category 3: Dynamic Data (30 minutes)

        // Dynamic menu structures
        cacheConfigurations.put("menus", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userMenus", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Category 4: Temporary Data (5 minutes)

        // Short-lived helper data
        cacheConfigurations.put("tempData", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
