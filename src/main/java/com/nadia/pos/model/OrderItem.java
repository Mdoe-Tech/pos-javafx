package com.nadia.pos.model;

import com.nadia.pos.exceptions.ValidationException;

import java.math.BigDecimal;

public abstract class OrderItem extends BaseEntity {
    protected Product product;
    protected Integer quantity;
    protected BigDecimal unitPrice;
    protected BigDecimal discount;
    protected String notes;

    public OrderItem() {
        super();
        this.quantity = 0;
        this.unitPrice = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
    }

    public abstract BigDecimal getSubtotal();

    @Override
    public void validate() throws ValidationException {
        if (product == null) {
            throw new ValidationException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Unit price must be greater than zero");
        }
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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
}
