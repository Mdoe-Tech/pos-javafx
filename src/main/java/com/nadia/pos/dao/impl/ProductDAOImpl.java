package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.ProductDAO;
import com.nadia.pos.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl extends BaseDAOImpl<Product> implements ProductDAO {

    public ProductDAOImpl() throws SQLException {
        super("products");
    }

    @Override
    protected Product mapResultSetToEntity(ResultSet rs) throws SQLException {
        Product product = new Product(productId);
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setCode(rs.getString("code"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCostPrice(rs.getBigDecimal("cost_price"));
        product.setCategory(rs.getString("category"));
        product.setUnit(rs.getString("unit"));
        product.setMinimumStock(rs.getInt("minimum_stock"));
        product.setBarcode(rs.getString("barcode"));
        product.setImage(rs.getBytes("image"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, Product product) throws SQLException {
        stmt.setString(1, product.getName());
        stmt.setString(2, product.getCode());
        stmt.setString(3, product.getDescription());
        stmt.setBigDecimal(4, product.getPrice());
        stmt.setBigDecimal(5, product.getCostPrice());
        stmt.setString(6, product.getCategory());
        stmt.setString(7, product.getUnit());
        stmt.setInt(8, product.getMinimumStock());
        stmt.setString(9, product.getBarcode());
        stmt.setBytes(10, product.getImage());
        stmt.setTimestamp(11, Timestamp.valueOf(product.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO products (name, code, description, price, cost_price, category, " +
                "unit, minimum_stock, barcode, image, updated_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE products SET name=?, code=?, description=?, price=?, cost_price=?, " +
                "category=?, unit=?, minimum_stock=?, barcode=?, image=?, updated_at=? WHERE id=?";
    }

    @Override
    public Optional<Product> findByCode(String code) {
        String query = "SELECT * FROM products WHERE code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by code", e);
        }
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) {
        String query = "SELECT * FROM products WHERE barcode = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, barcode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by barcode", e);
        }
    }

    @Override
    public List<Product> findByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE category = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToEntity(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding products by category", e);
        }
    }

    @Override
    public List<Product> searchByName(String namePattern) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE name ILIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + namePattern + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToEntity(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Error searching products by name", e);
        }
    }

    @Override
    public List<Product> findLowStock() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE stock_quantity <= minimum_stock";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                products.add(mapResultSetToEntity(rs));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding low stock products", e);
        }
    }

    @Override
    public boolean updateStockQuantity(Long productId, Integer quantity) {
        String query = "UPDATE products SET stock_quantity = stock_quantity + ?, " +
                "updated_at = NOW() WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product stock quantity", e);
        }
    }
}
