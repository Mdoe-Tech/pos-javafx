package com.nadia.pos.model;

import com.nadia.pos.enums.SalesType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesOrder extends Order {
    private Customer customer;
    private SalesType type;
    private String deliveryAddress;
    private LocalDateTime deliveryDate;

    @Override
    public void calculateTotal() {
        totalAmount = items.stream()
                .map(item -> ((SalesOrderItem)item).getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public SalesType getType() {
        return type;
    }

    public void setType(SalesType type) {
        this.type = type;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
