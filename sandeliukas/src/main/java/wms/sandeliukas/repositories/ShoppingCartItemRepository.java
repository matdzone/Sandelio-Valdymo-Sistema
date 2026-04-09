package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wms.sandeliukas.model.ShoppingCartItem;

import java.util.List;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Integer> {

    List<ShoppingCartItem> findByBuyerEmailAndBoughtFalse(String buyerEmail);

    List<ShoppingCartItem> findByBuyerEmailAndBoughtFalseAndPurchaseIsNull(String buyerEmail);
}