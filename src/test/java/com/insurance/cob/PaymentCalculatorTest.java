package com.insurance.cob;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentCalculatorTest {
    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private final PaymentCalculator calculator = new PaymentCalculator();

    @Test
    void standardExampleLeavesNoPatientResponsibility() {
        PaymentCalculation result = calculator.calculate(
                amount("1000"), amount("800"), amount("700"),
                new StandardPaymentStrategy());

        assertEquals(amount("200"), result.secondaryPayment());
        assertEquals(amount("0"), result.patientResponsibility());
        assertEquals(amount("1000"),
                result.primaryPayment().add(result.secondaryPayment()));
    }

    @Test
    void nonDuplicationExampleLeavesTwoHundredForPatient() {
        PaymentCalculation result = calculator.calculate(
                amount("1000"), amount("800"), amount("700"),
                new NonDuplicationPaymentStrategy());

        assertEquals(amount("0"), result.secondaryPayment());
        assertEquals(amount("200"), result.patientResponsibility());
    }

    @Test
    void carveOutPaymentIsCappedByRemainingAllowedAmount() {
        PaymentCalculation result = calculator.calculate(
                amount("1000"), amount("900"), amount("500"),
                new CarveOutPaymentStrategy(amount("300")));

        assertEquals(amount("100"), result.secondaryPayment());
        assertEquals(amount("0"), result.patientResponsibility());
    }

    @Test
    void primaryOnlyCalculationLeavesUnpaidAmountToPatient() {
        PaymentCalculation result = calculator.calculatePrimaryOnly(
                amount("1000"), amount("800"));

        assertEquals(amount("0"), result.secondaryPayment());
        assertEquals(amount("200"), result.patientResponsibility());
    }

    @Test
    void rejectsPrimaryPaymentAboveAllowedAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(
                        amount("1000"), amount("1001"), amount("0"),
                        new StandardPaymentStrategy()));
    }

    @Test
    void calculationRejectsCombinedPaymentsAboveAllowedAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                new PaymentCalculation(
                        amount("1000"), amount("800"), amount("201"), amount("-1")));
    }
}