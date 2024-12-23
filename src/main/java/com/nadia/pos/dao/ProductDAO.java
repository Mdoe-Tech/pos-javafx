package com.nadia.pos.dao;

import com.nadia.pos.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO extends BaseDAO<Product> {
    Optional<Product> findByCode(String code);
    Optional<Product> findByBarcode(String barcode);
    List<Product> findByCategory(String category);
    List<Product> searchByName(String namePattern);
    List<Product> findLowStock();
    boolean updateStockQuantity(Long productId, Integer quantity);
}
