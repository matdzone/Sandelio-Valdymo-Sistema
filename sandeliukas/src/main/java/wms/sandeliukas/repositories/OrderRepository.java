package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import wms.sandeliukas.model.Order;

import java.time.LocalDate;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query(value = """
            SELECT COALESCE(SUM(o.sold), 0)
            FROM `Order` o
            JOIN OrderProduct op ON op.fk_Order = o.id
            WHERE op.fk_Product = :productId
            """, nativeQuery = true)
    Integer getTotalSold(Integer productId);

    @Query(value = """
            SELECT COUNT(DISTINCT DATE_FORMAT(o.orderDate, '%Y-%m'))
            FROM `Order` o
            JOIN OrderProduct op ON op.fk_Order = o.id
            WHERE op.fk_Product = :productId
            """, nativeQuery = true)
    Integer countSalesMonths(Integer productId);

    @Query(value = """
            SELECT COALESCE(SUM(o.sold), 0)
            FROM `Order` o
            JOIN OrderProduct op ON op.fk_Order = o.id
            WHERE op.fk_Product = :productId
              AND o.orderDate BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Integer getPeriodSold(Integer productId, LocalDate startDate, LocalDate endDate);

    @Query(value = """
            SELECT COALESCE(SUM(o.arrivingQuantity - o.received), 0)
            FROM `Order` o
            JOIN OrderProduct op ON op.fk_Order = o.id
            WHERE op.fk_Product = :productId
            """, nativeQuery = true)
    Integer incomingStock(Integer productId);
    @Query(value = """
        SELECT COALESCE(AVG(o.deliveryDuration), 7)
        FROM `Order` o
        JOIN OrderProduct op ON op.fk_Order = o.id
        WHERE op.fk_Product = :productId
          AND o.deliveryDuration IS NOT NULL
        """, nativeQuery = true)
    Double averageLeadTimeDays(Integer productId);
}