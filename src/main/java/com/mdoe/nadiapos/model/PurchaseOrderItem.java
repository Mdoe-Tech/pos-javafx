package com.mdoe.nadiapos.model;

import java.math.BigDecimal;

public class PurchaseOrderItem extends OrderItem {
    private Boolean received;
    private Integer receivedQuantity;

    public PurchaseOrderItem() {
        super();
        this.received = false;
        this.receivedQuantity = 0;
    }

    @Override
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
    }

    public Boolean getReceived() {
        return received;
    }

    public void setReceived(Boolean received) {
        this.received = received;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
}
