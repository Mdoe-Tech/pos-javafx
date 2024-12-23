package com.nadia.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseOrder extends Order {
    private Supplier supplier;
    private LocalDateTime expectedDeliveryDate;
    private String shippingTerms;
    private String paymentTerms;

    @Override
    public BigDecimal calculateTotal() {
        totalAmount = items.stream()
                .map(item -> ((PurchaseOrderItem)item).getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalAmount.add(tax).subtract(discount);
    }

    // Additional methods and Getters/Setters...

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public LocalDateTime getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDateTime expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public String getShippingTerms() {
        return shippingTerms;
    }

    public void setShippingTerms(String shippingTerms) {
        this.shippingTerms = shippingTerms;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
}

