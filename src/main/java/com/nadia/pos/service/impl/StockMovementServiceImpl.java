package com.nadia.pos.service.impl;

import com.nadia.pos.dao.InventoryDAO;
import com.nadia.pos.dao.ProductDAO;
import com.nadia.pos.dao.StockMovementDAO;
import com.nadia.pos.dao.EmployeeDAO;
import com.nadia.pos.enums.StockMovementType;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.Employee;
import com.nadia.pos.model.Inventory;
import com.nadia.pos.model.Product;
import com.nadia.pos.model.StockMovement;
import com.nadia.pos.service.StockMovementService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class StockMovementServiceImpl implements StockMovementService {
    private final StockMovementDAO stockMovementDAO;
    private final InventoryDAO inventoryDAO;
    private final ProductDAO productDAO;
    private final EmployeeDAO employeeDAO;

    public StockMovementServiceImpl(StockMovementDAO stockMovementDAO, InventoryDAO inventoryDAO,
                                    ProductDAO productDAO, EmployeeDAO employeeDAO) {
        this.stockMovementDAO = stockMovementDAO;
        this.inventoryDAO = inventoryDAO;
        this.productDAO = productDAO;
        this.employeeDAO = employeeDAO;
    }

    @Override
    public StockMovement recordMovement(StockMovement movement) throws ValidationException, SQLException {
        // Validate movement data
        movement.validate();

        // Load and validate product
        Product product = productDAO.findById(movement.getProduct().getId())
                .orElseThrow(() -> new ValidationException("Product not found"));

        // Load and validate employee
        Employee employee = employeeDAO.findById(movement.getProcessedBy().getId())
                .orElseThrow(() -> new ValidationException("Employee not found"));

        // Get current inventory
        Inventory inventory = inventoryDAO.findByProduct(product.getId());
        if (inventory == null) {
            throw new ValidationException("No inventory record found for product");
        }

        // Set previous and new stock levels
        movement.setPreviousStock(inventory.getQuantity());
        int newStock = calculateNewStock(inventory.getQuantity(), movement.getQuantity(), movement.getType());
        movement.setNewStock(newStock);

        // Validate new stock level
        validateStockLevel(inventory, newStock);

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        movement.setCreatedAt(now);
        movement.setUpdatedAt(now);

        // Record movement
        StockMovement recordedMovement = stockMovementDAO.save(movement);

        // Update inventory
        inventory.setQuantity(newStock);
        inventory.setUpdatedAt(now);
        if (movement.getType() == StockMovementType.RECEIPT) {
            inventory.setLastRestockDate(now);
        }
        inventoryDAO.update(inventory);

        return recordedMovement;
    }

    @Override
    public StockMovement recordAdjustment(Long productId, int quantity, String reason,
                                          BigDecimal unitCost, Long processedById, String notes)
            throws ValidationException, SQLException {
        StockMovement movement = new StockMovement();
        movement.setProduct(new Product());
        movement.setType(StockMovementType.ADJUSTMENT);
        movement.setQuantity(quantity);
        movement.setReferenceNumber(generateReferenceNumber());
        movement.setReason(reason);
        movement.setUnitCost(unitCost);
        movement.setProcessedBy(new Employee());
        movement.setNotes(notes);

        return recordMovement(movement);
    }

    @Override
    public StockMovement recordReceipt(Long productId, int quantity, String referenceNumber,
                                       BigDecimal unitCost, Long processedById, String notes)
            throws ValidationException, SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Receipt quantity must be positive");
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(new Product());
        movement.setType(StockMovementType.RECEIPT);
        movement.setQuantity(quantity);
        movement.setReferenceNumber(referenceNumber);
        movement.setReason("Stock Receipt");
        movement.setUnitCost(unitCost);
        movement.setProcessedBy(new Employee());
        movement.setNotes(notes);

        return recordMovement(movement);
    }

    @Override
    public StockMovement recordTransfer(Long productId, int quantity, String referenceNumber,
                                        String reason, Long processedById, String notes)
            throws ValidationException, SQLException {
        if (quantity <= 0) {
            throw new ValidationException("Transfer quantity must be positive");
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(new Product());
        movement.setType(StockMovementType.TRANSFER);
        movement.setQuantity(-quantity); // Negative quantity for outgoing transfer
        movement.setReferenceNumber(referenceNumber);
        movement.setReason(reason);
        movement.setUnitCost(BigDecimal.ZERO); // Transfers don't affect cost
        movement.setProcessedBy(new Employee());
        movement.setNotes(notes);

        return recordMovement(movement);
    }

    @Override
    public Optional<StockMovement> getMovement(Long id) throws SQLException {
        return stockMovementDAO.findById(id);
    }

    @Override
    public List<StockMovement> getMovementsByProduct(Long productId, LocalDateTime startDate,
                                                     LocalDateTime endDate) throws SQLException {
        return stockMovementDAO.findByProduct(productId, startDate, endDate);
    }

    @Override
    public List<StockMovement> getMovementsByReference(String referenceNumber) throws SQLException {
        return stockMovementDAO.findByReferenceNumber(referenceNumber);
    }

    @Override
    public List<StockMovement> getAllMovements() throws SQLException {
        List<StockMovement> movements = stockMovementDAO.findAll();
                System.out.println("Found " + movements.size() + " movements");
        for (StockMovement movement : movements) {
            System.out.println("Movement ID: " + movement.getId());
            System.out.println("Product: " + movement.getProduct());
            System.out.println("Employee: " + movement.getProcessedBy());
        }
        return movements;
    }

    private int calculateNewStock(int currentStock, int quantity, StockMovementType type) {
        return switch (type) {
            case RECEIPT -> currentStock + quantity;
            case TRANSFER, ADJUSTMENT -> currentStock + quantity;
            default -> throw new IllegalArgumentException("Unsupported movement type: " + type);
        };
    }

    private void validateStockLevel(Inventory inventory, int newStock) throws ValidationException {
        if (newStock < 0) {
            throw new ValidationException("Movement would result in negative stock");
        }
        if (inventory.getMaximumStock() > 0 && newStock > inventory.getMaximumStock()) {
            throw new ValidationException("Movement would exceed maximum stock level");
        }
    }

    private String generateReferenceNumber() {
        return "ADJ" + "-" + System.currentTimeMillis();
    }
}