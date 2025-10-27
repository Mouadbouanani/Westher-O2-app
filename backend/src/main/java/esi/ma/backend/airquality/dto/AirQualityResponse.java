package esi.ma.backend.airquality.dto;
import esi.ma.backend.airquality.model.AirQuality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityResponse {

    // Location
    private String cityName;
    private Double latitude;
    private Double longitude;

    // Overall AQI
    private Integer aqi;
    private String aqiCategory;
    private String aqiColor;
    private String dominantPollutant;

    // Individual pollutants
    private Map<String, PollutantInfo> pollutants;

    // Health information
    private String healthRecommendation;
    private String sensitiveGroupsAdvice;

    // Timestamp
    private LocalDateTime timestamp;
    private String dataSource;

    public static AirQualityResponse from(AirQuality airQuality) {
        Map<String, PollutantInfo> pollutants = new HashMap<>();

        if (airQuality.getPm25() != null) {
            pollutants.put("PM2.5", PollutantInfo.builder()
                    .value(airQuality.getPm25())
                    .unit("μg/m³")
                    .aqi(airQuality.getPm25Aqi())
                    .category(categorizeAQI(airQuality.getPm25Aqi()))
                    .build());
        }

        if (airQuality.getPm10() != null) {
            pollutants.put("PM10", PollutantInfo.builder()
                    .value(airQuality.getPm10())
                    .unit("μg/m³")
                    .aqi(airQuality.getPm10Aqi())
                    .category(categorizeAQI(airQuality.getPm10Aqi()))
                    .build());
        }

        if (airQuality.getO3() != null) {
            pollutants.put("O3", PollutantInfo.builder()
                    .value(airQuality.getO3())
                    .unit("μg/m³")
                    .aqi(airQuality.getO3Aqi())
                    .category(categorizeAQI(airQuality.getO3Aqi()))
                    .build());
        }

        if (airQuality.getNo2() != null) {
            pollutants.put("NO2", PollutantInfo.builder()
                    .value(airQuality.getNo2())
                    .unit("μg/m³")
                    .aqi(airQuality.getNo2Aqi())
                    .category(categorizeAQI(airQuality.getNo2Aqi()))
                    .build());
        }

        if (airQuality.getSo2() != null) {
            pollutants.put("SO2", PollutantInfo.builder()
                    .value(airQuality.getSo2())
                    .unit("μg/m³")
                    .aqi(airQuality.getSo2Aqi())
                    .category(categorizeAQI(airQuality.getSo2Aqi()))
                    .build());
        }

        if (airQuality.getCo() != null) {
            pollutants.put("CO", PollutantInfo.builder()
                    .value(airQuality.getCo())
                    .unit("mg/m³")
                    .aqi(airQuality.getCoAqi())
                    .category(categorizeAQI(airQuality.getCoAqi()))
                    .build());
        }

        return AirQualityResponse.builder()
                .cityName(airQuality.getCityName())
                .latitude(airQuality.getLatitude())
                .longitude(airQuality.getLongitude())
                .aqi(airQuality.getAqi())
                .aqiCategory(airQuality.getAqiCategory())
                .aqiColor(getAqiColor(airQuality.getAqi()))
                .dominantPollutant(airQuality.getDominantPollutant())
                .pollutants(pollutants)
                .healthRecommendation(airQuality.getHealthRecommendation())
                .sensitiveGroupsAdvice(airQuality.getSensitiveGroupsAdvice())
                .timestamp(airQuality.getTimestamp())
                .dataSource(airQuality.getDataSource())
                .build();
    }

    private static String categorizeAQI(Integer aqi) {
        if (aqi == null) return "Unknown";
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    private static String getAqiColor(Integer aqi) {
        if (aqi <= 50) return "#00E400";      // Green
        if (aqi <= 100) return "#FFFF00";     // Yellow
        if (aqi <= 150) return "#FF7E00";     // Orange
        if (aqi <= 200) return "#FF0000";     // Red
        if (aqi <= 300) return "#8F3F97";     // Purple
        return "#7E0023";                      // Maroon
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PollutantInfo {
        private Double value;
        private String unit;
        private Integer aqi;
        private String category;
    }
}