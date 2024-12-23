package com.nadia.pos.service;

import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderItemService<T extends OrderItem> {
    T addItem(T item) throws ValidationException;
    T updateItem(T item) throws ValidationException;
    void removeItem(Long id) throws ValidationException;
    Optional<T> getItem(Long id);
    List<T> getItemsByOrder(Long orderId);
    List<T> getItemsByProduct(Long productId);
    BigDecimal calculateSubtotal(T item);
}