package com.nadia.pos.dao;

import com.nadia.pos.model.OrderItem;

import java.util.List;

public interface OrderItemDAO<T extends OrderItem> extends BaseDAO<T> {
    List<T> findByOrder(Long orderId);
    List<T> findByProduct(Long productId);
}
