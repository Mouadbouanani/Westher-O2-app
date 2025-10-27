package esi.ma.backend.weather.service;

import esi.ma.backend.weather.model.WeatherData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class WeatherCacheService {

    private static final String WEATHER_CACHE_PREFIX = "weather:";
    private static final long CACHE_TTL_MINUTES = 15;

    private final RedisTemplate<String, WeatherData> redisTemplate;

    public WeatherCacheService(RedisTemplate<String, WeatherData> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public WeatherData getCachedWeather(String locationKey) {
        String cacheKey = WEATHER_CACHE_PREFIX + locationKey;
        try {
            return redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("Failed to get cached weather for {}", locationKey, e);
            return null;
        }
    }

    public void cacheWeather(String locationKey, WeatherData weatherData) {
        String cacheKey = WEATHER_CACHE_PREFIX + locationKey;
        try {
            redisTemplate.opsForValue().set(
                    cacheKey,
                    weatherData,
                    Duration.ofMinutes(CACHE_TTL_MINUTES)
            );
        } catch (Exception e) {
            log.warn("Failed to cache weather for {}", locationKey, e);
        }
    }

    public void invalidateCache(String locationKey) {
        String cacheKey = WEATHER_CACHE_PREFIX + locationKey;
        redisTemplate.delete(cacheKey);
    }
}