package com.insurance.cob;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStrategyTest {
    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    @Test
    void standardPaysOnlyRemainingAllowedAmount() {
        PaymentStrategy strategy = new StandardPaymentStrategy();
        assertEquals(amount("200.00"), strategy.calculateSecondaryPayment(
                amount("1000.00"), amount("800.00"), amount("700.00")));
    }

    @Test
    void standardReturnsZeroWhenPrimaryExhaustsAllowedAmount() {
        PaymentStrategy strategy = new StandardPaymentStrategy();
        assertEquals(amount("0"), strategy.calculateSecondaryPayment(
                amount("1000"), amount("1000"), amount("700")));
    }

    @Test
    void nonDuplicationPaysOnlyBenefitAbovePrimaryPayment() {
        PaymentStrategy strategy = new NonDuplicationPaymentStrategy();
        assertEquals(amount("0"), strategy.calculateSecondaryPayment(
                amount("1000"), amount("800"), amount("700")));
        assertEquals(amount("100"), strategy.calculateSecondaryPayment(
                amount("1000"), amount("600"), amount("700")));
    }

    @Test
    void nonDuplicationCannotExceedRemainingAllowedAmount() {
        PaymentStrategy strategy = new NonDuplicationPaymentStrategy();
        assertEquals(amount("200"), strategy.calculateSecondaryPayment(
                amount("1000"), amount("800"), amount("1500")));
    }

    @Test
    void carveOutUsesConfiguredAmountAndCapsAtRemainingAllowedAmount() {
        PaymentStrategy strategy = new CarveOutPaymentStrategy(amount("150"));
        assertEquals(amount("150"), strategy.calculateSecondaryPayment(
                amount("1000"), amount("800"), amount("700")));
        assertEquals(amount("100"), strategy.calculateSecondaryPayment(
                amount("1000"), amount("900"), amount("700")));
    }

    @Test
    void rejectsNegativeAmounts() {
        PaymentStrategy strategy = new StandardPaymentStrategy();
        assertThrows(IllegalArgumentException.class, () ->
                strategy.calculateSecondaryPayment(
                        amount("1000"), amount("800"), amount("-1")));
    }

    @Test
    void rejectsPrimaryPaymentAboveAllowedAmount() {
        PaymentStrategy strategy = new StandardPaymentStrategy();
        assertThrows(IllegalArgumentException.class, () ->
                strategy.calculateSecondaryPayment(
                        amount("1000"), amount("1001"), amount("100")));
    }

    @Test
    void rejectsNegativeCarveOut() {
        assertThrows(IllegalArgumentException.class, () ->
                new CarveOutPaymentStrategy(amount("-1")));
    }
}