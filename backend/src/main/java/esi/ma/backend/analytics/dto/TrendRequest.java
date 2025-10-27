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
public class TrendRequest {

    private Double latitude;
    private Double longitude;
    private String metric;  // temperature, humidity, aqi, o2, etc.
    private Integer months;
    private List<?> historicalData;
}