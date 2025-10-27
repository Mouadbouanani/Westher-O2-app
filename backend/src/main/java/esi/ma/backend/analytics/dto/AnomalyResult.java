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
public class AnomalyResult {

    private Boolean anomaliesDetected;
    private Integer anomalyCount;
    private List<Anomaly> anomalies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anomaly {
        private LocalDateTime timestamp;
        private String metric;
        private Double value;
        private Double expectedValue;
        private Double deviation;
        private String severity; // LOW, MODERATE, HIGH, EXTREME
        private String description;
        private Double anomalyScore;
    }
}