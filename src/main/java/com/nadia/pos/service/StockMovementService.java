package com.nadia.pos.service;

import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.StockMovement;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockMovementService {
    StockMovement recordMovement(StockMovement stockMovement) throws ValidationException, SQLException;
    StockMovement recordAdjustment(Long productId, int quantity, String reason,
                                   BigDecimal unitCost, Long processedById, String notes)
            throws ValidationException, SQLException;

    StockMovement recordReceipt(Long productId, int quantity, String referenceNumber,
                                BigDecimal unitCost, Long processedById, String notes)
            throws ValidationException, SQLException;

    StockMovement recordTransfer(Long productId, int quantity, String referenceNumber,
                                 String reason, Long processedById, String notes)
            throws ValidationException, SQLException;

    Optional<StockMovement> getMovement(Long id) throws SQLException;

    List<StockMovement> getMovementsByProduct(Long productId, LocalDateTime startDate,
                                              LocalDateTime endDate) throws SQLException;

    List<StockMovement> getMovementsByReference(String referenceNumber) throws SQLException;
    List<StockMovement> getAllMovements() throws SQLException;
}