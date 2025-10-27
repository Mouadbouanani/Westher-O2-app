package esi.ma.backend.analytics.service;

import esi.ma.backend.airquality.repository.AirQualityRepository;
import esi.ma.backend.airquality.repository.O2DataRepository;
import esi.ma.backend.analytics.dto.*;
import esi.ma.backend.common.exception.MLServiceException;
import esi.ma.backend.weather.model.WeatherData;
import esi.ma.backend.weather.repository.WeatherDataRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class MLServiceClient {

    @Value("${ml.service.url}")
    private String mlServiceUrl; // http://ml-service:8000

    @Value("${ml.service.timeout:30000}")
    private int timeout;

    private final RestTemplate restTemplate;
    private final WeatherDataRepository weatherDataRepository;
    private final AirQualityRepository airQualityRepository;
    private final O2DataRepository o2DataRepository;

    public MLServiceClient(RestTemplate restTemplate, WeatherDataRepository weatherDataRepository, AirQualityRepository airQualityRepository, O2DataRepository o2DataRepository) {
        this.restTemplate = restTemplate;
        this.weatherDataRepository = weatherDataRepository;
        this.airQualityRepository = airQualityRepository;
        this.o2DataRepository = o2DataRepository;
    }

    /**
     * Get ML-based weather forecast
     */
    @CircuitBreaker(name = "mlService", fallbackMethod = "getForecastFallback")
    @io.github.resilience4j.timelimiter.annotation.TimeLimiter(name = "mlService")
    public ForecastResult getMLForecast(double lat, double lon, int days) {
        String url = mlServiceUrl + "/api/v1/forecast";

        // Prepare request with historical data
        ForecastRequest request = new ForecastRequest();
        request.setLatitude(lat);
        request.setLongitude(lon);
        request.setDays(days);

        // Get last 90 days of historical data for ML training
        String locationKey = String.format("%.2f,%.2f", lat, lon);
        List<WeatherData> historicalData = weatherDataRepository
                .findByLocationKeyAndTimestampBetween(
                        locationKey,
                        LocalDateTime.now().minusDays(90),
                        LocalDateTime.now()
                );

        request.setHistoricalData(historicalData);

        try {
            HttpEntity<ForecastRequest> entity = new HttpEntity<>(request);
            ResponseEntity<ForecastResult> response = restTemplate.postForEntity(
                    url,
                    entity,
                    ForecastResult.class
            );

            log.info("ML forecast retrieved for {},{} - {} days", lat, lon, days);
            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get ML forecast", e);
            throw new MLServiceException("ML service unavailable", e);
        }
    }

    /**
     * Get trend analysis from ML service
     */
    @CircuitBreaker(name = "mlService", fallbackMethod = "getTrendFallback")
    public TrendResult getTrendAnalysis(double lat, double lon, String metric, int months) {
        String url = mlServiceUrl + "/api/v1/trends";

        TrendRequest request = new TrendRequest();
        request.setLatitude(lat);
        request.setLongitude(lon);
        request.setMetric(metric); // temperature, humidity, aqi, o2
        request.setMonths(months);

        // Get historical data based on metric type
        List<?> historicalData = getHistoricalDataForMetric(lat, lon, metric, months);
        request.setHistoricalData(historicalData);

        try {
            HttpEntity<TrendRequest> entity = new HttpEntity<>(request);
            ResponseEntity<TrendResult> response = restTemplate.postForEntity(
                    url,
                    entity,
                    TrendResult.class
            );

            log.info("Trend analysis retrieved for {},{} - metric: {}", lat, lon, metric);
            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get trend analysis", e);
            throw new MLServiceException("ML service unavailable", e);
        }
    }

    /**
     * Detect anomalies in weather patterns
     */
    @CircuitBreaker(name = "mlService")
    public AnomalyResult detectAnomalies(double lat, double lon) {
        String url = mlServiceUrl + "/api/v1/anomaly";

        AnomalyRequest request = new AnomalyRequest();
        request.setLatitude(lat);
        request.setLongitude(lon);

        // Get recent data for anomaly detection
        String locationKey = String.format("%.2f,%.2f", lat, lon);
        List<WeatherData> recentData = weatherDataRepository
                .findByLocationKeyAndTimestampBetween(
                        locationKey,
                        LocalDateTime.now().minusDays(30),
                        LocalDateTime.now()
                );

        request.setRecentData(recentData);

        try {
            HttpEntity<AnomalyRequest> entity = new HttpEntity<>(request);
            ResponseEntity<AnomalyResult> response = restTemplate.postForEntity(
                    url,
                    entity,
                    AnomalyResult.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to detect anomalies", e);
            throw new MLServiceException("ML service unavailable", e);
        }
    }

    /**
     * Get correlation analysis between metrics
     */
    public CorrelationResult getCorrelationAnalysis(
            double lat,
            double lon,
            String metric1,
            String metric2
    ) {
        String url = mlServiceUrl + "/api/v1/correlation";

        CorrelationRequest request = new CorrelationRequest();
        request.setLatitude(lat);
        request.setLongitude(lon);
        request.setMetric1(metric1);
        request.setMetric2(metric2);

        try {
            HttpEntity<CorrelationRequest> entity = new HttpEntity<>(request);
            ResponseEntity<CorrelationResult> response = restTemplate.postForEntity(
                    url,
                    entity,
                    CorrelationResult.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to get correlation analysis", e);
            throw new MLServiceException("ML service unavailable", e);
        }
    }

    // Helper methods

    private List<?> getHistoricalDataForMetric(double lat, double lon, String metric, int months) {
        String locationKey = String.format("%.2f,%.2f", lat, lon);
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        LocalDateTime endDate = LocalDateTime.now();

        switch (metric.toLowerCase()) {
            case "temperature":
            case "humidity":
            case "pressure":
            case "windspeed":
                return weatherDataRepository.findByLocationKeyAndTimestampBetween(
                        locationKey, startDate, endDate
                );

            case "aqi":
            case "pm25":
            case "pm10":
                return airQualityRepository.findByLocationKeyAndTimestampBetween(
                        locationKey, startDate, endDate
                );

            case "o2":
                return o2DataRepository.findByLocationKeyAndTimestampBetween(
                        locationKey, startDate, endDate
                );

            default:
                throw new IllegalArgumentException("Unknown metric: " + metric);
        }
    }

    // Fallback methods

    private ForecastResult getForecastFallback(double lat, double lon, int days, Exception e) {
        log.warn("ML forecast unavailable, using simple fallback", e);

        // Return simple forecast based on recent averages
        ForecastResult fallback = new ForecastResult();
        fallback.setMessage("ML service unavailable. Showing basic forecast based on recent data.");
        fallback.setFallback(true);

        // Calculate simple moving averages from recent data
        // ... implementation ...

        return fallback;
    }

    private TrendResult getTrendFallback(double lat, double lon, String metric, int months, Exception e) {
        log.warn("ML trend analysis unavailable, using simple fallback", e);

        TrendResult fallback = new TrendResult();
        fallback.setMessage("ML service unavailable. Showing basic trend analysis.");
        fallback.setFallback(true);

        return fallback;
    }
}