package com.nadia.pos.service;

import com.nadia.pos.model.Payment;
import com.nadia.pos.enums.PaymentStatus;
import com.nadia.pos.exceptions.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentService<T extends Payment> {
    T processPayment(T payment) throws ValidationException;
    T voidPayment(String referenceNumber) throws ValidationException;
    Optional<T> getPayment(Long id);
    Optional<T> getPaymentByReference(String referenceNumber);
    List<T> getPaymentsByStatus(PaymentStatus status);
    List<T> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<T> getPaymentsByEmployee(Long employeeId);
    BigDecimal getTotalPayments(LocalDateTime startDate, LocalDateTime endDate);
}
