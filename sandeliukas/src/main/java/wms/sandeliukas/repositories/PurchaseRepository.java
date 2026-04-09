package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wms.sandeliukas.model.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {

    @Query("select coalesce(max(p.id), 0) from Purchase p")
    Integer findMaxId();
}