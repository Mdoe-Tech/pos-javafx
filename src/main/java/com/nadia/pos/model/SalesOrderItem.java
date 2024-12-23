package com.nadia.pos.model;

import java.math.BigDecimal;

public class SalesOrderItem extends OrderItem {
    @Override
    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).subtract(discount);
    }
}
