package com.nadia.pos.dao;

import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.SalesOrderItem;
import java.util.List;

public interface SalesOrderItemDAO extends BaseDAO<SalesOrderItem> {
    List<OrderItem> findBySalesOrder(Long salesOrderId);
    List<SalesOrderItem> findByProduct(Long productId);
}