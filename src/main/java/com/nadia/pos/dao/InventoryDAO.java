package com.nadia.pos.dao;

import com.nadia.pos.model.Inventory;

import java.sql.SQLException;
import java.util.List;

public interface InventoryDAO extends BaseDAO<Inventory> {
    Inventory findByProduct(Long productId) throws SQLException;

    String getFindAllQuery();

    List<Inventory> findLowStock() throws SQLException;
}
