package com.nadia.pos.dao;

import com.nadia.pos.model.StockMovement;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface StockMovementDAO extends BaseDAO<StockMovement> {
    List<StockMovement> findByProduct(Long productId, LocalDateTime startDate, LocalDateTime endDate)
            throws SQLException;
    List<StockMovement> findByReferenceNumber(String referenceNumber) throws SQLException;
}
