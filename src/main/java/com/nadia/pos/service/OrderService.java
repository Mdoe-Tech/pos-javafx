package com.nadia.pos.service;

import com.nadia.pos.model.Order;
import com.nadia.pos.model.OrderItem;
import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService<T extends Order> {
    T createOrder(T order) throws ValidationException;
    void updateOrder(T order) throws ValidationException;
    T addOrderItem(String orderNumber, OrderItem item) throws ValidationException;
    T removeOrderItem(String orderNumber, Long itemId) throws ValidationException;
    void updateOrderStatus(String orderNumber, OrderStatus status) throws ValidationException;
    T processPayment(String orderNumber, Payment payment) throws ValidationException;
    Optional<T> getOrder(Long id);
    Optional<T> getOrderByNumber(String orderNumber);
    List<T> getOrdersByStatus(OrderStatus status);
    List<T> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<T> getOrdersByEmployee(Long employeeId);
    BigDecimal calculateOrderTotal(String orderNumber);
    BigDecimal getDailySales(LocalDateTime date);
    List<T> findAll();
}