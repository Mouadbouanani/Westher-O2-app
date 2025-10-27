package esi.ma.backend.analytics.dto;
import esi.ma.backend.weather.model.WeatherData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastRequest {

    private Double latitude;
    private Double longitude;
    private Integer days;
    private List<WeatherData> historicalData;

    // Additional context
    private String timezone;
    private Integer altitude;
}