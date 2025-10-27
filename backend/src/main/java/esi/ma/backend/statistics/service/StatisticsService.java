package esi.ma.backend.statistics.service;
import esi.ma.backend.alert.repository.AlertRepository;
import esi.ma.backend.crowdsourcing.repository.ReportRepository;
import esi.ma.backend.location.repository.LocationStatsRepository;
import esi.ma.backend.statistics.dto.SystemStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {

    private final LocationStatsRepository locationStatsRepository;
    private final AlertRepository alertRepository;
    private final ReportRepository reportRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Get comprehensive system statistics
     */
    public SystemStatsResponse getSystemStatistics() {
        return SystemStatsResponse.builder()
                .totalLocationsTracked(locationStatsRepository.countTotalLocations())
                .totalSearches(locationStatsRepository.sumTotalSearches())
                .totalWeatherRequests(getTotalWeatherRequests())
                .totalAqiRequests(getTotalAqiRequests())
                .activeAlerts(alertRepository.countActiveAlerts())
                .criticalAlerts(alertRepository.countActiveAlertsBySeverity(5))
                .totalAlertsCreated(alertRepository.count())
                .totalReports(reportRepository.count())
                .reportsLast24Hours(getReportsLast24Hours())
                .verifiedReports(reportRepository.countByVerifiedTrue())
                .cacheStats(getCacheStatistics())
                .apiHealth(getApiHealthStats())
                .build();
    }

    /**
     * Get cache statistics from Redis
     */
    private SystemStatsResponse.CacheStats getCacheStatistics() {
        // This would require implementing cache hit/miss tracking
        // Simplified version here
        return SystemStatsResponse.CacheStats.builder()
                .cacheHits(0L)
                .cacheMisses(0L)
                .hitRate(0.0)
                .cachedItems(0L)
                .build();
    }

    /**
     * Get API health statistics
     */
    private SystemStatsResponse.ApiHealthStats getApiHealthStats() {
        // This would require implementing health checks
        // Simplified version here
        return SystemStatsResponse.ApiHealthStats.builder()
                .weatherApiHealthy(true)
                .pollutionApiHealthy(true)
                .mlServiceHealthy(true)
                .averageResponseTime(150L)
                .build();
    }

    private long getTotalWeatherRequests() {
        return locationStatsRepository.findAll().stream()
                .mapToLong(stats -> stats.getWeatherRequestCount())
                .sum();
    }

    private long getTotalAqiRequests() {
        return locationStatsRepository.findAll().stream()
                .mapToLong(stats -> stats.getAqiRequestCount())
                .sum();
    }

    private long getReportsLast24Hours() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return reportRepository.countByCreatedAtAfter(yesterday);
    }
}