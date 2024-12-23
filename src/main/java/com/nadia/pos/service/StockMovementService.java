package com.nadia.pos.service;

import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.StockMovement;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockMovementService {
    /**
     * Records a new stock movement
     * @param stockMovement The stock movement to record
     * @return The recorded stock movement
     * @throws ValidationException if movement data is invalid
     * @throws SQLException if database operation fails
     */
    StockMovement recordMovement(StockMovement stockMovement) throws ValidationException, SQLException;

    /**
     * Records a stock adjustment
     * @param productId The ID of the product
     * @param quantity The quantity to adjust (positive for increase, negative for decrease)
     * @param reason The reason for adjustment
     * @param unitCost The unit cost of the adjustment
     * @param processedById The ID of the employee processing the adjustment
     * @param notes Additional notes
     * @return The recorded stock movement
     * @throws ValidationException if movement data is invalid
     * @throws SQLException if database operation fails
     */
    StockMovement recordAdjustment(Long productId, int quantity, String reason,
                                   BigDecimal unitCost, Long processedById, String notes)
            throws ValidationException, SQLException;

    /**
     * Records a stock receipt
     * @param productId The ID of the product
     * @param quantity The quantity received
     * @param referenceNumber The reference number (e.g., PO number)
     * @param unitCost The unit cost of received items
     * @param processedById The ID of the employee processing the receipt
     * @param notes Additional notes
     * @return The recorded stock movement
     * @throws ValidationException if movement data is invalid
     * @throws SQLException if database operation fails
     */
    StockMovement recordReceipt(Long productId, int quantity, String referenceNumber,
                                BigDecimal unitCost, Long processedById, String notes)
            throws ValidationException, SQLException;

    /**
     * Records a stock transfer
     * @param productId The ID of the product
     * @param quantity The quantity transferred
     * @param referenceNumber The reference number
     * @param reason The reason for transfer
     * @param processedById The ID of the employee processing the transfer
     * @param notes Additional notes
     * @return The recorded stock movement
     * @throws ValidationException if movement data is invalid
     * @throws SQLException if database operation fails
     */
    StockMovement recordTransfer(Long productId, int quantity, String referenceNumber,
                                 String reason, Long processedById, String notes)
            throws ValidationException, SQLException;

    /**
     * Retrieves a stock movement by ID
     * @param id The ID of the stock movement
     * @return Optional containing the stock movement if found
     * @throws SQLException if database operation fails
     */
    Optional<StockMovement> getMovement(Long id) throws SQLException;

    /**
     * Retrieves all stock movements for a product within a date range
     * @param productId The ID of the product
     * @param startDate The start date
     * @param endDate The end date
     * @return List of stock movements
     * @throws SQLException if database operation fails
     */
    List<StockMovement> getMovementsByProduct(Long productId, LocalDateTime startDate,
                                              LocalDateTime endDate) throws SQLException;

    /**
     * Retrieves all stock movements by reference number
     * @param referenceNumber The reference number to search for
     * @return List of stock movements
     * @throws SQLException if database operation fails
     */
    List<StockMovement> getMovementsByReference(String referenceNumber) throws SQLException;
}