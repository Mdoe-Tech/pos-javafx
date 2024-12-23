package com.nadia.pos.enums;

public enum StockMovementType {
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
    private final boolean isAddition;

    StockMovementType(String description, boolean isAddition) {
        this.description = description;
        this.isAddition = isAddition;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAddition() {
        return isAddition;
    }
}