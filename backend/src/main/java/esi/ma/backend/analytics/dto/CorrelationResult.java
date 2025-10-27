package esi.ma.backend.analytics.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationResult {

    private String metric1;
    private String metric2;
    private Double correlationCoefficient;
    private String correlationStrength; // VERY_STRONG, STRONG, MODERATE, WEAK, VERY_WEAK
    private String correlationType; // POSITIVE, NEGATIVE, NO_CORRELATION
    private Double pValue;
    private Boolean statistically_significant;
    private String interpretation;

    private List<ScatterPoint> scatterData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScatterPoint {
        private Double x;
        private Double y;
    }
}