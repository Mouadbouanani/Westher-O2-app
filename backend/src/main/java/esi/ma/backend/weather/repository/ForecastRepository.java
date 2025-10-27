package esi.ma.backend.weather.repository;

import esi.ma.backend.weather.model.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ForecastRepository extends JpaRepository<WeatherForecast, Long> {

    List<WeatherForecast> findByLocationKeyAndForecastTypeAndForecastDateBetween(
            String locationKey,
            WeatherForecast.ForecastType type,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT f FROM WeatherForecast f WHERE " +
            "f.locationKey = :locationKey AND " +
            "f.forecastType = :type AND " +
            "f.forecastDate >= :start AND " +
            "f.createdAt >= :createdSince " +
            "ORDER BY f.forecastDate ASC")
    List<WeatherForecast> findRecentForecast(
            String locationKey,
            WeatherForecast.ForecastType type,
            LocalDateTime start,
            LocalDateTime createdSince
    );

    void deleteByCreatedAtBefore(LocalDateTime threshold);
}