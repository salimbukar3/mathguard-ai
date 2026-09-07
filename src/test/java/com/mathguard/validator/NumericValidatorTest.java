package com.mathguard.validator;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class NumericValidatorTest {

    @Test
    void exactValuesAreEquivalent() {
        var validator = NumericValidator.defaultValidator();
        var result = validator.compare(new BigDecimal("10"), new BigDecimal("10.0"));
        assertTrue(result.equivalent());
        assertTrue(result.exact());
    }

    @Test
    void smallAbsoluteDifferenceCanPass() {
        var validator = new NumericValidator(new BigDecimal("0.001"), BigDecimal.ZERO);
        var result = validator.compare(new BigDecimal("3.14159"), new BigDecimal("3.1416"));
        assertTrue(result.equivalent());
        assertFalse(result.exact());
    }

    @Test
    void valueOutsideToleranceFails() {
        var validator = new NumericValidator(new BigDecimal("0.0001"), BigDecimal.ZERO);
        var result = validator.compare(new BigDecimal("3.14159"), new BigDecimal("3.15"));
        assertFalse(result.equivalent());
    }

    @Test
    void relativeToleranceCanDominateAbsoluteTolerance() {
        var validator = new NumericValidator(BigDecimal.ZERO, new BigDecimal("0.01"));
        var result = validator.compare(new BigDecimal("1000"), new BigDecimal("1005"));
        assertTrue(result.equivalent());
        assertEquals(0, result.allowedTolerance().compareTo(new BigDecimal("10")));
    }

    @Test
    void negativeValuesCompareCorrectly() {
        var validator = NumericValidator.defaultValidator();
        assertTrue(validator.compare(new BigDecimal("-5"), new BigDecimal("-5.000")).equivalent());
    }

    @Test
    void percentageErrorIsCalculated() {
        var validator = new NumericValidator(BigDecimal.ZERO, BigDecimal.ZERO);
        var result = validator.compare(new BigDecimal("400"), new BigDecimal("420"));
        assertEquals(0, result.percentageError().compareTo(new BigDecimal("5")));
    }

    @Test
    void zeroExpectedAndZeroActualHasZeroPercentageError() {
        var validator = NumericValidator.defaultValidator();
        var result = validator.compare(BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(0, result.percentageError().compareTo(BigDecimal.ZERO));
    }

    @Test
    void zeroExpectedAndNonZeroActualHasNoPercentageError() {
        var validator = NumericValidator.defaultValidator();
        var result = validator.compare(BigDecimal.ZERO, BigDecimal.ONE);
        assertNull(result.percentageError());
    }


    @Test
    void zeroTolerancePreservesHighPrecisionDifference() {
        var validator = new NumericValidator(BigDecimal.ZERO, BigDecimal.ZERO);
        var result = validator.compare(
                new BigDecimal("0.123456789012345678901234567890123456789"),
                new BigDecimal("0.123456789012345678901234567890123456780")
        );
        assertFalse(result.equivalent());
        assertTrue(result.absoluteDifference().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void negativeToleranceIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new NumericValidator(new BigDecimal("-0.1"), BigDecimal.ZERO));
    }
}
