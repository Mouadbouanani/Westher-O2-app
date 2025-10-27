package esi.ma.backend.airquality.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AltitudeEffectsInfo {

    private Integer altitude;
    private String altitudeCategory;
    private Double expectedO2Reduction;
    private Double estimatedO2Concentration;
    private Double estimatedBloodO2Saturation;
    private String altitudeSicknessRisk;
    private String acclimatizationTime;
    private String symptoms;
    private String recommendations;
}