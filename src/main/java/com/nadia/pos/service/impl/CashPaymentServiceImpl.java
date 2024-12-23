package com.nadia.pos.service.impl;

import com.nadia.pos.dao.CashPaymentDAO;
import com.nadia.pos.dao.EmployeeDAO;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.CashPayment;
import com.nadia.pos.service.CashPaymentService;
import com.nadia.pos.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CashPaymentServiceImpl extends AbstractPaymentServiceImpl<CashPayment> implements CashPaymentService {
    private final CashPaymentDAO cashPaymentDAO;

    public CashPaymentServiceImpl(CashPaymentDAO cashPaymentDAO, EmployeeDAO employeeDAO) {
        super(cashPaymentDAO, employeeDAO);
        this.cashPaymentDAO = cashPaymentDAO;
    }

    @Override
    public CashPayment processPayment(CashPayment payment) throws ValidationException {
        if (payment.getCashReceived() == null ||
                payment.getCashReceived().compareTo(payment.getAmount()) < 0) {
            throw new ValidationException("Insufficient cash received");
        }
        return super.processPayment(payment);
    }

    @Override
    public List<CashPayment> getPaymentsByMinChangeAmount(BigDecimal minAmount) {
        return cashPaymentDAO.findByChangeAmount(minAmount);
    }

    @Override
    public BigDecimal getTotalCashCollected(LocalDateTime startDate, LocalDateTime endDate) {
        return getPaymentsByDateRange(startDate, endDate).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(CashPayment::getCashReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    protected String generateReferenceNumber() {
        return "CASH-" + System.currentTimeMillis();
    }
}
