package com.mdoe.nadiapos.model;

import com.mdoe.nadiapos.enums.PaymentStatus;

import java.time.LocalDateTime;

public class CardPayment extends Payment {
    private String cardNumber;
    private String cardHolderName;
    private String cardType;
    private String authorizationCode;
    private LocalDateTime expiryDate;

    @Override
    public boolean processPayment() {
        if (validateCard()) {
            status = PaymentStatus.COMPLETED;
            return true;
        }
        return false;
    }

    @Override
    public boolean voidPayment() {
        if (status == PaymentStatus.COMPLETED) {
            status = PaymentStatus.VOIDED;
            return true;
        }
        return false;
    }

    private boolean validateCard() {
        return cardNumber != null &&
                expiryDate != null &&
                expiryDate.isAfter(LocalDateTime.now());
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
