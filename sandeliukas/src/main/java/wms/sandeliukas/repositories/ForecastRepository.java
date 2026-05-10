package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wms.sandeliukas.model.Forecast;

import java.util.Optional;

public interface ForecastRepository extends JpaRepository<Forecast, Integer> {

    Optional<Forecast> findFirstByProductId(Integer productId);

    @Query("select coalesce(max(f.id), 0) from Forecast f")
    Integer findMaxId();
}