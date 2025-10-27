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
public class TrendResult {

    private String metric;
    private Integer months;
    private TrendAnalysis analysis;
    private List<DataPoint> dataPoints;
    private List<DataPoint> trendLine;
    private Predictions predictions;

    private String message;
    private Boolean fallback;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysis {
        private String direction;  // INCREASING, DECREASING, STABLE
        private Double slope;
        private Double changePercentage;
        private String significance; // HIGHLY_SIGNIFICANT, SIGNIFICANT, NOT_SIGNIFICANT
        private Double rSquared;
        private String interpretation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDateTime timestamp;
        private Double value;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Predictions {
        private List<DataPoint> nextWeek;
        private List<DataPoint> nextMonth;
        private String predictionQuality;
    }
}