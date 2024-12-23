package com.nadia.pos.service.impl;

import com.nadia.pos.dao.PurchaseOrderDAO;
import com.nadia.pos.dao.PurchaseOrderItemDAO;
import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.PurchaseOrder;
import com.nadia.pos.model.PurchaseOrderItem;
import com.nadia.pos.service.PurchaseOrderService;

import java.time.LocalDateTime;
import java.util.List;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderDAO purchaseOrderDAO;
    private final PurchaseOrderItemDAO purchaseOrderItemDAO;

    public PurchaseOrderServiceImpl(PurchaseOrderDAO purchaseOrderDAO, PurchaseOrderItemDAO purchaseOrderItemDAO) {
        this.purchaseOrderDAO = purchaseOrderDAO;
        this.purchaseOrderItemDAO = purchaseOrderItemDAO;
    }

    @Override
    public PurchaseOrder createPurchaseOrder(PurchaseOrder order) {
        validatePurchaseOrder(order);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.calculateTotal();

        PurchaseOrder savedOrder = purchaseOrderDAO.save(order);

        // Save order items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                PurchaseOrderItem poItem = (PurchaseOrderItem) item;
                poItem.setCreatedAt(LocalDateTime.now());
                poItem.setUpdatedAt(LocalDateTime.now());
                purchaseOrderItemDAO.save(poItem);
            }
        }

        return savedOrder;
    }

    @Override
    public PurchaseOrder updatePurchaseOrder(PurchaseOrder order) {
        validatePurchaseOrder(order);
        PurchaseOrder existingOrder = findById(order.getId());

        if (existingOrder == null) {
            throw new RuntimeException("Purchase order not found");
        }

        if (OrderStatus.COMPLETED.equals(existingOrder.getStatus())) {
            throw new RuntimeException("Cannot update completed purchase order");
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.calculateTotal();
        return purchaseOrderDAO.update(order);
    }

    @Override
    public PurchaseOrder findById(Long id) {
        PurchaseOrder order = purchaseOrderDAO.findById(id).orElseThrow(null);
        if (order != null) {
            List<OrderItem> items = purchaseOrderItemDAO.findByPurchaseOrder(id);
            order.setItems(items);
        }
        return order;
    }

    @Override
    public void deletePurchaseOrder(PurchaseOrder purchaseOrder) {
        PurchaseOrder order = findById(purchaseOrder.getId());
        if (order == null) {
            throw new RuntimeException("Purchase order not found");
        }
        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new RuntimeException("Can only delete pending purchase orders");
        }
        purchaseOrderDAO.delete(order);
    }

    @Override
    public List<PurchaseOrder> findBySupplier(Long supplierId) {
        return purchaseOrderDAO.findBySupplier(supplierId);
    }

    @Override
    public List<PurchaseOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return purchaseOrderDAO.findByDateRange(startDate, endDate);
    }

    @Override
    public List<PurchaseOrder> findPendingDeliveries() {
        return purchaseOrderDAO.findPendingDeliveries();
    }

    @Override
    public List<PurchaseOrder> findByStatus(String status) {
        try {
            OrderStatus.valueOf(status);
            return purchaseOrderDAO.findByStatus(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    @Override
    public PurchaseOrderItem addOrderItem(Long orderId, PurchaseOrderItem item) {
        PurchaseOrder order = findById(orderId);
        if (order == null) {
            throw new RuntimeException("Purchase order not found");
        }
        if (OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new RuntimeException("Cannot add items to completed purchase order");
        }

        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        PurchaseOrderItem savedItem = purchaseOrderItemDAO.save(item);

        // Recalculate order total
        order.getItems().add(savedItem);
        order.calculateTotal();
        purchaseOrderDAO.update(order);

        return savedItem;
    }

    // PurchaseOrderServiceImpl.java (only showing the relevant method)
    @Override
    public void removeOrderItem(Long orderId, Long itemId) {
        PurchaseOrder order = findById(orderId);
        if (order == null) {
            throw new RuntimeException("Purchase order not found");
        }
        if (OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new RuntimeException("Cannot remove items from completed purchase order");
        }

        // Find the item to be deleted
        PurchaseOrderItem itemToDelete = (PurchaseOrderItem) order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        // Delete the item
        purchaseOrderItemDAO.delete(itemToDelete);

        // Remove from list and recalculate
        order.getItems().remove(itemToDelete);
        order.calculateTotal();
        purchaseOrderDAO.update(order);
    }

    @Override
    public List<OrderItem> findOrderItems(Long orderId) {
        return purchaseOrderItemDAO.findByPurchaseOrder(orderId);
    }

    @Override
    public List<PurchaseOrderItem> findUnreceivedItems(Long orderId) {
        return purchaseOrderItemDAO.findUnreceivedItems(orderId);
    }

    @Override
    public void updateReceivedQuantity(Long itemId, Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Received quantity cannot be negative");
        }

        boolean updated = purchaseOrderItemDAO.updateReceivedQuantity(itemId, quantity);
        if (!updated) {
            throw new RuntimeException("Failed to update received quantity");
        }
    }

    private void validatePurchaseOrder(PurchaseOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Purchase order cannot be null");
        }
        if (order.getSupplier() == null) {
            throw new IllegalArgumentException("Supplier must be specified");
        }
        if (order.getExpectedDeliveryDate() == null) {
            throw new IllegalArgumentException("Expected delivery date must be specified");
        }
        if (order.getExpectedDeliveryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Expected delivery date must be in the future");
        }
    }
}