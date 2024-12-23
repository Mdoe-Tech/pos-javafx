package com.nadia.pos.dao;

import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderDAO<T extends Order> extends BaseDAO<T> {
    Optional<T> findByOrderNumber(String orderNumber);
    List<T> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<T> findByStatus(OrderStatus status);
    List<T> findByEmployee(Long employeeId);
}
