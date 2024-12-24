package com.nadia.pos.dao.impl;

import com.nadia.pos.dao.CardPaymentDAO;
import com.nadia.pos.enums.PaymentStatus;
import com.nadia.pos.model.CardPayment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardPaymentDAOImpl extends PaymentDAOImpl<CardPayment> implements CardPaymentDAO {

    public CardPaymentDAOImpl() throws SQLException {
        super();
    }

    @Override
    protected CardPayment mapResultSetToEntity(ResultSet rs) throws SQLException {
        CardPayment payment = new CardPayment();
        payment.setId(rs.getLong("id"));
        payment.setReferenceNumber(rs.getString("reference_number"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setStatus(PaymentStatus.valueOf(rs.getString("status")));
        payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        payment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        payment.setCardType(rs.getString("card_type"));
        payment.setAuthorizationCode(rs.getString("authorization_code"));
        return payment;
    }

    @Override
    protected void setStatementParameters(PreparedStatement stmt, CardPayment payment) throws SQLException {
        stmt.setString(1, payment.getReferenceNumber());
        stmt.setBigDecimal(2, payment.getAmount());
        stmt.setString(3, payment.getStatus().name());
        stmt.setString(5, payment.getCardType());
        stmt.setString(7, payment.getAuthorizationCode());
        stmt.setTimestamp(8, Timestamp.valueOf(payment.getUpdatedAt()));
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO card_payments (reference_number, amount, status, employee_id, " +
                "card_type, last_four_digits, authorization_code, updated_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE card_payments SET reference_number=?, amount=?, status=?, employee_id=?, " +
                "card_type=?, last_four_digits=?, authorization_code=?, updated_at=? WHERE id=?";
    }

    @Override
    public Optional<CardPayment> findByAuthorizationCode(String authCode) {
        String query = "SELECT * FROM card_payments WHERE authorization_code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, authCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToEntity(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payment by authorization code", e);
        }
    }

    @Override
    public List<CardPayment> findByCardType(String cardType) {
        List<CardPayment> payments = new ArrayList<>();
        String query = "SELECT * FROM card_payments WHERE card_type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, cardType);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                payments.add(mapResultSetToEntity(rs));
            }
            return payments;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by card type", e);
        }
    }
}