package esi.ma.backend.analytics.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResult {

    private Double latitude;
    private Double longitude;
    private Integer days;
    private List<DayForecast> forecasts;

    // Model metadata
    private String modelType;
    private Double confidence;
    private String message;
    private Boolean fallback;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayForecast {
        private LocalDateTime date;

        // Temperature predictions
        private Double tempMin;
        private Double tempMax;
        private Double tempAvg;
        private Double tempMinConfidence;
        private Double tempMaxConfidence;

        // Other predictions
        private Double humidity;
        private Double precipitation;
        private Double precipitationProbability;
        private Double windSpeed;
        private Double pressure;

        // Predicted conditions
        private String weatherCondition;
        private Double conditionProbability;

        // Anomaly detection
        private Boolean anomalyDetected;
        private String anomalyDescription;
    }
}