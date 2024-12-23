package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.InventoryDAO;
import com.nadia.pos.model.Inventory;
import com.nadia.pos.model.Product;
import com.nadia.pos.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAOImpl extends BaseDAOImpl<Inventory> implements InventoryDAO {

    private final Connection connection;

    public InventoryDAOImpl() throws SQLException {
        super("inventory");
        this.connection = DatabaseUtil.getConnection();
    }

    @Override
    public Inventory findByProduct(Long productId) throws SQLException {
        String query = """
            SELECT i.*, p.name as product_name 
            FROM inventory i 
            JOIN products p ON i.product_id = p.id 
            WHERE i.product_id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        }
        return null;
    }

    @Override
    public List<Inventory> findLowStock() throws SQLException {
        String query = """
            SELECT i.*, p.name as product_name 
            FROM inventory i 
            JOIN products p ON i.product_id = p.id 
            WHERE i.quantity <= i.minimum_stock AND i.is_active = true
        """;

        List<Inventory> lowStockItems = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                lowStockItems.add(mapResultSetToEntity(rs));
            }
        }
        return lowStockItems;
    }

    @Override
    protected Inventory mapResultSetToEntity(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setId(rs.getLong("id"));

        Product product = new Product();
        product.setId(rs.getLong("product_id"));
        product.setName(rs.getString("product_name"));
        inventory.setProduct(product);

        inventory.setQuantity(rs.getInt("quantity"));
        inventory.setMinimumStock(rs.getInt("minimum_stock"));
        inventory.setMaximumStock(rs.getInt("maximum_stock"));
        inventory.setLocation(rs.getString("location"));
        inventory.setBinNumber(rs.getString("bin_number"));

        Timestamp lastRestockDate = rs.getTimestamp("last_restock_date");
        if (lastRestockDate != null) {
            inventory.setLastRestockDate(lastRestockDate.toLocalDateTime());
        }

        Timestamp lastStockCheckDate = rs.getTimestamp("last_stock_check_date");
        if (lastStockCheckDate != null) {
            inventory.setLastStockCheckDate(lastStockCheckDate.toLocalDateTime());
        }

        inventory.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        inventory.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        inventory.setIsActive(rs.getBoolean("is_active"));

        return inventory;
    }

    @Override
    public Inventory save(Inventory inventory) {
        String query = getInsertQuery();
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(stmt, inventory);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                inventory.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return inventory;
    }

    @Override
    public void update(Inventory inventory) {
        String query = getUpdateQuery();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            setStatementParameters(stmt, inventory);
            stmt.setLong(12, inventory.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, Inventory inventory) throws SQLException {
        stmt.setLong(1, inventory.getProduct().getId());
        stmt.setInt(2, inventory.getQuantity());
        stmt.setInt(3, inventory.getMinimumStock());
        stmt.setInt(4, inventory.getMaximumStock());
        stmt.setString(5, inventory.getLocation());
        stmt.setString(6, inventory.getBinNumber());
        stmt.setTimestamp(7, inventory.getLastRestockDate() != null ?
                Timestamp.valueOf(inventory.getLastRestockDate()) : null);
        stmt.setTimestamp(8, inventory.getLastStockCheckDate() != null ?
                Timestamp.valueOf(inventory.getLastStockCheckDate()) : null);
    }

    @Override
    protected String getInsertQuery() {
        return """
            INSERT INTO inventory (
                product_id, quantity, minimum_stock, maximum_stock, 
                location, bin_number, last_restock_date, last_stock_check_date,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
        """;
    }

    @Override
    protected String getUpdateQuery() {
        return """
            UPDATE inventory SET 
                product_id = ?, 
                quantity = ?, 
                minimum_stock = ?, 
                maximum_stock = ?, 
                location = ?, 
                bin_number = ?, 
                last_restock_date = ?, 
                last_stock_check_date = ?,
                updated_at = NOW()
            WHERE id = ?
        """;
    }

    public void close() {
        DatabaseUtil.closeConnection(connection);
    }
}