package com.vortexadmin.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

/**
 * Root application configuration class for Vortex Admin Pro.
 *
 * <p>Annotated with {@code @EnableCaching} and {@code @EnableAsync}, this class activates
 * Spring's declarative caching abstraction and asynchronous method execution across the
 * entire application context.
 *
 * <p><strong>Caching strategy:</strong> Uses Caffeine as the in-process cache backend.
 * Three named caches are pre-configured with different TTLs that reflect the expected
 * volatility of each data set:
 * <ul>
 *   <li>{@code dashboard} – 30-second TTL; dashboard statistics change frequently.</li>
 *   <li>{@code roles} – 5-minute (300-second) TTL; role/permission assignments change
 *       rarely.</li>
 *   <li>{@code permissions} – 10-minute (600-second) TTL; the permission catalogue is
 *       essentially static after seeding.</li>
 * </ul>
 * Each cache has a maximum size of 500 entries and records hit/miss statistics for
 * observability.
 *
 * <p><strong>HTTP client:</strong> Provides a {@link RestTemplate} bean with sensible
 * connection and read timeouts for use by services that call external HTTP APIs (e.g.,
 * OAuth2 token endpoints, webhook delivery).
 */
@Configuration
@EnableCaching
@EnableAsync
public class AppConfig {

    /**
     * Creates and registers the application's {@link CacheManager} backed by Caffeine.
     *
     * <p>Delegates cache construction to {@link #buildCache(String, int)} for each named
     * cache region. The returned {@link SimpleCacheManager} is picked up automatically
     * by Spring's {@code @Cacheable}, {@code @CachePut}, and {@code @CacheEvict}
     * annotations throughout the service layer.
     *
     * @return a configured {@link CacheManager} with three pre-registered Caffeine caches.
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            buildCache("dashboard",   30),   // dashboard stats — 30 sec (frequent changes)
            buildCache("roles",      300),   // roles + permissions — 5 min (rare changes)
            buildCache("permissions", 600)   // permission list — 10 min
        ));
        return manager;
    }

    /**
     * Builds a single named {@link CaffeineCache} with the given TTL.
     *
     * <p>Each cache is configured with:
     * <ul>
     *   <li>{@code expireAfterWrite} – entries expire {@code ttlSeconds} after they were
     *       last written, regardless of read access patterns.</li>
     *   <li>{@code maximumSize(500)} – limits memory usage by evicting the least-recently-used
     *       entry when the cache exceeds 500 entries.</li>
     *   <li>{@code recordStats()} – enables hit/miss/eviction counters accessible via
     *       Caffeine's {@code CacheStats} API.</li>
     * </ul>
     *
     * @param name       the logical name of the cache region (used in {@code @Cacheable} annotations).
     * @param ttlSeconds the time-to-live in seconds after which a cached entry is evicted.
     * @return a fully configured {@link CaffeineCache} instance.
     */
    private CaffeineCache buildCache(String name, int ttlSeconds) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds))
                .maximumSize(500)
                .recordStats()
                .build());
    }

    /**
     * Provides a pre-configured {@link RestTemplate} bean for outbound HTTP calls.
     *
     * <p>Configured with:
     * <ul>
     *   <li>5-second connection timeout – prevents indefinite blocking while establishing
     *       a TCP connection to a remote server.</li>
     *   <li>10-second read timeout – limits the time spent waiting for a response body
     *       after the connection is established.</li>
     * </ul>
     *
     * @param builder the auto-configured {@link RestTemplateBuilder} provided by Spring Boot.
     * @return a timeout-constrained {@link RestTemplate} instance.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }
}
