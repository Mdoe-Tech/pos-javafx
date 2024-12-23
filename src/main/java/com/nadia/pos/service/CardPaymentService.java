package com.nadia.pos.service;

import com.nadia.pos.model.CardPayment;
import java.util.Optional;
import java.util.List;

public interface CardPaymentService extends PaymentService<CardPayment> {
    Optional<CardPayment> getPaymentByAuthCode(String authorizationCode);
    List<CardPayment> getPaymentsByCardType(String cardType);
    boolean validateCardDetails(CardPayment payment);
}