package esi.ma.backend.airquality.repository;
import esi.ma.backend.airquality.model.AirQuality;
import esi.ma.backend.airquality.model.O2Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface O2DataRepository extends JpaRepository<O2Data, Long> {

    Optional<O2Data> findFirstByLocationKeyAndTimestampAfterOrderByTimestampDesc(
            String locationKey,
            LocalDateTime threshold
    );

    List<O2Data> findByLocationKeyAndTimestampBetween(
            String locationKey,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT o FROM O2Data o WHERE o.altitude >= :minAltitude ORDER BY o.timestamp DESC")
    List<O2Data> findByMinimumAltitude(int minAltitude);

    @Query("SELECT AVG(o.o2Concentration) FROM O2Data o WHERE " +
            "o.locationKey = :locationKey AND o.timestamp >= :since")
    Double getAverageO2Concentration(String locationKey, LocalDateTime since);
}