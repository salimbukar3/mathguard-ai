package com.mathguard.normalizer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.regex.Pattern;

/**
 * Converts supported numerical formats into a common BigDecimal form.
 *
 * Supported in v1.0:
 * - integers
 * - decimals, including .5 and -.5
 * - fractions
 * - percentages
 * - correctly grouped thousands separators
 * - ASCII hyphen-minus and Unicode mathematical minus
 */
public final class AnswerNormalizer {

    private static final MathContext DIVISION_CONTEXT = MathContext.DECIMAL128;

    private static final Pattern DECIMAL = Pattern.compile(
            "[-+]?(?:(?:\\d+)|(?:\\d{1,3}(?:,\\d{3})+))(?:\\.\\d+)?|[-+]?\\.\\d+"
    );

    public BigDecimal parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Answer is blank");
        }

        String value = raw.trim().replace('\u2212', '-');
        boolean percentage = value.endsWith("%");
        if (percentage) {
            value = value.substring(0, value.length() - 1).trim();
        }

        if (value.isBlank()) {
            throw new IllegalArgumentException("Answer contains no numerical value");
        }

        BigDecimal parsed;
        int slash = value.indexOf('/');
        if (slash >= 0) {
            if (slash != value.lastIndexOf('/')) {
                throw new IllegalArgumentException("Invalid fraction: " + raw);
            }
            String numeratorRaw = value.substring(0, slash).trim();
            String denominatorRaw = value.substring(slash + 1).trim();
            BigDecimal numerator = parseDecimal(numeratorRaw, raw);
            BigDecimal denominator = parseDecimal(denominatorRaw, raw);
            if (denominator.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("Fraction denominator cannot be zero");
            }
            parsed = dividePreservingExactness(numerator, denominator);
        } else {
            parsed = parseDecimal(value, raw);
        }

        // Division by 100 terminates exactly in decimal representation.
        return percentage ? parsed.movePointLeft(2) : parsed;
    }

    private BigDecimal dividePreservingExactness(BigDecimal numerator, BigDecimal denominator) {
        try {
            return numerator.divide(denominator);
        } catch (ArithmeticException nonTerminatingDecimal) {
            return numerator.divide(denominator, DIVISION_CONTEXT);
        }
    }

    private BigDecimal parseDecimal(String value, String original) {
        if (!DECIMAL.matcher(value).matches()) {
            throw new IllegalArgumentException("Unsupported or malformed numerical answer: " + original);
        }

        try {
            // Do not apply a MathContext here: finite decimal input should retain all digits.
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unsupported numerical answer: " + original, ex);
        }
    }
}
