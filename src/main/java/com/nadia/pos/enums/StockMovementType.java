package com.nadia.pos.enums;

public enum StockMovementType {
    RECEIPT("Stock Receipt", true),
    TRANSFER("Stock Transfer", false),
    ADJUSTMENT("Stock Adjustment", null),
    PURCHASE_RECEIVE("Purchase Receive", true),
    SALES_DEDUCT("Sales Deduct", false),
    ADJUSTMENT_ADD("Adjustment Add", true),
    ADJUSTMENT_DEDUCT("Adjustment Deduct", false),
    RETURN_FROM_CUSTOMER("Return from Customer", true),
    RETURN_TO_SUPPLIER("Return to Supplier", false),
    DAMAGED("Damaged", false),
    EXPIRED("Expired", false),
    TRANSFER_IN("Transfer In", true),
    TRANSFER_OUT("Transfer Out", false);

    private final String description;
    private final Boolean isAddition;  // Using Boolean object to allow null for ADJUSTMENT

    StockMovementType(String description, Boolean isAddition) {
        this.description = description;
        this.isAddition = isAddition;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isAddition() {
        return isAddition;
    }

    /**
     * Determines if the given quantity should be added or subtracted based on the movement type
     * @param quantity The original quantity
     * @return The adjusted quantity (positive or negative) based on movement type
     */
    public int adjustQuantity(int quantity) {
        if (this == ADJUSTMENT) {
            return quantity; // ADJUSTMENT can be either positive or negative already
        }
        return isAddition ? Math.abs(quantity) : -Math.abs(quantity);
    }
}