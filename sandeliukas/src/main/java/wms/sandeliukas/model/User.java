package wms.sandeliukas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "User")
public class User {

    @Id
    @Column(name = "email")
    private String email;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "registrationDate")
    private LocalDate registrationDate;

    @Column(name = "showSystemNotifications")
    private Boolean showSystemNotifications;

    @Column(name = "showMessageNotifications")
    private Boolean showMessageNotifications;

    @Column(name = "isResting")
    private Boolean resting;

    @Column(name = "role")
    private Integer role;

    @Column(name = "status")
    private Integer status;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Boolean getShowSystemNotifications() {
        return showSystemNotifications;
    }

    public void setShowSystemNotifications(Boolean showSystemNotifications) {
        this.showSystemNotifications = showSystemNotifications;
    }

    public Boolean getShowMessageNotifications() {
        return showMessageNotifications;
    }

    public void setShowMessageNotifications(Boolean showMessageNotifications) {
        this.showMessageNotifications = showMessageNotifications;
    }

    public Boolean getResting() {
        return resting;
    }

    public void setResting(Boolean resting) {
        this.resting = resting;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
