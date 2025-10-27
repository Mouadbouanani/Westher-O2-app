 package esi.ma.backend.statistics.controller;
import esi.ma.backend.common.dto.ApiResponse;
import esi.ma.backend.statistics.dto.SystemStatsResponse;
import esi.ma.backend.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class StatsController {

    private final StatisticsService statisticsService;

    /**
     * GET /api/stats
     * Get system-wide statistics
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SystemStatsResponse>> getSystemStats() {
        try {
            SystemStatsResponse stats = statisticsService.getSystemStatistics();
            return ResponseEntity.ok(ApiResponse.success(stats));

        } catch (Exception e) {
            log.error("Error getting system statistics", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get statistics"));
        }
    }

    /**
     * GET /api/stats/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("OK", "System is healthy"));
    }
}