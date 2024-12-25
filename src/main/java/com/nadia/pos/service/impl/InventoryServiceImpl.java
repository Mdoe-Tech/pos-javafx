package com.nadia.pos.service.impl;

import com.nadia.pos.dao.InventoryDAO;
import com.nadia.pos.dao.ProductDAO;
import com.nadia.pos.dao.StockMovementDAO;
import com.nadia.pos.enums.StockMovementType;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Employee;
import com.nadia.pos.model.Inventory;
import com.nadia.pos.model.Product;
import com.nadia.pos.model.StockMovement;
import com.nadia.pos.service.InventoryService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InventoryServiceImpl implements InventoryService {
    private final InventoryDAO inventoryDAO;
    private final ProductDAO productDAO;
    private final StockMovementDAO stockMovementDAO;


    public InventoryServiceImpl(InventoryDAO inventoryDAO, ProductDAO productDAO, StockMovementDAO stockMovementDAO) {
        this.inventoryDAO = inventoryDAO;
        this.productDAO = productDAO;
        this.stockMovementDAO = stockMovementDAO;
    }

    @Override
    public Inventory createInventory(Inventory inventory, Long processedById)
            throws ValidationException, SQLException {
        // Validate inventory data
        inventory.validate();

        // Validate that the product exists
        Optional<Product> product = productDAO.findById(inventory.getProduct().getId());
        if (product.isEmpty()) {
            throw new ValidationException("Product with ID " + inventory.getProduct().getId() + " does not exist");
        }

        // Check if inventory already exists for this product
        Optional<Inventory> existingInventory = getInventoryByProduct(inventory.getProduct().getId());
        if (existingInventory.isPresent()) {
            throw new ValidationException("Inventory already exists for product with ID " +
                    inventory.getProduct().getId());
        }

        // Set product details from database
        inventory.setProduct(product.get());

        // Set initial timestamps
        LocalDateTime now = LocalDateTime.now();
        inventory.setCreatedAt(now);
        inventory.setUpdatedAt(now);

        // Save inventory
        Inventory savedInventory = inventoryDAO.save(inventory);

        // Create initial stock movement
        createInitialStockMovement(savedInventory, processedById);

        return savedInventory;
    }

    private void createInitialStockMovement(Inventory inventory, Long processedById)
            throws ValidationException, SQLException {
        StockMovement movement = new StockMovement();
        movement.setProduct(inventory.getProduct());
        movement.setType(StockMovementType.RECEIPT);
        movement.setQuantity(inventory.getQuantity());
        movement.setReferenceNumber("INIT-" + System.currentTimeMillis());
        movement.setReason("Initial Inventory Setup");
        movement.setUnitCost(BigDecimal.valueOf(inventory.getQuantity()));

        Employee employee = new Employee();
        employee.setId(processedById);
        movement.setProcessedBy(employee);

        movement.setNotes("Initial inventory creation");
        movement.setPreviousStock(0);
        movement.setNewStock(inventory.getQuantity());

        LocalDateTime now = LocalDateTime.now();
        movement.setCreatedAt(now);
        movement.setUpdatedAt(now);

        stockMovementDAO.save(movement);
    }


    @Override
    public Inventory updateInventory(Long id, Inventory inventory) throws ValidationException, SQLException {
        // Validate inventory data
        inventory.validate();

        // Validate that the product exists
        Optional<Product> product = productDAO.findById(inventory.getProduct().getId());
        if (product.isEmpty()) {
            throw new ValidationException("Product with ID " + inventory.getProduct().getId() + " does not exist");
        }

        // Check if inventory exists
        Optional<Inventory> existingInventory = inventoryDAO.findById(id);
        if (existingInventory.isEmpty()) {
            throw new ValidationException("Inventory with id " + id + " not found");
        }

        // Set product details from database
        inventory.setProduct(product.get());

        // Set ID and timestamps
        inventory.setId(id);
        inventory.setCreatedAt(existingInventory.get().getCreatedAt());
        inventory.setUpdatedAt(LocalDateTime.now());

        return inventoryDAO.update(inventory);
    }

    // Rest of the methods remain the same as they don't need product validation
    @Override
    public Inventory addStock(Long id, int quantity) throws ValidationException, SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity to add must be positive");
        }

        Optional<Inventory> optionalInventory = inventoryDAO.findById(id);
        if (optionalInventory.isEmpty()) {
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

        Optional<Inventory> optionalInventory = inventoryDAO.findById(id);
        if (optionalInventory.isEmpty()) {
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

        Optional<Inventory> optionalInventory = inventoryDAO.findById(id);
        if (optionalInventory.isEmpty()) {
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
        return inventoryDAO.findById(id);
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