package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.CashPaymentDAO;
import com.nadia.pos.enums.PaymentStatus;
import com.nadia.pos.model.CashPayment;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class CashPaymentDAOImpl extends PaymentDAOImpl<CashPayment> implements CashPaymentDAO {

    public CashPaymentDAOImpl() throws SQLException {
        super();
    }

    @Override
    protected CashPayment mapResultSetToEntity(ResultSet rs) throws SQLException {
        CashPayment payment = new CashPayment();
        payment.setId(rs.getLong("id"));
        payment.setReferenceNumber(rs.getString("reference_number"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setStatus(PaymentStatus.valueOf(rs.getString("status")));
        payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        payment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        payment.setChangeAmount(rs.getBigDecimal("change_amount"));
        return payment;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, CashPayment payment) throws SQLException {
        stmt.setString(1, payment.getReferenceNumber());
        stmt.setBigDecimal(2, payment.getAmount());
        stmt.setString(3, payment.getStatus().name());
        stmt.setBigDecimal(5, payment.getChangeAmount());
        stmt.setTimestamp(7, Timestamp.valueOf(payment.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO cash_payments (reference_number, amount, status, employee_id, " +
                "change_amount, received_amount, updated_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE cash_payments SET reference_number=?, amount=?, status=?, employee_id=?, " +
                "change_amount=?, received_amount=?, updated_at=? WHERE id=?";
    }

    @Override
    public List<CashPayment> findByChangeAmount(BigDecimal minAmount) {
        List<CashPayment> payments = new ArrayList<>();
        String query = "SELECT * FROM cash_payments WHERE change_amount >= ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBigDecimal(1, minAmount);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by change amount", e);
        }
    }
}