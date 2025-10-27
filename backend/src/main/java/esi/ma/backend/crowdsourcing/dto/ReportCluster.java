package esi.ma.backend.crowdsourcing.dto;


import esi.ma.backend.crowdsourcing.model.EnvironmentalReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCluster {
    private Double latitude;
    private Double longitude;
    private Integer count;
    private List<EnvironmentalReport> reports;

    public Integer getCount() {
        return reports != null ? reports.size() : 0;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<EnvironmentalReport> getReports() {
        return reports;
    }

    public void setReports(List<EnvironmentalReport> reports) {
        this.reports = reports;
    }
}