package esi.ma.backend.analytics.controller;

import esi.ma.backend.analytics.dto.AnomalyResult;
import esi.ma.backend.analytics.dto.CorrelationResult;
import esi.ma.backend.analytics.dto.ForecastResult;
import esi.ma.backend.analytics.dto.TrendResult;
import esi.ma.backend.analytics.service.MLServiceClient;
import esi.ma.backend.common.dto.ApiResponse;
import esi.ma.backend.common.exception.MLServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
@Slf4j
public class AnalyticsController {

    private final MLServiceClient mlServiceClient;

    public AnalyticsController(MLServiceClient mlServiceClient) {
        this.mlServiceClient = mlServiceClient;
    }

    /**
     * GET /api/analytics/forecast?lat=35.6762&lon=139.6503&days=7
     */
    @GetMapping("/forecast")
    public ResponseEntity<ApiResponse<ForecastResult>> getMLForecast(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "7") int days
    ) {
        try {
            if (days < 1 || days > 30) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Days must be between 1 and 30"));
            }

            ForecastResult forecast = mlServiceClient.getMLForecast(lat, lon, days);
            return ResponseEntity.ok(ApiResponse.success(forecast));

        } catch (MLServiceException e) {
            log.error("ML service error", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("ML forecasting service temporarily unavailable"));
        } catch (Exception e) {
            log.error("Error getting forecast", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get forecast: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analytics/trends?lat=35.6762&lon=139.6503&metric=temperature&months=12
     */
    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<TrendResult>> getTrendAnalysis(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam String metric,
            @RequestParam(defaultValue = "12") int months
    ) {
        try {
            // Validate metric
            List<String> validMetrics = Arrays.asList(
                    "temperature", "humidity", "pressure", "windspeed",
                    "aqi", "pm25", "pm10", "o2"
            );

            if (!validMetrics.contains(metric.toLowerCase())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid metric. Valid options: " +
                                String.join(", ", validMetrics)));
            }

            if (months < 1 || months > 24) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Months must be between 1 and 24"));
            }

            TrendResult trend = mlServiceClient.getTrendAnalysis(lat, lon, metric, months);
            return ResponseEntity.ok(ApiResponse.success(trend));

        } catch (MLServiceException e) {
            log.error("ML service error", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("ML trend analysis service temporarily unavailable"));
        } catch (Exception e) {
            log.error("Error getting trend analysis", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get trend analysis: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analytics/anomalies?lat=35.6762&lon=139.6503
     */
    @GetMapping("/anomalies")
    public ResponseEntity<ApiResponse<AnomalyResult>> detectAnomalies(
            @RequestParam Double lat,
            @RequestParam Double lon
    ) {
        try {
            AnomalyResult anomalies = mlServiceClient.detectAnomalies(lat, lon);
            return ResponseEntity.ok(ApiResponse.success(anomalies));

        } catch (MLServiceException e) {
            log.error("ML service error", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.error("ML anomaly detection service temporarily unavailable"));
        } catch (Exception e) {
            log.error("Error detecting anomalies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to detect anomalies: " + e.getMessage()));
        }
    }

    /**
     * GET /api/analytics/correlation?lat=35.6762&lon=139.6503&metric1=temperature&metric2=humidity
     */
    @GetMapping("/correlation")
    public ResponseEntity<ApiResponse<CorrelationResult>> getCorrelation(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam String metric1,
            @RequestParam String metric2
    ) {
        try {
            CorrelationResult correlation = mlServiceClient.getCorrelationAnalysis(
                    lat, lon, metric1, metric2
            );
            return ResponseEntity.ok(ApiResponse.success(correlation));

        } catch (Exception e) {
            log.error("Error getting correlation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get correlation: " + e.getMessage()));
        }
    }
}