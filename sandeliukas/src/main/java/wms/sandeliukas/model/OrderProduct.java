package wms.sandeliukas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "OrderProduct")
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "fk_Order")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "fk_Product")
    private Product product;

    public Integer getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}