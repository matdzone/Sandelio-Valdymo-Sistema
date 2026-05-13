package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wms.sandeliukas.model.Purchase;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {

    @Query("select coalesce(max(p.id), 0) from Purchase p")
    Integer findMaxId();

    List<Purchase> findByBuyerEmailAndPaymentStatus(String buyerEmail, Integer paymentStatus);

    Optional<Purchase> findByIdAndBuyerEmail(Integer id, String buyerEmail);
}
