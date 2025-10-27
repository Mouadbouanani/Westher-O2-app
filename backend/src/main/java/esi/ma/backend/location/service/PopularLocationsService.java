package esi.ma.backend.location.service;



import esi.ma.backend.common.util.GeoUtil;
import esi.ma.backend.location.model.LocationStats;
import esi.ma.backend.location.repository.LocationStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PopularLocationsService {

    private final LocationStatsRepository locationStatsRepository;
    private final GeocodingService geocodingService;

    /**
     * Track location access
     */
    @Transactional
    public void trackLocationAccess(double lat, double lon, String type) {
        String locationKey = GeoUtil.createLocationKey(lat, lon);

        LocationStats stats = locationStatsRepository
                .findByLocationKey(locationKey)
                .orElseGet(() -> createNewLocationStats(lat, lon, locationKey));

        // Update counts based on type
        switch (type.toLowerCase()) {
            case "search":
                stats.incrementSearchCount();
                break;
            case "weather":
                stats.incrementWeatherRequestCount();
                break;
            case "aqi":
                stats.incrementAqiRequestCount();
                break;
        }

        locationStatsRepository.save(stats);
    }

    /**
     * Get most popular locations
     */
    public List<LocationStats> getPopularLocations(int limit) {
        return locationStatsRepository.findTop10ByOrderBySearchCountDesc();
    }

    /**
     * Get trending locations (popular in last 7 days)
     */
    public List<LocationStats> getTrendingLocations(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return locationStatsRepository.findTrendingLocations(
                since,
                PageRequest.of(0, limit)
        );
    }

    /**
     * Get recently accessed locations
     */
    public List<LocationStats> getRecentLocations(int limit) {
        return locationStatsRepository.findTop10ByOrderByLastAccessedAtDesc();
    }

    /**
     * Get global statistics
     */
    public GlobalLocationStats getGlobalStats() {
        long totalLocations = locationStatsRepository.countTotalLocations();
        long totalSearches = locationStatsRepository.sumTotalSearches();

        return GlobalLocationStats.builder()
                .totalLocations(totalLocations)
                .totalSearches(totalSearches)
                .build();
    }

    /**
     * Create new location stats entry
     */
    private LocationStats createNewLocationStats(double lat, double lon, String locationKey) {
        // Try to get location name
        String cityName = "Unknown";
        String countryName = null;
        String countryCode = null;

        try {
            var location = geocodingService.reverseGeocode(lat, lon);
            cityName = location.getCityName();
            countryName = location.getCountryName();
            countryCode = location.getCountryCode();
        } catch (Exception e) {
            log.warn("Failed to get location name for {},{}", lat, lon);
        }

        return LocationStats.builder()
                .locationKey(locationKey)
                .cityName(cityName)
                .countryName(countryName)
                .countryCode(countryCode)
                .latitude(lat)
                .longitude(lon)
                .searchCount(0L)
                .weatherRequestCount(0L)
                .aqiRequestCount(0L)
                .build();
    }

    /**
     * Cleanup old location stats (scheduled task)
     * Remove locations not accessed in 90 days with low search count
     */
    @Scheduled(cron = "0 0 3 * * ?") // Run at 3 AM daily
    @Transactional
    public void cleanupOldStats() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(90);
        // This would need a custom query - left as exercise
        log.info("Cleanup task ran at {}", LocalDateTime.now());
    }

    @lombok.Data
    @lombok.Builder
    public static class GlobalLocationStats {
        private long totalLocations;
        private long totalSearches;
    }
}