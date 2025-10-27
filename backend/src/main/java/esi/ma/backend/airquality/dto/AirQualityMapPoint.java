package esi.ma.backend.airquality.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityMapPoint {

    private Double latitude;
    private Double longitude;
    private Integer aqi;
    private String category;
    private String color;
    private String cityName;
}