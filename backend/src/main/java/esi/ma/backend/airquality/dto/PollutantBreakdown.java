package esi.ma.backend.airquality.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollutantBreakdown {

    private Integer overallAqi;
    private String dominantPollutant;
    private List<PollutantDetail> pollutants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PollutantDetail {
        private String name;
        private String fullName;
        private Double concentration;
        private String unit;
        private Integer aqi;
        private String category;
        private String healthEffect;
        private String sources;
    }
}