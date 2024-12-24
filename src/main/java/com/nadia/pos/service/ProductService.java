package com.nadia.pos.service;

import com.nadia.pos.model.Product;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.exceptions.BusinessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    void createProduct(Product product) throws ValidationException;

    void updateProduct(Product product) throws ValidationException;

    void updateStock(Long productId, Integer quantity) throws BusinessException, ValidationException;

    void updatePrice(Long productId, BigDecimal newPrice) throws ValidationException;

    Optional<Product> findProductById(Long id);

    Optional<Product> findProductByCode(String code);

    Optional<Product> findProductByBarcode(String barcode);

    List<Product> findProductsByCategory(String category);

    List<Product> searchProducts(String namePattern);

    List<Product> findLowStockProducts();

    List<Product> findAllProducts();

    void deleteProduct(Long id);

    List<Product> findAll();
}