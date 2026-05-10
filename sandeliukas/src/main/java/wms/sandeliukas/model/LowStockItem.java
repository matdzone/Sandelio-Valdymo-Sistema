package wms.sandeliukas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "LowStockItem")
public class LowStockItem {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "createdAt")
    private LocalDate createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_Product")
    private Product product;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}