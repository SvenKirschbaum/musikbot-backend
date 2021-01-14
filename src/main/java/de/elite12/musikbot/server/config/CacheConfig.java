package de.elite12.musikbot.server.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CaffeineCache playlistCache() {
        return new CaffeineCache(
                "playlist",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.DAYS)
                        .recordStats()
                        .build()
        );
    }

    @Bean
    public CaffeineCache statsCache() {
        return new CaffeineCache(
                "stats",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build()
        );
    }

    @Bean
    public CaffeineCache searchCache() {
        return new CaffeineCache(
                "search",
                Caffeine.newBuilder()
                        .expireAfterWrite(15, TimeUnit.MINUTES)
                        .recordStats()
                        .build()
        );
    }

    @Bean
    public CaffeineCache authenticationCache() {
        return new CaffeineCache(
                "oauth",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .recordStats()
                        .build()
        );
    }
}
