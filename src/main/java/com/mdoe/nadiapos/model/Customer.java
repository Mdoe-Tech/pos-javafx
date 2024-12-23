package com.mdoe.nadiapos.model;

import com.mdoe.nadiapos.enums.CustomerType;
import com.mdoe.nadiapos.exceptions.ValidationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Customer extends BaseEntity {
    private String name;
    private String code;
    private String phone;
    private String email;
    private String address;
    private CustomerType type; // RETAIL, WHOLESALE
    private BigDecimal creditLimit;
    private BigDecimal currentCredit;
    private List<Order> orders;

    public Customer() {
        super();
        this.orders = new ArrayList<>();
        this.creditLimit = BigDecimal.ZERO;
        this.currentCredit = BigDecimal.ZERO;
    }

    @Override
    public void validate() throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Customer name cannot be empty");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number cannot be empty");
        }
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public CustomerType getType() {
        return type;
    }

    public void setType(CustomerType type) {
        this.type = type;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCurrentCredit() {
        return currentCredit;
    }

    public void setCurrentCredit(BigDecimal currentCredit) {
        this.currentCredit = currentCredit;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", type=" + type +
                ", creditLimit=" + creditLimit +
                ", currentCredit=" + currentCredit +
                ", orders=" + orders +
                '}';
    }
}
