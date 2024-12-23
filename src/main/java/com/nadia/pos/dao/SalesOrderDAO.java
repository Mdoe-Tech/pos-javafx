package com.nadia.pos.dao;

import com.nadia.pos.model.SalesOrder;
import com.nadia.pos.enums.SalesType;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderDAO extends BaseDAO<SalesOrder> {
    List<SalesOrder> findByCustomer(Long customerId);
    List<SalesOrder> findByType(SalesType type);
    List<SalesOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<SalesOrder> findPendingDeliveries();
}