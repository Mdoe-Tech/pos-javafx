package com.nadia.pos.service.impl;

import com.nadia.pos.dao.OrderDAO;
import com.nadia.pos.dao.OrderItemDAO;
import com.nadia.pos.dao.EmployeeDAO;
import com.nadia.pos.dao.ProductDAO;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.*;
import com.nadia.pos.service.OrderService;
import com.nadia.pos.enums.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public abstract class AbstractOrderServiceImpl<T extends Order> implements OrderService<T> {
    protected final OrderDAO<T> orderDAO;
    protected final OrderItemDAO<OrderItem> orderItemDAO;
    protected final EmployeeDAO employeeDAO;
    protected final ProductDAO productDAO;
    protected final BigDecimal TAX_RATE = new BigDecimal("0.10");

    protected AbstractOrderServiceImpl(OrderDAO<T> orderDAO, OrderItemDAO<OrderItem> orderItemDAO,
                                       EmployeeDAO employeeDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.employeeDAO = employeeDAO;
        this.productDAO = productDAO;
    }

    @Override
    public T createOrder(T order) throws ValidationException {
        // Validate order data
        order.validate();

        // Validate employee
        Employee employee = employeeDAO.findById(order.getCreatedBy().getId())
                .orElseThrow(() -> new ValidationException("Employee not found"));

        // Validate and process order items
        validateOrderItems(order.getItems());

        // Calculate totals
        calculateOrderTotals(order);

        // Set timestamps and initial status
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setOrderDate(now);
        order.setStatus(OrderStatus.PENDING);

        // Generate order number if not provided
        if (order.getOrderNumber() == null) {
            order.setOrderNumber(generateOrderNumber());
        }

        // Save order
        T savedOrder = orderDAO.save(order);

        // Save order items
        for (OrderItem item : order.getItems()) {
            item.setCreatedAt(now);
            item.setUpdatedAt(now);
            orderItemDAO.save(item);
        }

        return savedOrder;
    }

    @Override
    public T updateOrder(T order) throws ValidationException {
        T existingOrder = orderDAO.findByOrderNumber(order.getOrderNumber())
                .orElseThrow(() -> new ValidationException("Order not found"));

        if (existingOrder.getStatus() == OrderStatus.COMPLETED ||
                existingOrder.getStatus() == OrderStatus.CANCELLED) {
            throw new ValidationException("Cannot update completed or cancelled order");
        }

        validateOrderItems(order.getItems());
        calculateOrderTotals(order);

        order.setUpdatedAt(LocalDateTime.now());

        return orderDAO.update(order);
    }

    @Override
    public T addOrderItem(String orderNumber, OrderItem item) throws ValidationException {
        T order = orderDAO.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ValidationException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ValidationException("Can only add items to pending orders");
        }

        item.validate();
        order.getItems().add(item);
        calculateOrderTotals(order);

        return orderDAO.update(order);
    }

    @Override
    public T removeOrderItem(String orderNumber, Long itemId) throws ValidationException {
        T order = orderDAO.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ValidationException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ValidationException("Can only remove items from pending orders");
        }

        order.getItems().removeIf(item -> item.getId().equals(itemId));
        calculateOrderTotals(order);

        return orderDAO.update(order);
    }

    @Override
    public T updateOrderStatus(String orderNumber, OrderStatus status) throws ValidationException {
        T order = orderDAO.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ValidationException("Order not found"));

        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        return orderDAO.update(order);
    }

    @Override
    public T processPayment(String orderNumber, Payment payment) throws ValidationException {
        T order = orderDAO.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ValidationException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ValidationException("Can only process payment for pending orders");
        }

        if (payment.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new ValidationException("Payment amount must match order total");
        }

        order.setPayment(payment);
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());

        return orderDAO.update(order);
    }

    @Override
    public Optional<T> getOrder(Long id) {
        return orderDAO.findById(id);
    }

    @Override
    public Optional<T> getOrderByNumber(String orderNumber) {
        return orderDAO.findByOrderNumber(orderNumber);
    }

    @Override
    public List<T> getOrdersByStatus(OrderStatus status) {
        return orderDAO.findByStatus(status);
    }

    @Override
    public List<T> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderDAO.findByDateRange(startDate, endDate);
    }

    @Override
    public List<T> getOrdersByEmployee(Long employeeId) {
        return orderDAO.findByEmployee(employeeId);
    }

    @Override
    public BigDecimal calculateOrderTotal(String orderNumber) {
        T order = orderDAO.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return calculateOrderTotals(order);
    }

    @Override
    public BigDecimal getDailySales(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return getOrdersByDateRange(startOfDay, endOfDay).stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    protected BigDecimal calculateOrderTotals(T order) {
        // Calculate subtotal
        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply discount
        BigDecimal discountedSubtotal = subtotal.subtract(order.getDiscount());

        // Calculate tax
        order.setTax(discountedSubtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP));

        // Calculate total
        order.setTotalAmount(discountedSubtotal.add(order.getTax()));

        return order.getTotalAmount();
    }

    protected void validateOrderItems(List<OrderItem> items) throws ValidationException {
        for (OrderItem item : items) {
            item.validate();
            Product product = productDAO.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ValidationException("Product not found: " + item.getProduct().getId()));
        }
    }

    protected void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus)
            throws ValidationException {
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new ValidationException("Cannot change status of completed or cancelled orders");
        }
    }

    protected String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}