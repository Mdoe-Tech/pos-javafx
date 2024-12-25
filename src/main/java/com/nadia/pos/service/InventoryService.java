package com.nadia.pos.service;

import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Inventory;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface InventoryService {
    Inventory createInventory(Inventory inventory, Long processedById)
            throws ValidationException, SQLException;

    Inventory updateInventory(Long id, Inventory inventory) throws ValidationException, SQLException;

    Inventory addStock(Long id, int quantity) throws ValidationException, SQLException;

    Inventory removeStock(Long id, int quantity) throws ValidationException, SQLException;

    Inventory performStockCheck(Long id, int actualQuantity) throws ValidationException, SQLException;

    Optional<Inventory> getInventory(Long id) throws SQLException;

    Optional<Inventory> getInventoryByProduct(Long productId) throws SQLException;

    List<Inventory> getAllInventory() throws SQLException;
    List<Inventory> getLowStockInventory() throws SQLException;
}
