package com.nadia.pos.service.impl;

import com.nadia.pos.dao.PaymentDAO;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Payment;
import com.nadia.pos.service.PaymentService;
import com.nadia.pos.enums.PaymentStatus;
import com.nadia.pos.dao.EmployeeDAO;
import com.nadia.pos.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public abstract class AbstractPaymentServiceImpl<T extends Payment> implements PaymentService<T> {
    protected final PaymentDAO<T> paymentDAO;
    protected final EmployeeDAO employeeDAO;

    protected AbstractPaymentServiceImpl(PaymentDAO<T> paymentDAO, EmployeeDAO employeeDAO) {
        this.paymentDAO = paymentDAO;
        this.employeeDAO = employeeDAO;
    }

    @Override
    public T processPayment(T payment) throws ValidationException {
        // Validate payment data
        payment.validate();

        // Validate employee
        Employee employee = employeeDAO.findById(payment.getProcessedBy().getId())
                .orElseThrow(() -> new ValidationException("Employee not found"));

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setPaymentDate(now);

        // Generate reference number if not provided
        if (payment.getReferenceNumber() == null) {
            payment.setReferenceNumber(generateReferenceNumber());
        }

        // Process the payment
        if (payment.processPayment()) {
            return paymentDAO.save(payment);
        } else {
            throw new ValidationException("Payment processing failed");
        }
    }

    @Override
    public T voidPayment(String referenceNumber) throws ValidationException {
        T payment = paymentDAO.findByReference(referenceNumber)
                .orElseThrow(() -> new ValidationException("Payment not found"));

        if (payment.voidPayment()) {
            payment.setUpdatedAt(LocalDateTime.now());
            return paymentDAO.update(payment);
        } else {
            throw new ValidationException("Cannot void payment in current status: " + payment.getStatus());
        }
    }

    @Override
    public Optional<T> getPayment(Long id) {
        return paymentDAO.findById(id);
    }

    @Override
    public Optional<T> getPaymentByReference(String referenceNumber) {
        return paymentDAO.findByReference(referenceNumber);
    }

    @Override
    public List<T> getPaymentsByStatus(PaymentStatus status) {
        return paymentDAO.findByStatus(status);
    }

    @Override
    public List<T> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentDAO.findByDateRange(startDate, endDate);
    }

    @Override
    public List<T> getPaymentsByEmployee(Long employeeId) {
        return paymentDAO.findByEmployee(employeeId);
    }

    @Override
    public BigDecimal getTotalPayments(LocalDateTime startDate, LocalDateTime endDate) {
        return getPaymentsByDateRange(startDate, endDate).stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected String generateReferenceNumber() {
        return "PAY-" + System.currentTimeMillis();
    }
}