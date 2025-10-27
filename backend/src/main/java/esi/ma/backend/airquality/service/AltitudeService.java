package esi.ma.backend.airquality.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@Slf4j
public class AltitudeService {

    @Value("${elevation.api.url}")
    private String elevationApiUrl; // https://api.open-elevation.com/api/v1

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Integer> redisTemplate;

    private static final String ALTITUDE_CACHE_PREFIX = "altitude:";

    public AltitudeService(RestTemplate restTemplate, RedisTemplate<String, Integer> redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get altitude for coordinates with caching
     */
    public Integer getAltitude(double lat, double lon) {
        String cacheKey = ALTITUDE_CACHE_PREFIX + String.format("%.2f,%.2f", lat, lon);

        // Check cache
        Integer cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Fetch from API
        Integer altitude = fetchAltitudeFromApi(lat, lon);

        // Cache for 30 days (altitude doesn't change)
        redisTemplate.opsForValue().set(cacheKey, altitude, Duration.ofDays(30));

        return altitude;
    }

    /**
     * Fetch altitude from Open-Elevation API
     */
    private Integer fetchAltitudeFromApi(double lat, double lon) {
        try {
            String url = String.format(
                    "%s/lookup?locations=%.6f,%.6f",
                    elevationApiUrl, lat, lon
            );

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode results = response.getBody().get("results").get(0);
            return results.get("elevation").asInt();

        } catch (Exception e) {
            log.warn("Failed to fetch altitude for {},{}, using default", lat, lon, e);
            return 0; // Default to sea level
        }
    }
}