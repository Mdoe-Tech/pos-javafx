package com.nadia.pos.service.impl;

import com.nadia.pos.dao.InventoryDAO;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Inventory;
import com.nadia.pos.service.InventoryService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InventoryServiceImpl implements InventoryService {
    private final InventoryDAO inventoryDAO;

    public InventoryServiceImpl(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }

    @Override
    public Inventory createInventory(Inventory inventory) throws ValidationException, SQLException {
        // Validate inventory data
        inventory.validate();

        // Check if inventory already exists for this product
        Optional<Inventory> existingInventory = getInventoryByProduct(inventory.getProduct().getId());
        if (existingInventory.isPresent()) {
            throw new ValidationException("Inventory already exists for product with ID " +
                    inventory.getProduct().getId());
        }

        // Set initial timestamps
        LocalDateTime now = LocalDateTime.now();
        inventory.setCreatedAt(now);
        inventory.setUpdatedAt(now);

        return inventoryDAO.save(inventory);
    }

    @Override
    public Inventory updateInventory(Long id, Inventory inventory) throws ValidationException, SQLException {
        // Validate inventory data
        inventory.validate();

        // Check if inventory exists
        Optional<Inventory> existingInventory = Optional.ofNullable(inventoryDAO.findById(id));
        if (!existingInventory.isPresent()) {
            throw new ValidationException("Inventory with id " + id + " not found");
        }

        // Set ID and timestamps
        inventory.setId(id);
        inventory.setCreatedAt(existingInventory.get().getCreatedAt());
        inventory.setUpdatedAt(LocalDateTime.now());

        return inventoryDAO.update(inventory);
    }

    @Override
    public Inventory addStock(Long id, int quantity) throws ValidationException, SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity to add must be positive");
        }

        Optional<Inventory> optionalInventory = Optional.ofNullable(inventoryDAO.findById(id));
        if (!optionalInventory.isPresent()) {
            throw new ValidationException("Inventory with id " + id + " not found");
        }

        Inventory inventory = optionalInventory.get();
        if (!inventory.canAddStock(quantity)) {
            throw new ValidationException("Adding " + quantity + " units would exceed maximum stock level of " +
                    inventory.getMaximumStock());
        }

        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setLastRestockDate(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());

        return inventoryDAO.update(inventory);
    }

    @Override
    public Inventory removeStock(Long id, int quantity) throws ValidationException, SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity to remove must be positive");
        }

        Optional<Inventory> optionalInventory = Optional.ofNullable(inventoryDAO.findById(id));
        if (!optionalInventory.isPresent()) {
            throw new ValidationException("Inventory with id " + id + " not found");
        }

        Inventory inventory = optionalInventory.get();
        if (inventory.getQuantity() < quantity) {
            throw new ValidationException("Insufficient stock. Current quantity: " +
                    inventory.getQuantity() + ", Requested: " + quantity);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setUpdatedAt(LocalDateTime.now());

        return inventoryDAO.update(inventory);
    }

    @Override
    public Inventory performStockCheck(Long id, int actualQuantity) throws ValidationException, SQLException {
        if (actualQuantity < 0) {
            throw new ValidationException("Actual quantity cannot be negative");
        }

        Optional<Inventory> optionalInventory = Optional.ofNullable(inventoryDAO.findById(id));
        if (!optionalInventory.isPresent()) {
            throw new ValidationException("Inventory with id " + id + " not found");
        }

        Inventory inventory = optionalInventory.get();
        inventory.setQuantity(actualQuantity);
        inventory.setLastStockCheckDate(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());

        return inventoryDAO.update(inventory);
    }

    @Override
    public Optional<Inventory> getInventory(Long id) throws SQLException {
        return Optional.ofNullable(inventoryDAO.findById(id));
    }

    @Override
    public Optional<Inventory> getInventoryByProduct(Long productId) throws SQLException {
        Inventory inventory = inventoryDAO.findByProduct(productId);
        return Optional.ofNullable(inventory);
    }

    @Override
    public List<Inventory> getAllInventory() throws SQLException {
        return inventoryDAO.findAll();
    }

    @Override
    public List<Inventory> getLowStockInventory() throws SQLException {
        return inventoryDAO.findLowStock();
    }
}