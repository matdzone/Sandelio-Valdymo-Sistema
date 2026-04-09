package wms.sandeliukas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Purchase")
public class Purchase {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "paymentDate")
    private LocalDate paymentDate;

    @Column(name = "reservationDate")
    private LocalDate reservationDate;

    @Column(name = "pickupDate")
    private LocalDate pickupDate;

    @Column(name = "paymentStatus")
    private Integer paymentStatus;

    @Column(name = "status")
    private Integer status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_Buyer")
    private User buyer;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }
}