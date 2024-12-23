package com.nadia.pos.service.impl;

import com.nadia.pos.dao.SalesOrderDAO;
import com.nadia.pos.dao.SalesOrderItemDAO;
import com.nadia.pos.enums.OrderStatus;
import com.nadia.pos.enums.SalesType;
import com.nadia.pos.model.OrderItem;
import com.nadia.pos.model.SalesOrder;
import com.nadia.pos.model.SalesOrderItem;
import com.nadia.pos.service.SalesOrderService;

import java.time.LocalDateTime;
import java.util.List;

public class SalesOrderServiceImpl implements SalesOrderService {
    private final SalesOrderDAO salesOrderDAO;
    private final SalesOrderItemDAO salesOrderItemDAO;

    public SalesOrderServiceImpl(SalesOrderDAO salesOrderDAO, SalesOrderItemDAO salesOrderItemDAO) {
        this.salesOrderDAO = salesOrderDAO;
        this.salesOrderItemDAO = salesOrderItemDAO;
    }

    @Override
    public SalesOrder createSalesOrder(SalesOrder order) {
        validateSalesOrder(order);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.calculateTotal();

        SalesOrder savedOrder = salesOrderDAO.save(order);

        // Save order items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                salesOrderItemDAO.save((SalesOrderItem) item);
            }
        }

        return savedOrder;
    }

    @Override
    public SalesOrder updateSalesOrder(SalesOrder order) {
        validateSalesOrder(order);
        SalesOrder existingOrder = findById(order.getId());

        if (existingOrder == null) {
            throw new RuntimeException("Sales order not found");
        }

        if (OrderStatus.COMPLETED.equals(existingOrder.getStatus())) {
            throw new RuntimeException("Cannot update completed sales order");
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.calculateTotal();
        return salesOrderDAO.update(order);
    }

    @Override
    public SalesOrder findById(Long id) {
        SalesOrder order = salesOrderDAO.findById(id).orElseThrow(null);
        if (order != null) {
            List<OrderItem> items = salesOrderItemDAO.findBySalesOrder(id);
            order.setItems(items);
        }
        return order;
    }

    @Override
    public void deleteSalesOrder(Long id) {
        SalesOrder order = findById(id);
        if (order == null) {
            throw new RuntimeException("Sales order not found");
        }
        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            throw new RuntimeException("Can only delete pending sales orders");
        }
        salesOrderDAO.delete(order);
    }

    @Override
    public List<SalesOrder> findByCustomer(Long customerId) {
        return salesOrderDAO.findByCustomer(customerId);
    }

    @Override
    public List<SalesOrder> findByType(SalesType type) {
        return salesOrderDAO.findByType(type);
    }

    @Override
    public List<SalesOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return salesOrderDAO.findByDateRange(startDate, endDate);
    }

    @Override
    public List<SalesOrder> findPendingDeliveries() {
        return salesOrderDAO.findPendingDeliveries();
    }

    @Override
    public SalesOrderItem addOrderItem(Long orderId, SalesOrderItem item) {
        SalesOrder order = findById(orderId);
        if (order == null) {
            throw new RuntimeException("Sales order not found");
        }
        if (OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new RuntimeException("Cannot add items to completed sales order");
        }

        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        SalesOrderItem savedItem = salesOrderItemDAO.save(item);

        // Recalculate order total
        order.getItems().add(savedItem);
        order.calculateTotal();
        salesOrderDAO.update(order);

        return savedItem;
    }

    @Override
    public void removeOrderItem(Long orderId, Long itemId) {
        SalesOrder order = findById(orderId);
        if (order == null) {
            throw new RuntimeException("Sales order not found");
        }
        if (OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new RuntimeException("Cannot remove items from completed sales order");
        }

        // Find the item to be deleted
        SalesOrderItem itemToDelete = (SalesOrderItem) order.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        // Delete the item
        salesOrderItemDAO.delete(itemToDelete);

        // Remove from list and recalculate
        order.getItems().remove(itemToDelete);
        order.calculateTotal();
        salesOrderDAO.update(order);
    }

    @Override
    public List<OrderItem> findOrderItems(Long orderId) {
        return salesOrderItemDAO.findBySalesOrder(orderId);
    }

    @Override
    public List<SalesOrderItem> findItemsByProduct(Long productId) {
        return salesOrderItemDAO.findByProduct(productId);
    }

    private void validateSalesOrder(SalesOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Sales order cannot be null");
        }
        if (order.getCustomer() == null) {
            throw new IllegalArgumentException("Customer must be specified");
        }
        if (order.getType() == null) {
            throw new IllegalArgumentException("Sales type must be specified");
        }
        if (order.getDeliveryDate() == null) {
            throw new IllegalArgumentException("Delivery date must be specified");
        }
        if (order.getDeliveryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Delivery date must be in the future");
        }
        if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address must be specified");
        }
    }
}