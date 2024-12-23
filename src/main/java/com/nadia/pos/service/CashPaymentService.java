package com.nadia.pos.service;

import com.nadia.pos.model.CashPayment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CashPaymentService extends PaymentService<CashPayment> {
    List<CashPayment> getPaymentsByMinChangeAmount(BigDecimal minAmount);
    BigDecimal getTotalCashCollected(LocalDateTime startDate, LocalDateTime endDate);
}