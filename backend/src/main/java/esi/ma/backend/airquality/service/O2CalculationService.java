package esi.ma.backend.airquality.service;
import esi.ma.backend.airquality.dto.AltitudeEffectsInfo;
import esi.ma.backend.airquality.model.O2Data;

import esi.ma.backend.airquality.repository.O2DataRepository;
import esi.ma.backend.common.util.GeoUtil;
import esi.ma.backend.airquality.service.AltitudeService;

import esi.ma.backend.weather.model.WeatherData;
import esi.ma.backend.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class O2CalculationService {

    // Constants
    private static final double SEA_LEVEL_O2_PERCENTAGE = 20.95;
    private static final double SEA_LEVEL_PRESSURE = 1013.25; // hPa
    private static final double STANDARD_TEMPERATURE = 15.0;  // Celsius
    private static final double GAS_CONSTANT = 8.314;         // J/(mol·K)
    private static final double MOLAR_MASS_AIR = 0.02897;     // kg/mol
    private static final double GRAVITY = 9.80665;            // m/s²

    private final WeatherService weatherService;
    private final AltitudeService altitudeService;
    private final O2DataRepository o2DataRepository;

    /**
     * Get O2 data for location with caching
     */
    @Cacheable(value = "o2data", key = "#lat + ',' + #lon")
    public O2Data getO2DataForLocation(double lat, double lon) {
        // Check if recent data exists
        String locationKey = GeoUtil.createLocationKey(lat, lon);
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        O2Data recent = o2DataRepository
                .findFirstByLocationKeyAndTimestampAfterOrderByTimestampDesc(locationKey, threshold)
                .orElse(null);

        if (recent != null) {
            log.info("Returning cached O2 data for {}", locationKey);
            return recent;
        }

        // Get weather data
        WeatherData weather = weatherService.getCurrentWeather(lat, lon);

        // Get altitude
        Integer altitude = altitudeService.getAltitude(lat, lon);

        // Calculate O2 concentration - handle case where altitude might be null
        double o2Concentration;
        if (altitude != null) {
            o2Concentration = calculateO2Concentration(
                    altitude,
                    weather.getPressure(),
                    weather.getTemperature(),
                    weather.getHumidity()
            );
        } else {
            // Use default altitude (sea level) if altitude is not available
            o2Concentration = calculateO2Concentration(
                    0, // Default to sea level
                    weather.getPressure(),
                    weather.getTemperature(),
                    weather.getHumidity()
            );
        }

        // Create O2Data object
        O2Data o2Data = new O2Data();
        o2Data.setLocationKey(locationKey);
        o2Data.setCityName(weather.getCityName());
        o2Data.setLatitude(lat);
        o2Data.setLongitude(lon);
        o2Data.setAltitude(altitude);
        o2Data.setO2Concentration(o2Concentration);
        o2Data.setTemperature(weather.getTemperature());
        o2Data.setPressure(weather.getPressure());
        o2Data.setHumidity(weather.getHumidity());
        o2Data.setHealthLevel(classifyO2Level(o2Concentration));
        o2Data.setHealthRecommendation(getHealthRecommendation(o2Concentration));

        // Save to database
        return o2DataRepository.save(o2Data);
    }

    /**
     * Calculate O2 concentration with comprehensive model
     */
    public double calculateO2Concentration(
            Integer altitude,
            double pressure,
            double temperature,
            double humidity
    ) {
        // 1. Base calculation using barometric pressure
        double pressureRatio = pressure / SEA_LEVEL_PRESSURE;
        double baseO2 = SEA_LEVEL_O2_PERCENTAGE * pressureRatio;

        // 2. Temperature adjustment
        // O2 density decreases with temperature increase
        // Using ideal gas law approximation
        double tempKelvin = temperature + 273.15;
        double standardTempKelvin = STANDARD_TEMPERATURE + 273.15;
        double temperatureAdjustment = standardTempKelvin / tempKelvin;

        // 3. Humidity adjustment
        // Water vapor partial pressure reduces O2 partial pressure
        // Saturation vapor pressure using Magnus formula
        double saturationVaporPressure = 6.112 * Math.exp(
                (17.67 * temperature) / (temperature + 243.5)
        );
        double actualVaporPressure = (humidity / 100.0) * saturationVaporPressure;
        double dryAirPressure = pressure - actualVaporPressure;
        double humidityAdjustment = dryAirPressure / pressure;

        // 4. Altitude verification using barometric formula
        double altitudeAdjustment = Math.exp(
                -(MOLAR_MASS_AIR * GRAVITY * (altitude != null ? altitude : 0)) /
                        (GAS_CONSTANT * tempKelvin)
        );

        // Combine all factors with weighting
        double o2Concentration = baseO2 *
                (0.4 * temperatureAdjustment + 0.6) *
                humidityAdjustment *
                (0.3 * altitudeAdjustment + 0.7);

        // Round to 2 decimal places
        return Math.round(o2Concentration * 100.0) / 100.0;
    }

    /**
     * Classify O2 level for health assessment
     */
    public String classifyO2Level(double o2Concentration) {
        if (o2Concentration >= 19.5) {
            return "NORMAL";
        } else if (o2Concentration >= 17.0) {
            return "LOW";
        } else if (o2Concentration >= 14.0) {
            return "VERY_LOW";
        } else {
            return "DANGEROUS";
        }
    }

    /**
     * Get health recommendation based on O2 level
     */
    public String getHealthRecommendation(double o2Concentration) {
        if (o2Concentration >= 19.5) {
            return "Oxygen levels are normal and safe for all activities. " +
                    "No special precautions needed.";
        } else if (o2Concentration >= 17.0) {
            return "Oxygen levels are slightly reduced. You may experience mild symptoms at high altitude. " +
                    "Allow time for acclimatization. People with respiratory or heart conditions should exercise caution.";
        } else if (o2Concentration >= 14.0) {
            return "Oxygen levels are significantly reduced. Physical exertion is not recommended. " +
                    "Symptoms of altitude sickness are likely. People with heart or lung conditions should avoid this altitude. " +
                    "Consider using supplemental oxygen.";
        } else {
            return "DANGEROUS oxygen levels detected. Risk of severe altitude sickness and hypoxia. " +
                    "Immediate descent to lower altitude is strongly recommended. Seek medical attention if experiencing symptoms. " +
                    "Supplemental oxygen is necessary. Do not engage in any physical activity.";
        }
    }

    /**
     * Estimate blood oxygen saturation (SpO2)
     */
    public double estimateBloodO2Saturation(Integer altitude, double o2Concentration) {
        // Base SpO2 at sea level
        double baseSpO2 = 98.0;

        // Altitude-based reduction
        double altitudeReduction;
        int effectiveAltitude = altitude != null ? altitude : 0;
        if (effectiveAltitude < 1500) {
            altitudeReduction = 0;
        } else if (effectiveAltitude < 2500) {
            altitudeReduction = 2.0;
        } else if (effectiveAltitude < 3500) {
            altitudeReduction = 5.0;
        } else if (effectiveAltitude < 4500) {
            altitudeReduction = 8.0;
        } else if (effectiveAltitude < 5500) {
            altitudeReduction = 12.0;
        } else {
            altitudeReduction = 15.0;
        }

        // O2 concentration adjustment
        double o2Factor = o2Concentration / SEA_LEVEL_O2_PERCENTAGE;

        // Calculate estimated SpO2
        double estimatedSpO2 = (baseSpO2 - altitudeReduction) * o2Factor;

        // Clamp between 70-100
        return Math.max(70.0, Math.min(100.0, Math.round(estimatedSpO2 * 10.0) / 10.0));
    }

    /**
     * Get historical O2 data
     */
    public List<O2Data> getHistoricalO2Data(
            double lat,
            double lon,
            LocalDateTime start,
            LocalDateTime end
    ) {
        String locationKey = GeoUtil.createLocationKey(lat, lon);
        return o2DataRepository.findByLocationKeyAndTimestampBetween(
                locationKey, start, end
        );
    }

    /**
     * Get detailed altitude effects information
     */
    public AltitudeEffectsInfo getAltitudeEffectsInfo(int altitude) {
        String category;
        String sicknessRisk;
        String acclimatizationTime;
        String symptoms;
        String recommendations;
        double expectedReduction;
        double estimatedO2;

        if (altitude < 1500) {
            category = "Low Altitude";
            sicknessRisk = "Minimal";
            acclimatizationTime = "Not required";
            symptoms = "None expected";
            recommendations = "No special precautions needed";
            expectedReduction = 0;
            estimatedO2 = 20.95;
        } else if (altitude < 2400) {
            category = "Moderate Altitude";
            sicknessRisk = "Low";
            acclimatizationTime = "1-2 days";
            symptoms = "Mild shortness of breath during exertion";
            recommendations = "Stay hydrated, avoid alcohol, gradual physical activity";
            expectedReduction = 5;
            estimatedO2 = 19.9;
        } else if (altitude < 3600) {
            category = "High Altitude";
            sicknessRisk = "Moderate";
            acclimatizationTime = "2-4 days";
            symptoms = "Headache, nausea, fatigue, shortness of breath, sleep disturbances";
            recommendations = "Ascend gradually, rest frequently, consider preventive medication (acetazolamide), avoid alcohol";
            expectedReduction = 15;
            estimatedO2 = 17.8;
        } else if (altitude < 5500) {
            category = "Very High Altitude";
            sicknessRisk = "High";
            acclimatizationTime = "4-7 days";
            symptoms = "Severe headache, nausea, vomiting, extreme fatigue, difficulty sleeping, rapid heartbeat";
            recommendations = "Proper acclimatization essential, consider supplemental oxygen, descent if symptoms worsen, medical supervision advised";
            expectedReduction = 30;
            estimatedO2 = 14.7;
        } else {
            category = "Extreme Altitude";
            sicknessRisk = "Very High";
            acclimatizationTime = "Weeks (full acclimatization not possible)";
            symptoms = "All symptoms of altitude sickness, risk of HAPE/HACE, severe impairment of physical and mental functions";
            recommendations = "Supplemental oxygen required, expert mountaineering experience necessary, medical support essential, limited time at altitude";
            expectedReduction = 50;
            estimatedO2 = 10.5;
        }

        double estimatedSpO2 = estimateBloodO2Saturation(altitude, estimatedO2);

        return AltitudeEffectsInfo.builder()
                .altitude(altitude)
                .altitudeCategory(category)
                .expectedO2Reduction(expectedReduction)
                .estimatedO2Concentration(estimatedO2)
                .estimatedBloodO2Saturation(estimatedSpO2)
                .altitudeSicknessRisk(sicknessRisk)
                .acclimatizationTime(acclimatizationTime)
                .symptoms(symptoms)
                .recommendations(recommendations)
                .build();
    }
}