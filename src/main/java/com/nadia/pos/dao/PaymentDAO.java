package com.nadia.pos.dao;

import com.nadia.pos.model.Payment;
import com.nadia.pos.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentDAO<T extends Payment> extends BaseDAO<T> {
    List<T> findByStatus(PaymentStatus status);
    List<T> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<T> findByEmployee(Long employeeId);
    Optional<T> findByReference(String referenceNumber);
}