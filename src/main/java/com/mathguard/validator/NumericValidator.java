package com.mathguard.validator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Compares two normalized numerical values using configurable absolute
 * and relative tolerance.
 */
public final class NumericValidator {

    private static final MathContext DIVISION_CONTEXT = MathContext.DECIMAL128;

    private final BigDecimal absoluteTolerance;
    private final BigDecimal relativeTolerance;

    public NumericValidator(BigDecimal absoluteTolerance, BigDecimal relativeTolerance) {
        this.absoluteTolerance = nonNegative(absoluteTolerance, "absoluteTolerance");
        this.relativeTolerance = nonNegative(relativeTolerance, "relativeTolerance");
    }

    public static NumericValidator defaultValidator() {
        return new NumericValidator(new BigDecimal("0.0001"), new BigDecimal("0.00001"));
    }

    public ValidationOutcome compare(BigDecimal expected, BigDecimal actual) {
        Objects.requireNonNull(expected, "expected must not be null");
        Objects.requireNonNull(actual, "actual must not be null");

        // Subtraction and multiplication of finite BigDecimals are exact, so no MathContext
        // is applied here. This prevents unnecessary rounding before the comparison.
        BigDecimal difference = expected.subtract(actual).abs();
        BigDecimal relativeAllowance = expected.abs().multiply(relativeTolerance);
        BigDecimal allowedTolerance = absoluteTolerance.max(relativeAllowance);
        boolean equivalent = difference.compareTo(allowedTolerance) <= 0;
        boolean exact = difference.compareTo(BigDecimal.ZERO) == 0;
        BigDecimal percentageError = percentageError(expected, actual);

        return new ValidationOutcome(
                equivalent,
                exact,
                difference,
                percentageError,
                allowedTolerance
        );
    }

    private BigDecimal percentageError(BigDecimal expected, BigDecimal actual) {
        if (expected.compareTo(BigDecimal.ZERO) == 0) {
            return actual.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null;
        }
        return expected.subtract(actual)
                .abs()
                .divide(expected.abs(), DIVISION_CONTEXT)
                .multiply(BigDecimal.valueOf(100))
                .setScale(6, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

    private static BigDecimal nonNegative(BigDecimal value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
        return value;
    }

    public record ValidationOutcome(
            boolean equivalent,
            boolean exact,
            BigDecimal absoluteDifference,
            BigDecimal percentageError,
            BigDecimal allowedTolerance
    ) {
    }
}
