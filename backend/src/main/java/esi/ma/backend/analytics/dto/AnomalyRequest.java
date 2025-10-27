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
public class AnomalyRequest {

    private Double latitude;
    private Double longitude;
    private List<WeatherData> recentData;
}