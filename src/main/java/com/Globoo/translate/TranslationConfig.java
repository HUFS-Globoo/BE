package com.Globoo.translate;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableCaching
public class TranslationConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("translations");
    }
}