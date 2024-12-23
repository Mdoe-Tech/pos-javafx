package com.nadia.pos.service;

import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.PurchaseOrder;
import com.nadia.pos.model.PurchaseOrderItem;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrder createPurchaseOrder(PurchaseOrder order);
    PurchaseOrder updatePurchaseOrder(PurchaseOrder order);
    PurchaseOrder findById(Long id);
    void deletePurchaseOrder(PurchaseOrder purchaseOrder);

    List<PurchaseOrder> findBySupplier(Long supplierId);
    List<PurchaseOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<PurchaseOrder> findPendingDeliveries();
    List<PurchaseOrder> findByStatus(String status);

    // Order item related operations
    PurchaseOrderItem addOrderItem(Long orderId, PurchaseOrderItem item);
    void removeOrderItem(Long orderId, Long itemId);
    List<OrderItem> findOrderItems(Long orderId);
    List<PurchaseOrderItem> findUnreceivedItems(Long orderId);
    void updateReceivedQuantity(Long itemId, Integer quantity);
}