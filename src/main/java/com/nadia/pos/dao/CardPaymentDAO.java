package com.nadia.pos.dao;

import com.nadia.pos.model.CardPayment;

import java.util.List;
import java.util.Optional;

public interface CardPaymentDAO extends PaymentDAO<CardPayment> {
    Optional<CardPayment> findByAuthorizationCode(String authCode);
    List<CardPayment> findByCardType(String cardType);
}