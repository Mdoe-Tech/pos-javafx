package com.nadia.pos.model;

import com.nadia.pos.enums.StockMovementType;
import com.nadia.pos.exceptions.ValidationException;

import java.math.BigDecimal;

public class StockMovement extends BaseEntity {
    private Product product;
    private StockMovementType type;
    private Integer quantity;
    private String referenceNumber;
    private String reason;
    private BigDecimal unitCost;
    private Employee processedBy;
    private String notes;
    private Integer previousStock;
    private Integer newStock;

    public StockMovement() {
        super();
        this.quantity = 0;
        this.unitCost = BigDecimal.ZERO;
    }

    @Override
    public void validate() throws ValidationException {
        if (product == null) {
            throw new ValidationException("Product cannot be null");
        }
        if (type == null) {
            throw new ValidationException("Movement type cannot be null");
        }
        if (quantity == 0) {
            throw new ValidationException("Quantity cannot be zero");
        }
        if (processedBy == null) {
            throw new ValidationException("Processed by employee cannot be null");
        }
        if (referenceNumber == null || referenceNumber.trim().isEmpty()) {
            throw new ValidationException("Reference number cannot be empty");
        }
    }

    public BigDecimal getTotalCost() {
        return unitCost.multiply(BigDecimal.valueOf(Math.abs(quantity)));
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public StockMovementType getType() {
        return type;
    }

    public void setType(StockMovementType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public Employee getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Employee processedBy) {
        this.processedBy = processedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    @Override
    public String toString() {
        return "StockMovement{" +
                "product=" + product +
                ", type=" + type +
                ", quantity=" + quantity +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", reason='" + reason + '\'' +
                ", unitCost=" + unitCost +
                ", processedBy=" + processedBy +
                ", notes='" + notes + '\'' +
                ", previousStock=" + previousStock +
                ", newStock=" + newStock +
                '}';
    }
}
