package com.nadia.pos.service;

import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.SalesOrder;
import com.nadia.pos.model.SalesOrderItem;
import com.nadia.pos.enums.SalesType;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderService {
    void createSalesOrder(SalesOrder order);
    void updateSalesOrder(SalesOrder order);
    SalesOrder findById(Long id);
    void deleteSalesOrder(Long id);

    List<SalesOrder> findByCustomer(Long customerId);
    List<SalesOrder> findByType(SalesType type);
    List<SalesOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<SalesOrder> findPendingDeliveries();
    List<SalesOrder> findAll();

    // Order item related operations
    SalesOrderItem addOrderItem(Long orderId, SalesOrderItem item);
    void removeOrderItem(Long orderId, Long itemId);
    List<OrderItem> findOrderItems(Long orderId);
    List<SalesOrderItem> findItemsByProduct(Long productId);
}