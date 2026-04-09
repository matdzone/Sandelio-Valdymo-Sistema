package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wms.sandeliukas.model.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}