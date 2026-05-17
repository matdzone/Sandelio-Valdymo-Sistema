package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wms.sandeliukas.model.LowStockItem;

import java.util.Optional;

public interface LowStockItemRepository extends JpaRepository<LowStockItem, Integer> {

    Optional<LowStockItem> findFirstByProductId(Integer productId);

    @Query("select coalesce(max(l.id), 0) from LowStockItem l")
    Integer findMaxId();

    void deleteByProductId(Integer productId);
}