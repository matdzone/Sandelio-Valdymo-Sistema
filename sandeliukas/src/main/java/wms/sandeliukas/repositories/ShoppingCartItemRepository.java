package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wms.sandeliukas.model.ShoppingCartItem;

import java.util.List;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Integer> {

    List<ShoppingCartItem> findByBuyerEmailAndBoughtFalse(String buyerEmail);

    List<ShoppingCartItem> findByBuyerEmailAndBoughtFalseAndPurchaseIsNull(String buyerEmail);

    List<ShoppingCartItem> findByPurchaseId(Integer purchaseId);

    boolean existsByBuyerEmailAndProductIdAndBoughtFalse(String buyerEmail, Integer productId);

    @Query("select coalesce(max(s.id), 0) from ShoppingCartItem s")
    Integer findMaxId();
}
