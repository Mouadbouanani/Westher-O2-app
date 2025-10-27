package esi.ma.backend.crowdsourcing.service;

import esi.ma.backend.common.exception.InvalidRequestException;
import esi.ma.backend.common.exception.RateLimitException;
import esi.ma.backend.common.exception.ResourceNotFoundException;
import esi.ma.backend.crowdsourcing.dto.ReportCluster;
import esi.ma.backend.crowdsourcing.dto.ReportRequest;
import esi.ma.backend.crowdsourcing.model.EnvironmentalReport;
import esi.ma.backend.crowdsourcing.model.ReportCategory;
import esi.ma.backend.crowdsourcing.repository.ReportRepository;
import esi.ma.backend.location.model.Location;
import esi.ma.backend.location.service.GeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final ImageUploadService imageUploadService;
    private final GeocodingService geocodingService;

    private static final int MAX_REPORTS_PER_HOUR = 5; // Anti-spam
    private static final int MAX_PHOTO_SIZE_MB = 5;

    public ReportService(ReportRepository reportRepository, ImageUploadService imageUploadService, GeocodingService geocodingService) {
        this.reportRepository = reportRepository;
        this.imageUploadService = imageUploadService;
        this.geocodingService = geocodingService;
    }

    /**
     * Submit a new environmental report
     */
    @Transactional
    public EnvironmentalReport submitReport(
            ReportRequest request,
            MultipartFile photo,
            String ipAddress
    ) throws IOException {
        // Anti-spam: Check submission rate
        String ipHash = hashIP(ipAddress);
        int recentReports = reportRepository.countByIpHashAndCreatedAtAfter(
                ipHash,
                LocalDateTime.now().minusHours(1)
        );

        if (recentReports >= MAX_REPORTS_PER_HOUR) {
            throw new RateLimitException("Too many reports submitted. Please wait before submitting again.");
        }

        // Create report
        EnvironmentalReport report = new EnvironmentalReport();
        report.setSubmitterId(UUID.randomUUID().toString());
        report.setLatitude(request.getLatitude());
        report.setLongitude(request.getLongitude());
        report.setLocationKey(String.format("%.2f,%.2f",
                request.getLatitude(), request.getLongitude()));
        report.setCategory(request.getCategory());
        report.setTitle(sanitizeInput(request.getTitle()));
        report.setDescription(sanitizeInput(request.getDescription()));
        report.setSeverity(request.getSeverity());
        report.setTags(request.getTags());
        report.setIpHash(ipHash);

        // Get city name
        try {
            Location location = geocodingService.reverseGeocode(
                    request.getLatitude(),
                    request.getLongitude()
            );
            report.setCityName(((esi.ma.backend.location.model.Location) location).getCityName());
        } catch (Exception e) {
            log.warn("Failed to geocode location", e);
        }

        // Upload photo if provided
        if (photo != null && !photo.isEmpty()) {
            validatePhoto(photo);
            String photoUrl = imageUploadService.uploadReportImage(photo, report.getSubmitterId());
            report.setPhotoUrl(photoUrl);
        }

        // Save report
        EnvironmentalReport saved = reportRepository.save(report);
        log.info("New environmental report submitted: {}", saved.getId());

        return saved;
    }

    /**
     * Get reports near a location
     */
    public Page<EnvironmentalReport> getReportsNearLocation(
            double lat,
            double lon,
            double radiusKm,
            ReportCategory category,
            Pageable pageable
    ) {
        // Calculate bounding box
        double latDelta = radiusKm / 111.0; // ~111 km per degree latitude
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLon = lon - lonDelta;
        double maxLon = lon + lonDelta;

        if (category != null) {
            return reportRepository.findByCategoryAndLocationWithinBoundsAndNotHidden(
                    category, minLat, maxLat, minLon, maxLon, pageable
            );
        } else {
            return reportRepository.findByLocationWithinBoundsAndNotHidden(
                    minLat, maxLat, minLon, maxLon, pageable
            );
        }
    }

    /**
     * Get report by ID
     */
    public EnvironmentalReport getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }

    /**
     * Upvote a report
     */
    @Transactional
    public EnvironmentalReport upvoteReport(Long reportId) {
        EnvironmentalReport report = getReportById(reportId);
        report.setUpvotes(report.getUpvotes() + 1);
        updateCredibilityScore(report);
        return reportRepository.save(report);
    }

    /**
     * Downvote a report
     */
    @Transactional
    public EnvironmentalReport downvoteReport(Long reportId) {
        EnvironmentalReport report = getReportById(reportId);
        report.setDownvotes(report.getDownvotes() + 1);
        updateCredibilityScore(report);
        return reportRepository.save(report);
    }

    /**
     * Get reports for map view (clustered)
     */
    public List<ReportCluster> getReportsForMap(
            double neLat, double neLon,
            double swLat, double swLon,
            ReportCategory category
    ) {
        List<EnvironmentalReport> reports;

        if (category != null) {
            reports = reportRepository.findByCategoryAndLocationWithinBounds(
                    category, swLat, neLat, swLon, neLon
            );
        } else {
            reports = reportRepository.findByLocationWithinBounds(
                    swLat, neLat, swLon, neLon
            );
        }

        // Cluster nearby reports for map visualization
        return clusterReports(reports);
    }

    /**
     * Get trending reports (most upvoted recently)
     */
    public List<EnvironmentalReport> getTrendingReports(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return reportRepository.findTopByCreatedAtAfterOrderByUpvotesDesc(since,
                PageRequest.of(0, limit));
    }

    // Helper methods

    private void updateCredibilityScore(EnvironmentalReport report) {
        int totalVotes = report.getUpvotes() + report.getDownvotes();
        if (totalVotes == 0) {
            report.setCredibilityScore(0.5);
            return;
        }

        double score = (double) report.getUpvotes() / totalVotes;

        // Apply Wilson score interval for better ranking
        double z = 1.96; // 95% confidence
        double phat = score;
        double n = totalVotes;

        double wilsonScore = (phat + z*z/(2*n) - z * Math.sqrt((phat*(1-phat)+z*z/(4*n))/n))
                / (1 + z*z/n);

        report.setCredibilityScore(Math.round(wilsonScore * 100.0) / 100.0);
    }

    private String hashIP(String ipAddress) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ipAddress.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash IP", e);
        }
    }

    private String sanitizeInput(String input) {
        if (input == null) return null;
        // Remove potentially harmful characters
        return input.replaceAll("<script>", "")
                .replaceAll("</script>", "")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .trim();
    }

    private void validatePhoto(MultipartFile photo) {
        // Check file size
        long sizeInMB = photo.getSize() / (1024 * 1024);
        if (sizeInMB > MAX_PHOTO_SIZE_MB) {
            throw new InvalidRequestException.InvalidFileException("Photo size exceeds " + MAX_PHOTO_SIZE_MB + "MB limit");
        }

        // Check file type
        String contentType = photo.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") &&
                        !contentType.equals("image/png") &&
                        !contentType.equals("image/webp"))) {
            throw new InvalidRequestException.InvalidFileException("Only JPEG, PNG, and WebP images are allowed");
        }
    }

    private List<ReportCluster> clusterReports(List<EnvironmentalReport> reports) {
        // Simple grid-based clustering for map markers
        Map<String, ReportCluster> clusters = new HashMap<>();
        double gridSize = 0.1; // ~11km grid cells

        for (EnvironmentalReport report : reports) {
            int gridLat = (int) (report.getLatitude() / gridSize);
            int gridLon = (int) (report.getLongitude() / gridSize);
            String gridKey = gridLat + "," + gridLon;

            clusters.computeIfAbsent(gridKey, k -> {
                ReportCluster cluster = new ReportCluster();
                cluster.setLatitude(gridLat * gridSize + gridSize / 2);
                cluster.setLongitude(gridLon * gridSize + gridSize / 2);
                cluster.setReports(new ArrayList<>());
                return cluster;
            }).getReports().add(report);
        }

        return new ArrayList<>(clusters.values());
    }
}