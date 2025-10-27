package esi.ma.backend.analytics.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelationRequest {

    private Double latitude;
    private Double longitude;
    private String metric1;
    private String metric2;
}