package esi.ma.backend.airquality.service;
import esi.ma.backend.airquality.dto.AirQualityMapPoint;
import esi.ma.backend.airquality.dto.PollutantBreakdown;
import esi.ma.backend.airquality.model.AirQuality;

import esi.ma.backend.airquality.repository.AirQualityRepository;
import esi.ma.backend.common.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AirQualityService {

    private final PollutionApiService pollutionApiService;
    private final AirQualityRepository airQualityRepository;

    // EPA AQI Breakpoints
    private static final double[][] PM25_BREAKPOINTS = {
            {0.0, 12.0, 0, 50},
            {12.1, 35.4, 51, 100},
            {35.5, 55.4, 101, 150},
            {55.5, 150.4, 151, 200},
            {150.5, 250.4, 201, 300},
            {250.5, 500.4, 301, 500}
    };

    private static final double[][] PM10_BREAKPOINTS = {
            {0, 54, 0, 50},
            {55, 154, 51, 100},
            {155, 254, 101, 150},
            {255, 354, 151, 200},
            {355, 424, 201, 300},
            {425, 604, 301, 500}
    };

    private static final double[][] O3_BREAKPOINTS = {
            {0, 54, 0, 50},
            {55, 70, 51, 100},
            {71, 85, 101, 150},
            {86, 105, 151, 200},
            {106, 200, 201, 300}
    };

    private static final double[][] NO2_BREAKPOINTS = {
            {0, 53, 0, 50},
            {54, 100, 51, 100},
            {101, 360, 101, 150},
            {361, 649, 151, 200},
            {650, 1249, 201, 300},
            {1250, 2049, 301, 500}
    };

    private static final double[][] SO2_BREAKPOINTS = {
            {0, 35, 0, 50},
            {36, 75, 51, 100},
            {76, 185, 101, 150},
            {186, 304, 151, 200},
            {305, 604, 201, 300},
            {605, 1004, 301, 500}
    };

    private static final double[][] CO_BREAKPOINTS = {
            {0, 4.4, 0, 50},
            {4.5, 9.4, 51, 100},
            {9.5, 12.4, 101, 150},
            {12.5, 15.4, 151, 200},
            {15.5, 30.4, 201, 300},
            {30.5, 50.4, 301, 500}
    };

    /**
     * Get current air quality with caching
     */
    @Cacheable(value = "airquality", key = "#lat + ',' + #lon")
    public AirQuality getAirQuality(double lat, double lon) {
        // Check if recent data exists in database
        String locationKey = GeoUtil.createLocationKey(lat, lon);
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        AirQuality recent = airQualityRepository
                .findFirstByLocationKeyAndTimestampAfterOrderByTimestampDesc(locationKey, threshold)
                .orElse(null);

        if (recent != null) {
            log.info("Returning cached air quality for {}", locationKey);
            return recent;
        }

        // Fetch from API
        AirQuality airQuality = pollutionApiService.fetchPollutionData(lat, lon);

        // Calculate individual AQIs
        if (airQuality.getPm25() != null) {
            airQuality.setPm25Aqi(calculatePollutantAQI(airQuality.getPm25(), PM25_BREAKPOINTS));
        }
        if (airQuality.getPm10() != null) {
            airQuality.setPm10Aqi(calculatePollutantAQI(airQuality.getPm10(), PM10_BREAKPOINTS));
        }
        if (airQuality.getO3() != null) {
            airQuality.setO3Aqi(calculatePollutantAQI(airQuality.getO3(), O3_BREAKPOINTS));
        }
        if (airQuality.getNo2() != null) {
            airQuality.setNo2Aqi(calculatePollutantAQI(airQuality.getNo2(), NO2_BREAKPOINTS));
        }
        if (airQuality.getSo2() != null) {
            airQuality.setSo2Aqi(calculatePollutantAQI(airQuality.getSo2(), SO2_BREAKPOINTS));
        }
        if (airQuality.getCo() != null) {
            airQuality.setCoAqi(calculatePollutantAQI(airQuality.getCo(), CO_BREAKPOINTS));
        }

        // Calculate overall AQI (maximum of all pollutant AQIs)
        int overallAqi = Math.max(
                airQuality.getPm25Aqi() != null ? airQuality.getPm25Aqi() : 0,
                Math.max(
                        airQuality.getPm10Aqi() != null ? airQuality.getPm10Aqi() : 0,
                        Math.max(
                                airQuality.getO3Aqi() != null ? airQuality.getO3Aqi() : 0,
                                Math.max(
                                        airQuality.getNo2Aqi() != null ? airQuality.getNo2Aqi() : 0,
                                        Math.max(
                                                airQuality.getSo2Aqi() != null ? airQuality.getSo2Aqi() : 0,
                                                airQuality.getCoAqi() != null ? airQuality.getCoAqi() : 0
                                        )
                                )
                        )
                )
        );

        airQuality.setAqi(overallAqi);
        airQuality.setAqiCategory(categorizeAQI(overallAqi));
        airQuality.setDominantPollutant(findDominantPollutant(airQuality));
        airQuality.setHealthRecommendation(getHealthRecommendation(overallAqi));
        airQuality.setSensitiveGroupsAdvice(getSensitiveGroupsAdvice(overallAqi));

        // Save to database
        return airQualityRepository.save(airQuality);
    }

    /**
     * Get historical air quality data
     */
    public List<AirQuality> getHistoricalAirQuality(
            double lat,
            double lon,
            LocalDateTime start,
            LocalDateTime end
    ) {
        String locationKey = GeoUtil.createLocationKey(lat, lon);
        return airQualityRepository.findByLocationKeyAndTimestampBetween(
                locationKey, start, end
        );
    }

    /**
     * Get air quality data for map visualization
     */
    public List<AirQualityMapPoint> getAirQualityForMapBounds(
            double neLat, double neLon,
            double swLat, double swLon
    ) {
        // Get recent air quality data within bounds
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);

        List<AirQuality> dataPoints = airQualityRepository
                .findRecentByLocationBounds(swLat, neLat, swLon, neLon, threshold);

        return dataPoints.stream()
                .map(aq -> AirQualityMapPoint.builder()
                        .latitude(aq.getLatitude())
                        .longitude(aq.getLongitude())
                        .aqi(aq.getAqi())
                        .category(aq.getAqiCategory())
                        .color(getAqiColor(aq.getAqi()))
                        .cityName(aq.getCityName())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get detailed pollutant breakdown
     */
    public PollutantBreakdown getPollutantBreakdown(double lat, double lon) {
        AirQuality airQuality = getAirQuality(lat, lon);

        List<PollutantBreakdown.PollutantDetail> pollutants = new ArrayList<>();

        if (airQuality.getPm25() != null) {
            pollutants.add(PollutantBreakdown.PollutantDetail.builder()
                    .name("PM2.5")
                    .fullName("Fine Particulate Matter")
                    .concentration(airQuality.getPm25())
                    .unit("μg/m³")
                    .aqi(airQuality.getPm25Aqi())
                    .category(categorizeAQI(airQuality.getPm25Aqi()))
                    .healthEffect("Can penetrate deep into lungs and bloodstream, causing respiratory and cardiovascular issues")
                    .sources("Vehicle emissions, industrial processes, wood burning, wildfires")
                    .build());
        }

        if (airQuality.getPm10() != null) {
            pollutants.add(PollutantBreakdown.PollutantDetail.builder()
                    .name("PM10")
                    .fullName("Coarse Particulate Matter")
                    .concentration(airQuality.getPm10())
                    .unit("μg/m³")
                    .aqi(airQuality.getPm10Aqi())
                    .category(categorizeAQI(airQuality.getPm10Aqi()))
                    .healthEffect("Can irritate airways, aggravate asthma, and reduce lung function")
                    .sources("Dust, construction sites, unpaved roads, agriculture")
                    .build());
        }

        if (airQuality.getO3() != null) {
            pollutants.add(PollutantBreakdown.PollutantDetail.builder()
                    .name("O3")
                    .fullName("Ground-level Ozone")
                    .concentration(airQuality.getO3())
                    .unit("μg/m³")
                    .aqi(airQuality.getO3Aqi())
                    .category(categorizeAQI(airQuality.getO3Aqi()))
                    .healthEffect("Irritates respiratory system, reduces lung function, triggers asthma")
                    .sources("Formed from reactions between NOx and VOCs in sunlight")
                    .build());
        }

        if (airQuality.getNo2() != null) {
            pollutants.add(PollutantBreakdown.PollutantDetail.builder()
                    .name("NO2")
                    .fullName("Nitrogen Dioxide")
                    .concentration(airQuality.getNo2())
                    .unit("μg/m³")
                    .aqi(airQuality.getNo2Aqi())
                    .category(categorizeAQI(airQuality.getNo2Aqi()))
                    .healthEffect("Inflames airways, reduces immunity to lung infections")
                    .sources("Vehicle emissions, power plants, industrial facilities")
                    .build());
        }

        if (airQuality.getSo2() != null) {
            pollutants.add(PollutantBreakdown.PollutantDetail.builder()
                    .name("SO2")
                    .fullName("Sulfur Dioxide")
                    .concentration(airQuality.getSo2())
                    .unit("μg/m³")
                    .aqi(airQuality.getSo2Aqi())
                    .category(categorizeAQI(airQuality.getSo2Aqi()))
                    .healthEffect("Constricts airways, triggers asthma symptoms")
                    .sources("Coal and oil combustion, metal smelting, volcanic eruptions")
                    .build());
        }

        if (airQuality.getCo() != null) {
            pollutants.add(PollutantBreakdown.PollutantDetail.builder()
                    .name("CO")
                    .fullName("Carbon Monoxide")
                    .concentration(airQuality.getCo())
                    .unit("mg/m³")
                    .aqi(airQuality.getCoAqi())
                    .category(categorizeAQI(airQuality.getCoAqi()))
                    .healthEffect("Reduces oxygen delivery to organs and tissues")
                    .sources("Vehicle exhaust, incomplete combustion, industrial processes")
                    .build());
        }

        return PollutantBreakdown.builder()
                .overallAqi(airQuality.getAqi())
                .dominantPollutant(airQuality.getDominantPollutant())
                .pollutants(pollutants)
                .build();
    }

    /**
     * Calculate AQI for specific pollutant using EPA formula
     */
    private int calculatePollutantAQI(Double concentration, double[][] breakpoints) {
        if (concentration == null) return 0;

        for (double[] breakpoint : breakpoints) {
            double cLow = breakpoint[0];
            double cHigh = breakpoint[1];
            int iLow = (int) breakpoint[2];
            int iHigh = (int) breakpoint[3];

            if (concentration >= cLow && concentration <= cHigh) {
                // Linear interpolation formula
                double aqi = ((iHigh - iLow) / (cHigh - cLow)) *
                        (concentration - cLow) + iLow;
                return (int) Math.round(aqi);
            }
        }

        // If concentration exceeds all breakpoints, return hazardous
        return 500;
    }

    /**
     * Categorize AQI value
     */
    private String categorizeAQI(int aqi) {
        if (aqi <= 50) return "Good";
        if (aqi <= 100) return "Moderate";
        if (aqi <= 150) return "Unhealthy for Sensitive Groups";
        if (aqi <= 200) return "Unhealthy";
        if (aqi <= 300) return "Very Unhealthy";
        return "Hazardous";
    }

    /**
     * Find which pollutant has highest AQI
     */
    private String findDominantPollutant(AirQuality aq) {
        int maxAqi = aq.getAqi();

        if (aq.getPm25Aqi() != null && aq.getPm25Aqi() == maxAqi) return "PM2.5";
        if (aq.getPm10Aqi() != null && aq.getPm10Aqi() == maxAqi) return "PM10";
        if (aq.getO3Aqi() != null && aq.getO3Aqi() == maxAqi) return "O3";
        if (aq.getNo2Aqi() != null && aq.getNo2Aqi() == maxAqi) return "NO2";
        if (aq.getSo2Aqi() != null && aq.getSo2Aqi() == maxAqi) return "SO2";
        if (aq.getCoAqi() != null && aq.getCoAqi() == maxAqi) return "CO";

        return "Unknown";
    }

    /**
     * Get health recommendation based on AQI
     */
    private String getHealthRecommendation(int aqi) {
        if (aqi <= 50) {
            return "Air quality is satisfactory, and air pollution poses little or no risk. " +
                    "Enjoy your usual outdoor activities.";
        } else if (aqi <= 100) {
            return "Air quality is acceptable. However, there may be a risk for some people, " +
                    "particularly those who are unusually sensitive to air pollution.";
        } else if (aqi <= 150) {
            return "Members of sensitive groups may experience health effects. " +
                    "The general public is less likely to be affected. " +
                    "Consider reducing prolonged or heavy outdoor exertion.";
        } else if (aqi <= 200) {
            return "Some members of the general public may experience health effects; " +
                    "members of sensitive groups may experience more serious health effects. " +
                    "Consider limiting prolonged outdoor exertion.";
        } else if (aqi <= 300) {
            return "Health alert: The risk of health effects is increased for everyone. " +
                    "Avoid prolonged outdoor exertion. Move activities indoors or reschedule.";
        } else {
            return "Health warning of emergency conditions: everyone is more likely to be affected. " +
                    "Avoid all outdoor exertion. Remain indoors with air filtration if possible.";
        }
    }

    /**
     * Get advice for sensitive groups
     */
    private String getSensitiveGroupsAdvice(int aqi) {
        if (aqi <= 50) {
            return "None";
        } else if (aqi <= 100) {
            return "Unusually sensitive people should consider reducing prolonged or heavy outdoor exertion.";
        } else if (aqi <= 150) {
            return "People with respiratory disease (such as asthma), children, and older adults " +
                    "should limit prolonged outdoor exertion.";
        } else if (aqi <= 200) {
            return "People with respiratory disease, children, and older adults should avoid " +
                    "prolonged outdoor exertion. Everyone else should limit prolonged outdoor exertion.";
        } else if (aqi <= 300) {
            return "People with respiratory disease, children, and older adults should avoid all " +
                    "outdoor exertion. Everyone else should avoid prolonged outdoor exertion.";
        } else {
            return "Everyone should avoid all outdoor physical activity. " +
                    "People with respiratory disease should remain indoors and keep activity levels low.";
        }
    }

    /**
     * Get AQI color for visualization
     */
    private String getAqiColor(Integer aqi) {
        if (aqi <= 50) return "#00E400";      // Green
        if (aqi <= 100) return "#FFFF00";     // Yellow
        if (aqi <= 150) return "#FF7E00";     // Orange
        if (aqi <= 200) return "#FF0000";     // Red
        if (aqi <= 300) return "#8F3F97";     // Purple
        return "#7E0023";                      // Maroon
    }

    public Integer getAltitude(double lat, double lon) {
        // This method is a placeholder. Actual implementation would call an AltitudeService.
        return null;
    }
}