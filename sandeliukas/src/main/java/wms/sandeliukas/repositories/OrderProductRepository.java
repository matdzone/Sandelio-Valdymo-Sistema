package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wms.sandeliukas.model.OrderProduct;

public interface OrderProductRepository
        extends JpaRepository<OrderProduct,Integer> {
}