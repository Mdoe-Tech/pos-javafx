package com.mdoe.nadiapos.model;

import com.mdoe.nadiapos.enums.PaymentStatus;

import java.math.BigDecimal;

public class CashPayment extends Payment {
    private BigDecimal cashReceived;
    private BigDecimal changeAmount;

    @Override
    public boolean processPayment() {
        if (cashReceived.compareTo(amount) >= 0) {
            changeAmount = cashReceived.subtract(amount);
            status = PaymentStatus.COMPLETED;
            return true;
        }
        return false;
    }

    @Override
    public boolean voidPayment() {
        if (status == PaymentStatus.COMPLETED) {
            status = PaymentStatus.VOIDED;
            return true;
        }
        return false;
    }

    public BigDecimal getCashReceived() {
        return cashReceived;
    }

    public void setCashReceived(BigDecimal cashReceived) {
        this.cashReceived = cashReceived;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }
}
