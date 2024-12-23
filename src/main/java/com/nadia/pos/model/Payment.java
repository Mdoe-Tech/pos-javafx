package com.nadia.pos.model;

import com.nadia.pos.enums.PaymentStatus;
import com.nadia.pos.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class Payment extends BaseEntity {
    protected String referenceNumber;
    protected BigDecimal amount;
    protected PaymentStatus status;
    protected LocalDateTime paymentDate;
    protected String notes;
    protected Employee processedBy;

    public Payment() {
        super();
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public abstract boolean processPayment();
    public abstract boolean voidPayment();

    @Override
    public void validate() throws ValidationException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be greater than zero");
        }
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Employee getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Employee processedBy) {
        this.processedBy = processedBy;
    }
}
