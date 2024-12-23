package com.nadia.pos.service.impl;

import com.nadia.pos.dao.CardPaymentDAO;
import com.nadia.pos.dao.EmployeeDAO;
import com.nadia.pos.exceptions.ValidationException;
import com.nadia.pos.model.CardPayment;
import com.nadia.pos.service.CardPaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CardPaymentServiceImpl extends AbstractPaymentServiceImpl<CardPayment> implements CardPaymentService {
    private final CardPaymentDAO cardPaymentDAO;

    public CardPaymentServiceImpl(CardPaymentDAO cardPaymentDAO, EmployeeDAO employeeDAO) {
        super(cardPaymentDAO, employeeDAO);
        this.cardPaymentDAO = cardPaymentDAO;
    }

    @Override
    public CardPayment processPayment(CardPayment payment) throws ValidationException {
        if (!validateCardDetails(payment)) {
            throw new ValidationException("Invalid card details");
        }
        return super.processPayment(payment);
    }

    @Override
    public Optional<CardPayment> getPaymentByAuthCode(String authorizationCode) {
        return cardPaymentDAO.findByAuthorizationCode(authorizationCode);
    }

    @Override
    public List<CardPayment> getPaymentsByCardType(String cardType) {
        return cardPaymentDAO.findByCardType(cardType);
    }

    @Override
    public boolean validateCardDetails(CardPayment payment) {
        return payment.getCardNumber() != null &&
                payment.getExpiryDate() != null &&
                payment.getExpiryDate().isAfter(LocalDateTime.now()) &&
                payment.getCardType() != null &&
                payment.getCardHolderName() != null;
    }

    @Override
    protected String generateReferenceNumber() {
        return "CARD-" + System.currentTimeMillis();
    }
}