package esi.ma.backend.airquality.dto;

import esi.ma.backend.airquality.model.O2Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class O2Response {

    // Location
    private String cityName;
    private Double latitude;
    private Double longitude;
    private Integer altitude;

    // O2 Data
    private Double o2Concentration;
    private String o2PercentageFormatted;
    private String healthLevel;
    private String healthLevelColor;
    private String healthRecommendation;

    // Environmental factors
    private Double temperature;
    private Double pressure;
    private Double humidity;

    // Additional info
    private Double estimatedBloodO2Saturation;
    private String altitudeSicknessRisk;

    // Timestamp
    private LocalDateTime timestamp;

    public static O2Response from(O2Data o2Data) {
        return O2Response.builder()
                .cityName(o2Data.getCityName())
                .latitude(o2Data.getLatitude())
                .longitude(o2Data.getLongitude())
                .altitude(o2Data.getAltitude())
                .o2Concentration(o2Data.getO2Concentration())
                .o2PercentageFormatted(String.format("%.2f%%", o2Data.getO2Concentration()))
                .healthLevel(o2Data.getHealthLevel())
                .healthLevelColor(getHealthLevelColor(o2Data.getHealthLevel()))
                .healthRecommendation(o2Data.getHealthRecommendation())
                .temperature(o2Data.getTemperature())
                .pressure(o2Data.getPressure())
                .humidity(o2Data.getHumidity())
                .estimatedBloodO2Saturation(estimateBloodO2(o2Data.getAltitude()))
                .altitudeSicknessRisk(getAltitudeSicknessRisk(o2Data.getAltitude()))
                .timestamp(o2Data.getTimestamp())
                .build();
    }

    private static String getHealthLevelColor(String healthLevel) {
        switch (healthLevel) {
            case "NORMAL": return "#00E400";      // Green
            case "LOW": return "#FFFF00";         // Yellow
            case "VERY_LOW": return "#FF7E00";    // Orange
            case "DANGEROUS": return "#FF0000";   // Red
            default: return "#808080";            // Gray
        }
    }

    private static Double estimateBloodO2(Integer altitude) {
        if (altitude == null) return 98.0;
        if (altitude < 1500) return 98.0;
        if (altitude < 2500) return 96.0;
        if (altitude < 3500) return 93.0;
        if (altitude < 4500) return 90.0;
        if (altitude < 5500) return 86.0;
        return 83.0;
    }

    private static String getAltitudeSicknessRisk(Integer altitude) {
        if (altitude == null || altitude < 2400) return "Minimal";
        if (altitude < 3000) return "Low";
        if (altitude < 3600) return "Moderate";
        if (altitude < 4500) return "High";
        return "Very High";
    }
}