package wms.sandeliukas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "`Order`")
public class Order {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "arrivingQuantity")
    private Integer arrivingQuantity;

    @Column(name = "orderDate")
    private LocalDate orderDate;

    @Column(name = "arrivalDate")
    private LocalDate arrivalDate;

    @Column(name = "departureDate")
    private LocalDate departureDate;

    @Column(name = "deliveryDuration")
    private Integer deliveryDuration;

    @Column(name = "received")
    private Integer received;

    @Column(name = "sold")
    private Integer sold;

    @Column(name = "reservationDate")
    private LocalDate reservationDate;

    @Column(name = "`condition`")
    private Integer condition;

    @Column(name = "paymentStatus")
    private Integer paymentStatus;

    @Column(name = "fk_Administrator")
    private String administratorEmail;

    public Integer getId() {
        return id;
    }
    public Integer getArrivingQuantity() {
        return arrivingQuantity;
    }
    public LocalDate getOrderDate() {
        return orderDate;
    }
    public LocalDate getArrivalDate() {
        return arrivalDate;
    }
    public LocalDate getDepartureDate() {
        return departureDate;
    }
    public Integer getDeliveryDuration() {
        return deliveryDuration;
    }
    public Integer getReceived() {
        return received;
    }
    public Integer getSold() {
        return sold;
    }
    public LocalDate getReservationDate() {
        return reservationDate;
    }
    public Integer getCondition() {
        return condition;
    }
    public Integer getPaymentStatus() {
        return paymentStatus;
    }
    public String getAdministratorEmail() {
        return administratorEmail;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public void setArrivingQuantity(Integer arrivingQuantity) {
        this.arrivingQuantity = arrivingQuantity;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public void setDeliveryDuration(Integer deliveryDuration) {
        this.deliveryDuration = deliveryDuration;
    }

    public void setReceived(Integer received) {
        this.received = received;
    }

    public void setSold(Integer sold) {
        this.sold = sold;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public void setCondition(Integer condition) {
        this.condition = condition;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setAdministratorEmail(String administratorEmail) {
        this.administratorEmail = administratorEmail;
    }
}