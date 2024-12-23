package com.nadia.pos.model;

import com.nadia.pos.exceptions.ValidationException;

import java.time.LocalDateTime;

public class Inventory extends BaseEntity {
    private Product product;
    private Integer quantity;
    private Integer minimumStock;
    private Integer maximumStock;
    private String location;
    private String binNumber;
    private LocalDateTime lastRestockDate;
    private LocalDateTime lastStockCheckDate;

    public Inventory() {
        super();
        this.quantity = 0;
        this.minimumStock = 0;
        this.maximumStock = 0;
    }

    @Override
    public void validate() throws ValidationException {
        if (product == null) {
            throw new ValidationException("Product cannot be null");
        }
        if (quantity < 0) {
            throw new ValidationException("Quantity cannot be negative");
        }
        if (minimumStock < 0) {
            throw new ValidationException("Minimum stock cannot be negative");
        }
        if (maximumStock < minimumStock) {
            throw new ValidationException("Maximum stock cannot be less than minimum stock");
        }
    }

    public boolean isLowStock() {
        return quantity <= minimumStock;
    }

    public boolean canAddStock(int amount) {
        return maximumStock == 0 || (quantity + amount) <= maximumStock;
    }

    // Getters and Setters
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }

    public Integer getMaximumStock() {
        return maximumStock;
    }

    public void setMaximumStock(Integer maximumStock) {
        this.maximumStock = maximumStock;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public void setBinNumber(String binNumber) {
        this.binNumber = binNumber;
    }

    public LocalDateTime getLastRestockDate() {
        return lastRestockDate;
    }

    public void setLastRestockDate(LocalDateTime lastRestockDate) {
        this.lastRestockDate = lastRestockDate;
    }

    public LocalDateTime getLastStockCheckDate() {
        return lastStockCheckDate;
    }

    public void setLastStockCheckDate(LocalDateTime lastStockCheckDate) {
        this.lastStockCheckDate = lastStockCheckDate;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "product=" + product +
                ", quantity=" + quantity +
                ", minimumStock=" + minimumStock +
                ", maximumStock=" + maximumStock +
                ", location='" + location + '\'' +
                ", binNumber='" + binNumber + '\'' +
                ", lastRestockDate=" + lastRestockDate +
                ", lastStockCheckDate=" + lastStockCheckDate +
                '}';
    }
}