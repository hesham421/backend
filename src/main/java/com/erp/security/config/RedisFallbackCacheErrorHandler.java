package com.erp.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Suppresses Redis connection failures (logs WARN, no-op / cache-miss) instead of letting them
 * propagate as a 500 error, so the app falls back to the DB.
 */
@Slf4j
public class RedisFallbackCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("[CACHE] Redis GET failed for cache='{}', key='{}'. Falling back to DB. Error: {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("[CACHE] Redis PUT failed for cache='{}', key='{}'. Error: {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("[CACHE] Redis EVICT failed for cache='{}', key='{}'. Error: {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("[CACHE] Redis CLEAR failed for cache='{}'. Error: {}",
                cache.getName(), exception.getMessage());
    }
}
