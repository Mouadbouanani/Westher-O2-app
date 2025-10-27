package esi.ma.backend.weather.dto;
import esi.ma.backend.weather.model.WeatherData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    // Location
    private String cityName;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private Integer altitude;

    // Current conditions
    private Double temperature;
    private Double feelsLike;
    private String temperatureFormatted;
    private String feelsLikeFormatted;

    // Temperature range
    private Double tempMin;
    private Double tempMax;

    // Atmospheric conditions
    private Double humidity;
    private String humidityFormatted;
    private Double pressure;
    private String pressureFormatted;
    private Double dewPoint;

    // Wind
    private Double windSpeed;
    private String windSpeedFormatted;
    private Double windDirection;
    private String windDirectionCardinal;

    // Precipitation & clouds
    private Double precipitation;
    private Double cloudCoverage;
    private String cloudCoverageFormatted;

    // Visibility & UV
    private Double visibility;
    private String visibilityFormatted;
    private Double uvIndex;
    private String uvCategory;

    // Conditions
    private String weatherMain;
    private String weatherDescription;
    private String iconCode;
    private String iconUrl;

    // Metadata
    private LocalDateTime timestamp;
    private String dataSource;

    public static WeatherResponse from(WeatherData weather) {
        return WeatherResponse.builder()
                .cityName(weather.getCityName())
                .countryCode(weather.getCountryCode())
                .latitude(weather.getLatitude())
                .longitude(weather.getLongitude())
                .altitude(weather.getAltitude())
                .temperature(weather.getTemperature())
                .feelsLike(weather.getFeelsLike())
                .temperatureFormatted(String.format("%.1f°C", weather.getTemperature()))
                .feelsLikeFormatted(weather.getFeelsLike() != null ?
                        String.format("%.1f°C", weather.getFeelsLike()) : null)
                .tempMin(weather.getTempMin())
                .tempMax(weather.getTempMax())
                .humidity(weather.getHumidity())
                .humidityFormatted(String.format("%.0f%%", weather.getHumidity()))
                .pressure(weather.getPressure())
                .pressureFormatted(String.format("%.0f hPa", weather.getPressure()))
                .dewPoint(weather.getDewPoint())
                .windSpeed(weather.getWindSpeed())
                .windSpeedFormatted(String.format("%.1f m/s (%.0f km/h)",
                        weather.getWindSpeed(), weather.getWindSpeed() * 3.6))
                .windDirection(weather.getWindDirection())
                .windDirectionCardinal(getCardinalDirection(weather.getWindDirection()))
                .precipitation(weather.getPrecipitation())
                .cloudCoverage(weather.getCloudCoverage())
                .cloudCoverageFormatted(weather.getCloudCoverage() != null ?
                        String.format("%.0f%%", weather.getCloudCoverage()) : null)
                .visibility(weather.getVisibility())
                .visibilityFormatted(weather.getVisibility() != null ?
                        String.format("%.1f km", weather.getVisibility()) : null)
                .uvIndex(weather.getUvIndex())
                .uvCategory(getUvCategory(weather.getUvIndex()))
                .weatherMain(weather.getWeatherMain())
                .weatherDescription(weather.getWeatherDescription())
                .iconCode(weather.getIconCode())
                .iconUrl(getIconUrl(weather.getIconCode()))
                .timestamp(weather.getTimestamp())
                .dataSource(weather.getDataSource())
                .build();
    }

    private static String getCardinalDirection(Double degrees) {
        if (degrees == null) return null;

        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(((degrees % 360) / 22.5)) % 16;
        return directions[index];
    }

    private static String getUvCategory(Double uvIndex) {
        if (uvIndex == null) return null;

        if (uvIndex < 3) return "Low";
        if (uvIndex < 6) return "Moderate";
        if (uvIndex < 8) return "High";
        if (uvIndex < 11) return "Very High";
        return "Extreme";
    }

    private static String getIconUrl(String iconCode) {
        if (iconCode == null) return null;
        return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
    }
}