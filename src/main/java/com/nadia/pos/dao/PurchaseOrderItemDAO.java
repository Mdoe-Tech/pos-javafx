package com.nadia.pos.dao;

import com.nadia.pos.model.PurchaseOrderItem;
import java.util.List;

public interface PurchaseOrderItemDAO extends BaseDAO<PurchaseOrderItem> {
    List<PurchaseOrderItem> findByPurchaseOrder(Long purchaseOrderId);
    List<PurchaseOrderItem> findUnreceivedItems(Long purchaseOrderId);
    boolean updateReceivedQuantity(Long itemId, Integer quantity);
}
