package com.mathguard.model;

import com.mathguard.classification.ErrorType;

import java.math.BigDecimal;

/**
 * Structured output from one evaluation.
 */
public record EvaluationResult(
        boolean correct,
        String extractedAnswer,
        BigDecimal expectedValue,
        BigDecimal actualValue,
        BigDecimal absoluteDifference,
        BigDecimal percentageError,
        BigDecimal allowedTolerance,
        ErrorType errorType,
        String message
) {
}
