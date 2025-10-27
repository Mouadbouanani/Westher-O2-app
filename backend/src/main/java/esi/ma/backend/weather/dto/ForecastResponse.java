package esi.ma.backend.weather.dto;

import esi.ma.backend.weather.model.WeatherForecast;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastResponse {

    private String cityName;
    private Double latitude;
    private Double longitude;
    private List<DailyForecast> daily;
    private List<HourlyForecast> hourly;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyForecast {
        private LocalDate date;
        private String dayOfWeek;

        private Double tempMax;
        private Double tempMin;
        private Double tempAvg;
        private String tempMaxFormatted;
        private String tempMinFormatted;

        private String weatherMain;
        private String weatherDescription;
        private String iconCode;
        private String iconUrl;

        private Double precipitation;
        private Double precipitationProbability;
        private Double windSpeed;
        private Double humidity;
        private Double uvIndex;
        private String uvCategory;

        private LocalDateTime sunrise;
        private LocalDateTime sunset;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyForecast {
        private LocalDateTime datetime;
        private String hour;

        private Double temperature;
        private String temperatureFormatted;
        private Double feelsLike;

        private String weatherMain;
        private String weatherDescription;
        private String iconCode;
        private String iconUrl;

        private Double precipitation;
        private Double precipitationProbability;
        private Double windSpeed;
        private Double humidity;
        private Double cloudCoverage;
    }

    public static ForecastResponse fromDaily(
            String cityName,
            Double lat,
            Double lon,
            List<WeatherForecast> forecasts
    ) {
        List<DailyForecast> daily = forecasts.stream()
                .map(f -> DailyForecast.builder()
                        .date(f.getForecastDate().toLocalDate())
                        .dayOfWeek(f.getForecastDate().getDayOfWeek().toString())
                        .tempMax(f.getTempMax())
                        .tempMin(f.getTempMin())
                        .tempAvg((f.getTempMax() + f.getTempMin()) / 2)
                        .tempMaxFormatted(String.format("%.0f°C", f.getTempMax()))
                        .tempMinFormatted(String.format("%.0f°C", f.getTempMin()))
                        .weatherMain(f.getWeatherMain())
                        .weatherDescription(f.getWeatherDescription())
                        .iconCode(f.getIconCode())
                        .iconUrl("https://openweathermap.org/img/wn/" + f.getIconCode() + "@2x.png")
                        .precipitation(f.getPrecipitation())
                        .precipitationProbability(f.getPrecipitationProbability())
                        .windSpeed(f.getWindSpeed())
                        .humidity(f.getHumidity())
                        .uvIndex(f.getUvIndex())
                        .uvCategory(getUvCategory(f.getUvIndex()))
                        .build())
                .collect(Collectors.toList());

        return ForecastResponse.builder()
                .cityName(cityName)
                .latitude(lat)
                .longitude(lon)
                .daily(daily)
                .build();
    }

    public static ForecastResponse fromHourly(
            String cityName,
            Double lat,
            Double lon,
            List<WeatherForecast> forecasts
    ) {
        List<HourlyForecast> hourly = forecasts.stream()
                .map(f -> HourlyForecast.builder()
                        .datetime(f.getForecastDate())
                        .hour(String.format("%02d:00", f.getForecastDate().getHour()))
                        .temperature(f.getTemperature())
                        .temperatureFormatted(String.format("%.1f°C", f.getTemperature()))
                        .feelsLike(f.getFeelsLike())
                        .weatherMain(f.getWeatherMain())
                        .weatherDescription(f.getWeatherDescription())
                        .iconCode(f.getIconCode())
                        .iconUrl("https://openweathermap.org/img/wn/" + f.getIconCode() + "@2x.png")
                        .precipitation(f.getPrecipitation())
                        .precipitationProbability(f.getPrecipitationProbability())
                        .windSpeed(f.getWindSpeed())
                        .humidity(f.getHumidity())
                        .cloudCoverage(f.getCloudCoverage())
                        .build())
                .collect(Collectors.toList());

        return ForecastResponse.builder()
                .cityName(cityName)
                .latitude(lat)
                .longitude(lon)
                .hourly(hourly)
                .build();
    }

    private static String getUvCategory(Double uvIndex) {
        if (uvIndex == null) return null;
        if (uvIndex < 3) return "Low";
        if (uvIndex < 6) return "Moderate";
        if (uvIndex < 8) return "High";
        if (uvIndex < 11) return "Very High";
        return "Extreme";
    }
}
