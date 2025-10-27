package esi.ma.backend.crowdsourcing.controller;

import esi.ma.backend.common.dto.ApiResponse;

import esi.ma.backend.common.exception.InvalidRequestException;
import esi.ma.backend.common.exception.RateLimitException;
import esi.ma.backend.common.exception.ResourceNotFoundException;
import esi.ma.backend.crowdsourcing.dto.ReportRequest;
import esi.ma.backend.crowdsourcing.model.EnvironmentalReport;
import esi.ma.backend.crowdsourcing.model.ReportCategory;

import esi.ma.backend.crowdsourcing.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Slf4j
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * POST /api/reports
     * Submit a new environmental report
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EnvironmentalReport>> submitReport(
            @RequestPart("report") @Valid ReportRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            HttpServletRequest httpRequest
    ) {
        try {
            String ipAddress = getClientIP(httpRequest);
            EnvironmentalReport report = reportService.submitReport(request, photo, ipAddress);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(report, "Report submitted successfully"));

        } catch (RateLimitException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (InvalidRequestException.InvalidFileException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error submitting report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to submit report: " + e.getMessage()));
        }
    }

    private String getClientIP(HttpServletRequest httpRequest) {
        String xfHeader = httpRequest.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return httpRequest.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    /**
     * GET /api/reports?lat=35.6762&lon=139.6503&radius=10&category=AIR_POLLUTION&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EnvironmentalReport>>> getReports(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "10") double radius,
            @RequestParam(required = false) ReportCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<EnvironmentalReport> reports = reportService.getReportsNearLocation(
                    lat, lon, radius, category, pageable
            );

            return ResponseEntity.ok(ApiResponse.success(reports));

        } catch (Exception e) {
            log.error("Error fetching reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Faile d to fetch reports: " + e.getMessage()));
        }

    }

    /**
     * GET /api/reports/{id}
     * Get report by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnvironmentalReport>> getReportById(
            @PathVariable Long id
    ) {
        try {
            EnvironmentalReport report = reportService.getReportById(id);
            return ResponseEntity.ok(ApiResponse.success(report));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching report by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch report: " + e.getMessage()));


        }

    }

}