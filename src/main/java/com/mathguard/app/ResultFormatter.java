package com.mathguard.app;

import com.mathguard.model.EvaluationResult;

import java.math.BigDecimal;

/**
 * Creates a readable terminal report.
 */
public final class ResultFormatter {

    public String format(EvaluationResult result) {
        return String.join(System.lineSeparator(),
                "Result: " + (result.correct() ? "PASS" : "FAIL"),
                "Extracted answer: " + value(result.extractedAnswer()),
                "Expected value: " + value(result.expectedValue()),
                "Actual value: " + value(result.actualValue()),
                "Absolute difference: " + value(result.absoluteDifference()),
                "Percentage error: " + percentage(result.percentageError()),
                "Allowed tolerance: " + value(result.allowedTolerance()),
                "Classification: " + result.errorType(),
                "Message: " + result.message()
        );
    }

    private String value(Object value) {
        if (value == null) return "N/A";
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros().toPlainString();
        }
        return value.toString();
    }

    private String percentage(BigDecimal value) {
        return value == null ? "N/A" : value.stripTrailingZeros().toPlainString() + "%";
    }
}
