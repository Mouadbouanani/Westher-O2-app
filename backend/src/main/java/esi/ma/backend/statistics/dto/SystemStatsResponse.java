package esi.ma.backend.statistics.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsResponse {

    // Location statistics
    private long totalLocationsTracked;
    private long totalSearches;
    private long totalWeatherRequests;
    private long totalAqiRequests;

    // Alert statistics
    private long activeAlerts;
    private long criticalAlerts;
    private long totalAlertsCreated;

    // Report statistics
    private long totalReports;
    private long reportsLast24Hours;
    private long verifiedReports;

    // Cache statistics
    private CacheStats cacheStats;

    // API health
    private ApiHealthStats apiHealth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheStats {
        private long cacheHits;
        private long cacheMisses;
        private double hitRate;
        private long cachedItems;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiHealthStats {
        private boolean weatherApiHealthy;
        private boolean pollutionApiHealthy;
        private boolean mlServiceHealthy;
        private long averageResponseTime;
    }
}