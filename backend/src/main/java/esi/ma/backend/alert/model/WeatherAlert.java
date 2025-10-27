package esi.ma.backend.alert.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "weather_alerts", indexes = {
        @Index(name = "idx_alert_location", columnList = "location_key"),
        @Index(name = "idx_alert_active", columnList = "active, created_at"),
        @Index(name = "idx_alert_severity", columnList = "severity DESC")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Location
    private String locationKey;
    private String cityName;
    private Double latitude;
    private Double longitude;

    // Alert type
    @Enumerated(EnumType.STRING)
    private AlertType type;

    // Severity (1-5)
    private Integer severity;

    // Alert details
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    // Trigger values
    private Double triggerValue;
    private String triggerUnit;

    // Status
    private Boolean active = true;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

    // Source
    private String source; // SYSTEM, API, USER_REPORT
}