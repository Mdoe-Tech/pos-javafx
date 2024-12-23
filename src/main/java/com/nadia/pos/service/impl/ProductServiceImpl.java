package com.nadia.pos.service.impl;

import com.nadia.pos.dao.ProductDAO;
import com.nadia.pos.model.Product;
import com.nadia.pos.service.ProductService;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.exceptions.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;

    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public Product createProduct(Product product) throws ValidationException {
        // Validate product data
        product.validate();

        // Check for duplicate code or barcode
        validateUniqueness(product);

        // Set default values if not provided
        if (product.getMinimumStock() == null) {
            product.setMinimumStock(0);
        }

        // Validate price and cost relationships
        validatePricing(product);

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productDAO.save(product);
    }

    @Override
    public Product updateProduct(Product product) throws ValidationException {
        // Validate product exists
        productDAO.findById(product.getId());
        // Validate product data
        product.validate();

        // Check for duplicate code or barcode (excluding current product)
        validateUniquenessForUpdate(product);

        // Validate price and cost relationships
        validatePricing(product);

        // Update timestamp
        product.setUpdatedAt(LocalDateTime.now());

        return productDAO.update(product);
    }

    @Override
    public void updateStock(Long productId, Integer quantity) throws BusinessException, ValidationException {
        Product product = productDAO.findById(productId);
        // Prevent negative stock
        if (product.getStockQuantity() + quantity < 0) {
            throw new BusinessException("Insufficient stock available");
        }

        if (!productDAO.updateStockQuantity(productId, quantity)) {
            throw new BusinessException("Failed to update stock quantity");
        }
    }

    @Override
    public void updatePrice(Long productId, BigDecimal newPrice) throws ValidationException {
        Product product = productDAO.findById(productId);
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }

        if (newPrice.compareTo(product.getCostPrice()) <= 0) {
            throw new ValidationException("Price must be greater than cost price");
        }

        product.setPrice(newPrice);
        product.setUpdatedAt(LocalDateTime.now());

        productDAO.update(product);
    }

    @Override
    public Optional<Product> findProductById(Long id) {
        return Optional.ofNullable(productDAO.findById(id));
    }

    @Override
    public Optional<Product> findProductByCode(String code) {
        return productDAO.findByCode(code);
    }

    @Override
    public Optional<Product> findProductByBarcode(String barcode) {
        return productDAO.findByBarcode(barcode);
    }

    @Override
    public List<Product> findProductsByCategory(String category) {
        return productDAO.findByCategory(category);
    }

    @Override
    public List<Product> searchProducts(String namePattern) {
        return productDAO.searchByName(namePattern);
    }

    @Override
    public List<Product> findLowStockProducts() {
        return productDAO.findLowStock();
    }

    private void validateUniqueness(Product product) throws ValidationException {
        // Check unique code
        if (productDAO.findByCode(product.getCode()).isPresent()) {
            throw new ValidationException("Product code already exists");
        }

        // Check unique barcode if provided
        if (product.getBarcode() != null && !product.getBarcode().isEmpty() &&
                productDAO.findByBarcode(product.getBarcode()).isPresent()) {
            throw new ValidationException("Barcode already exists");
        }
    }

    private void validateUniquenessForUpdate(Product product) throws ValidationException {
        // Check unique code
        productDAO.findByCode(product.getCode())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(product.getId())) {
                        try {
                            throw new ValidationException("Product code already exists");
                        } catch (ValidationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        // Check unique barcode if provided
        if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
            productDAO.findByBarcode(product.getBarcode())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(product.getId())) {
                            try {
                                throw new ValidationException("Barcode already exists");
                            } catch (ValidationException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }
    }

    private void validatePricing(Product product) throws ValidationException {
        if (product.getPrice().compareTo(product.getCostPrice()) <= 0) {
            throw new ValidationException("Price must be greater than cost price");
        }

        // Calculate margin percentage
        BigDecimal margin = product.getPrice().subtract(product.getCostPrice()).divide(product.getPrice(), 2, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal(100));

        // Ensure minimum margin of 10%
        if (margin.compareTo(new BigDecimal(10)) < 0) {
            throw new ValidationException("Price must have a minimum margin of 10%");
        }
    }
}