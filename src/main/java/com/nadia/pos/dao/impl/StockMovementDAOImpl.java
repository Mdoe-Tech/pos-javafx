package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.BaseDAOImpl;
import com.nadia.pos.dao.StockMovementDAO;
import com.nadia.pos.enums.StockMovementType;
import com.nadia.pos.model.Employee;
import com.nadia.pos.model.Product;
import com.nadia.pos.model.StockMovement;
import com.nadia.pos.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

public class StockMovementDAOImpl extends BaseDAOImpl<StockMovement> implements StockMovementDAO {

    private final Connection connection;

    public StockMovementDAOImpl() throws SQLException {
        super("stock_movements");
        this.connection = DatabaseUtil.getConnection();
    }

    @Override
    public List<StockMovement> findByProduct(Long productId, LocalDateTime startDate, LocalDateTime endDate)
            throws SQLException {
        String query = """
            SELECT sm.*, p.name as product_name, e.first_name, e.last_name 
            FROM stock_movements sm
            JOIN products p ON sm.product_id = p.id
            JOIN employees e ON sm.processed_by_id = e.id
            WHERE sm.product_id = ? AND sm.created_at BETWEEN ? AND ?
            ORDER BY sm.created_at DESC
        """;

        List<StockMovement> movements = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, productId);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            stmt.setTimestamp(3, Timestamp.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movements.add(mapResultSetToEntity(rs));
            }
        }
        return movements;
    }

    @Override
    public List<StockMovement> findByReferenceNumber(String referenceNumber) throws SQLException {
        String query = """
            SELECT sm.*, p.name as product_name, e.first_name, e.last_name 
            FROM stock_movements sm
            JOIN products p ON sm.product_id = p.id
            JOIN employees e ON sm.processed_by_id = e.id
            WHERE sm.reference_number = ?
            ORDER BY sm.created_at DESC
        """;

        List<StockMovement> movements = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, referenceNumber);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movements.add(mapResultSetToEntity(rs));
            }
        }
        return movements;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        String query = getInsertQuery();
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(stmt, movement);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                movement.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return movement;
    }

    @Override
    public StockMovement update(StockMovement movement) {
        String query = getUpdateQuery();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            setStatementParameters(stmt, movement);
            stmt.setLong(12, movement.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected StockMovement mapResultSetToEntity(ResultSet rs) throws SQLException {
        StockMovement movement = new StockMovement();
        movement.setId(rs.getLong("id"));

        Product product = new Product();
        product.setId(rs.getLong("product_id"));
        product.setName(rs.getString("product_name"));
        movement.setProduct(product);

        movement.setType(StockMovementType.valueOf(rs.getString("movement_type")));
        movement.setQuantity(rs.getInt("quantity"));
        movement.setReferenceNumber(rs.getString("reference_number"));
        movement.setReason(rs.getString("reason"));
        movement.setUnitCost(rs.getBigDecimal("unit_cost"));

        Employee employee = new Employee();
        employee.setId(rs.getLong("processed_by_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        movement.setProcessedBy(employee);

        movement.setNotes(rs.getString("notes"));
        movement.setPreviousStock(rs.getInt("previous_stock"));
        movement.setNewStock(rs.getInt("new_stock"));

        movement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        movement.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        movement.setIsActive(rs.getBoolean("is_active"));

        return movement;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, StockMovement movement) throws SQLException {
        stmt.setLong(1, movement.getProduct().getId());
        stmt.setString(2, movement.getType().name());
        stmt.setInt(3, movement.getQuantity());
        stmt.setString(4, movement.getReferenceNumber());
        stmt.setString(5, movement.getReason());
        stmt.setBigDecimal(6, movement.getUnitCost());
        stmt.setLong(7, movement.getProcessedBy().getId());
        stmt.setString(9, movement.getNotes());
        stmt.setInt(10, movement.getPreviousStock());
        stmt.setInt(11, movement.getNewStock());
    }

    @Override
    protected String getInsertQuery() {
        return """
                    INSERT INTO stock_movements (
                        product_id, movement_type, quantity, reference_number, 
                        reason, unit_cost, processed_by_id, related_order_id,
                        notes, previous_stock, new_stock, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
    }

    @Override
    protected String getUpdateQuery() {
        return """
                    UPDATE stock_movements SET 
                        product_id = ?, 
                        movement_type = ?, 
                        quantity = ?, 
                        reference_number = ?, 
                        reason = ?, 
                        unit_cost = ?, 
                        processed_by_id = ?, 
                        related_order_id = ?,
                        notes = ?, 
                        previous_stock = ?, 
                        new_stock = ?,
                        updated_at = NOW()
                    WHERE id = ?
                """;
    }

    public void close() {
        DatabaseUtil.closeConnection(connection);
    }
}
