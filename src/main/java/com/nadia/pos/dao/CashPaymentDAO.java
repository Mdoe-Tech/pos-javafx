package com.nadia.pos.dao;

import com.nadia.pos.model.CashPayment;

import java.math.BigDecimal;
import java.util.List;

public interface CashPaymentDAO extends PaymentDAO<CashPayment> {
    List<CashPayment> findByChangeAmount(BigDecimal minAmount);
}