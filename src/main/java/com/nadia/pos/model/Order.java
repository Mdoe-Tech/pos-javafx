package com.nadia.pos.model;

import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Order extends BaseEntity {
    protected String orderNumber;
    protected LocalDateTime orderDate;
    protected List<OrderItem> items;
    protected BigDecimal totalAmount;
    protected BigDecimal tax;
    protected BigDecimal discount;
    protected String notes;
    protected OrderStatus status;
    protected Payment payment;
    protected Employee createdBy;

    public Order() {
        super();
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.status = OrderStatus.PENDING;
    }

    public abstract BigDecimal calculateTotal();

    @Override
    public void validate() throws ValidationException {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            throw new ValidationException("Order number cannot be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber='" + orderNumber + '\'' +
                ", orderDate=" + orderDate +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                ", tax=" + tax +
                ", discount=" + discount +
                ", notes='" + notes + '\'' +
                ", status=" + status +
                ", payment=" + payment +
                ", createdBy=" + createdBy +
                '}';
    }
}
