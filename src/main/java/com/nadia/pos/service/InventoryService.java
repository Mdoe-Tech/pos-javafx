package com.nadia.pos.service;

import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Inventory;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface InventoryService {
    /**
     * Creates a new inventory record
     * @param inventory The inventory record to create
     * @return The created inventory record
     * @throws ValidationException if inventory data is invalid
     * @throws SQLException if database operation fails
     */
    Inventory createInventory(Inventory inventory) throws ValidationException, SQLException;

    /**
     * Updates an existing inventory record
     * @param id The ID of the inventory record to update
     * @param inventory The updated inventory data
     * @return The updated inventory record
     * @throws ValidationException if inventory data is invalid
     * @throws SQLException if database operation fails
     */
    Inventory updateInventory(Long id, Inventory inventory) throws ValidationException, SQLException;

    /**
     * Adds stock to an existing inventory record
     * @param id The ID of the inventory record
     * @param quantity The quantity to add
     * @return The updated inventory record
     * @throws ValidationException if quantity is invalid or would exceed maximum stock
     * @throws SQLException if database operation fails
     */
    Inventory addStock(Long id, int quantity) throws ValidationException, SQLException;

    /**
     * Removes stock from an existing inventory record
     * @param id The ID of the inventory record
     * @param quantity The quantity to remove
     * @return The updated inventory record
     * @throws ValidationException if quantity is invalid or insufficient stock
     * @throws SQLException if database operation fails
     */
    Inventory removeStock(Long id, int quantity) throws ValidationException, SQLException;

    /**
     * Performs a stock check on an inventory record
     * @param id The ID of the inventory record
     * @param actualQuantity The actual quantity found during stock check
     * @return The updated inventory record
     * @throws ValidationException if quantity is invalid
     * @throws SQLException if database operation fails
     */
    Inventory performStockCheck(Long id, int actualQuantity) throws ValidationException, SQLException;

    /**
     * Retrieves an inventory record by ID
     * @param id The ID of the inventory record
     * @return Optional containing the inventory record if found
     * @throws SQLException if database operation fails
     */
    Optional<Inventory> getInventory(Long id) throws SQLException;

    /**
     * Retrieves inventory record by product ID
     * @param productId The ID of the product
     * @return Optional containing the inventory record if found
     * @throws SQLException if database operation fails
     */
    Optional<Inventory> getInventoryByProduct(Long productId) throws SQLException;

    /**
     * Retrieves all inventory records
     * @return List of all inventory records
     * @throws SQLException if database operation fails
     */
    List<Inventory> getAllInventory() throws SQLException;

    /**
     * Retrieves all low stock inventory records
     * @return List of inventory records with quantity at or below minimum stock level
     * @throws SQLException if database operation fails
     */
    List<Inventory> getLowStockInventory() throws SQLException;
}
