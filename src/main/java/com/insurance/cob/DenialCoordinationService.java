package com.insurance.cob;

import java.math.BigDecimal;
import java.util.Objects;

public final class DenialCoordinationService {
    private final PaymentCalculator calculator;

    public DenialCoordinationService() {
        this.calculator = new PaymentCalculator();
    }

    public PaymentCalculation calculateAfterPrimaryDenial(
            BigDecimal allowedAmount,
            BigDecimal secondaryBenefit,
            PaymentStrategy secondaryStrategy,
            ClaimAdjudication primaryAdjudication) {

        Objects.requireNonNull(primaryAdjudication, "Primary adjudication is required");
        if (!primaryAdjudication.denied()) {
            throw new IllegalArgumentException("Primary adjudication must be denied");
        }

        return calculator.calculate(
                allowedAmount,
                BigDecimal.ZERO,
                secondaryBenefit,
                secondaryStrategy);
    }
}