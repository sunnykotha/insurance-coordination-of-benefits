package com.insurance.cob;

import java.math.BigDecimal;

public final class NonDuplicationPaymentStrategy implements PaymentStrategy {
    @Override
    public BigDecimal calculateSecondaryPayment(
            BigDecimal allowedAmount,
            BigDecimal primaryPayment,
            BigDecimal secondaryBenefit) {
        StandardPaymentStrategy.validate(allowedAmount, primaryPayment, secondaryBenefit);
        BigDecimal excessBenefit = secondaryBenefit.subtract(primaryPayment).max(BigDecimal.ZERO);
        BigDecimal remaining = allowedAmount.subtract(primaryPayment).max(BigDecimal.ZERO);
        return excessBenefit.min(remaining);
    }
}