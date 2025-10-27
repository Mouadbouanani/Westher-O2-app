package esi.ma.backend.airquality.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "o2_data", indexes = {
        @Index(name = "idx_o2_location_timestamp", columnList = "location_key, timestamp")
})
public class O2Data {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Location
    private String locationKey;
    private String cityName;
    private Double latitude;
    private Double longitude;
    private Integer altitude; // meters above sea level

    // O2 Calculation
    private Double o2Concentration; // percentage
    private String healthLevel; // NORMAL, LOW, VERY_LOW, DANGEROUS

    // Environmental factors used in calculation
    private Double temperature;
    private Double pressure;
    private Double humidity;

    // Additional info
    @Column(columnDefinition = "TEXT")
    private String healthRecommendation;

    @CreationTimestamp
    private LocalDateTime timestamp;

}