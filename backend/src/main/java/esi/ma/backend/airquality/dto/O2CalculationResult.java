package esi.ma.backend.airquality.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class O2CalculationResult {

    // Result
    private Double o2Concentration;
    private String healthLevel;
    private String recommendation;
    private Double estimatedBloodO2Saturation;

    // Input parameters used
    private Integer altitude;
    private Double pressure;
    private Double temperature;
    private Double humidity;

    // Additional calculations
    private Double pressureRatio;
    private Double altitudeEffect;
    private Double temperatureEffect;
    private Double humidityEffect;
}