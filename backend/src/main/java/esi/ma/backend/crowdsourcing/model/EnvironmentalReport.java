package esi.ma.backend.crowdsourcing.model;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "environmental_reports", indexes = {
        @Index(name = "idx_report_location", columnList = "latitude, longitude"),
        @Index(name = "idx_report_timestamp", columnList = "created_at"),
        @Index(name = "idx_report_category", columnList = "category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String submitterId;
    private String locationKey;
    private String cityName;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private ReportCategory category;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String photoUrl;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> tags;

    private Integer severity;
    private Integer upvotes = 0;
    private Integer downvotes = 0;
    private Double credibilityScore = 0.0;

    private Boolean verified = false;
    private Boolean moderated = false;
    private Boolean hidden = false;
    private String moderationNotes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String ipHash;

    public void incrementUpvotes() {
        this.upvotes++;
    }

    public Long getId() {
        return id;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public String getLocationKey() {
        return locationKey;
    }

    public String getCityName() {
        return cityName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public ReportCategory getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public Integer getSeverity() {
        return severity;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public Double getCredibilityScore() {
        return credibilityScore;
    }

    public Boolean getVerified() {
        return verified;
    }

    public Boolean getModerated() {
        return moderated;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public String getModerationNotes() {
        return moderationNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getIpHash() {
        return ipHash;
    }

    public void incrementDownvotes() {
        this.downvotes++;
    }
}