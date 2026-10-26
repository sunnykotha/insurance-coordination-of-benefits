package com.insurance.cob;

import java.math.BigDecimal;

public interface PaymentStrategy {
    BigDecimal calculateSecondaryPayment(
            BigDecimal allowedAmount,
            BigDecimal primaryPayment,
            BigDecimal secondaryBenefit);
}