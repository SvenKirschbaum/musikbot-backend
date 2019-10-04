package de.elite12.musikbot.server.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MusikbotCacheConfig {
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
}
