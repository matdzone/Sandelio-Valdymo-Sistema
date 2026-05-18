package wms.sandeliukas.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wms.sandeliukas.model.LowStockItem;
import wms.sandeliukas.model.Order;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.model.OrderProduct;
import wms.sandeliukas.repositories.LowStockItemRepository;
import wms.sandeliukas.repositories.OrderRepository;
import wms.sandeliukas.repositories.ProductRepository;
import wms.sandeliukas.repositories.OrderProductRepository;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReorderProductsService {

    private final LowStockItemRepository lowStockItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ReorderProductsService(
            LowStockItemRepository lowStockItemRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderProductRepository orderProductRepository)
    {
        this.lowStockItemRepository = lowStockItemRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderProductRepository = orderProductRepository;
    }

    // 3. requestReorderProductsData()
    public List<LowStockItem> requestReorderProductsData() {
        List<LowStockItem> missingProductsData = getMissingProductsList();

        for (LowStockItem item : missingProductsData) {
            getCurrentStockData(item.getProduct().getId());
        }

        return missingProductsData;
    }

    // 4. getMissingProductsList()
    public List<LowStockItem> getMissingProductsList() {
        return lowStockItemRepository.findAll();
    }

    // 6. getCurrentStockData()
    public Integer getCurrentStockData(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prekė nerasta"));

        return product.getCurrentStock();
    }

    // 1. getMissingProduct()
    public LowStockItem getMissingProduct(Integer lowStockItemId) {
        return lowStockItemRepository.findById(lowStockItemId)
                .orElseThrow(() -> new RuntimeException("Trūkstama prekė nerasta"));
    }

    // 3. getItemData()
    public Product getItemData(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prekė nerasta"));
    }

    // 7. orderData()
    @Transactional
    public Order orderData(Integer lowStockItemId, Integer orderQuantity) {
        return orderReorderProduct(lowStockItemId, orderQuantity);
    }

    // 8. createOrder()
    // 10. updateIncomingOrders()
    // 12. updateOrderStatus("Siunčiama")
    @Transactional
    public Order orderReorderProduct(Integer lowStockItemId, Integer orderQuantity) {
        if (orderQuantity == null || orderQuantity <= 0) {
            throw new RuntimeException("Užsakomas kiekis turi būti didesnis už 0");
        }

        LowStockItem missingProductData = getMissingProduct(lowStockItemId);
        Product itemData = getItemData(missingProductData.getProduct().getId());

        Order createdOrder = createOrder(orderQuantity);

        updateIncomingOrders(createdOrder, itemData);

        updateOrderStatus(createdOrder, 1);

        lowStockItemRepository.delete(missingProductData);

        return createdOrder;
    }

    // 8. createOrder()
    private Order createOrder(Integer orderQuantity) {
        Integer newOrderId = orderRepository.findMaxId() + 1;

        Order order = new Order();
        order.setId(newOrderId);
        order.setArrivingQuantity(orderQuantity);
        order.setOrderDate(LocalDate.now());
        order.setReservationDate(LocalDate.now());

        order.setArrivalDate(null);
        order.setDepartureDate(null);
        order.setDeliveryDuration(7);
        order.setReceived(0);
        order.setSold(0);

        // PaymentStatus: 1 = Reserved
        order.setPaymentStatus(1);

        // OrderStatus pradžioje: 1 = Shipping
        order.setCondition(1);

        order.setAdministratorEmail("admin@warehouse.lt");

        return orderRepository.save(order);
    }

    // 10. updateIncomingOrders()
    private void updateIncomingOrders(
            Order createdOrder,
            Product itemData)
    {
        OrderProduct relation =
                new OrderProduct();

        relation.setOrder(createdOrder);
        relation.setProduct(itemData);

        orderProductRepository.save(relation);
    }

    // 12. updateOrderStatus("Siunčiama")
    private void updateOrderStatus(Order createdOrder, Integer status) {
        createdOrder.setCondition(status);
        orderRepository.save(createdOrder);
    }
}