package com.nadia.pos.dao;

import com.nadia.pos.model.PurchaseOrder;
import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseOrderDAO extends BaseDAO<PurchaseOrder> {
    List<PurchaseOrder> findBySupplier(Long supplierId);
    List<PurchaseOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<PurchaseOrder> findPendingDeliveries();
    List<PurchaseOrder> findByStatus(String status);
}
