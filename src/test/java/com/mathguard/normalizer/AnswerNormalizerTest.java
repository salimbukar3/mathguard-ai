package com.mathguard.normalizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AnswerNormalizerTest {

    private final AnswerNormalizer normalizer = new AnswerNormalizer();

    @ParameterizedTest
    @CsvSource({
            "400, 400",
            "400.00, 400.00",
            ".5, 0.5",
            "-.5, -0.5",
            "+.5, 0.5",
            "1/2, 0.5",
            "2/4, 0.5",
            "3/2, 1.5",
            "-3/4, -0.75",
            "'.5/.25', 2",
            "50%, 0.5",
            "12.5%, 0.125",
            "-25%, -0.25",
            "'.5%', 0.005",
            "0%, 0",
            "'1,000', 1000",
            "'1,000.50', 1000.50",
            "+5, 5",
            "−5, -5",
            "'50 %', 0.5"
    })
    void parsesSupportedFormats(String raw, String expected) {
        assertEquals(0, normalizer.parse(raw).compareTo(new BigDecimal(expected)));
    }

    @ParameterizedTest
    @CsvSource({
            "'1,00'",
            "'12,34,567'",
            "'1,0000'",
            "'1 000'",
            "'1,'",
            "',100'",
            "'1.2.3'"
    })
    void rejectsMalformedNumbers(String raw) {
        assertThrows(IllegalArgumentException.class, () -> normalizer.parse(raw));
    }

    @Test
    void preservesFiniteDecimalPrecisionBeyondDecimal128() {
        String raw = "0.123456789012345678901234567890123456789";
        assertEquals(0, normalizer.parse(raw).compareTo(new BigDecimal(raw)));
    }

    @Test
    void rejectsZeroDenominator() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.parse("1/0"));
    }

    @Test
    void rejectsMultipleSlashes() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.parse("1/2/3"));
    }

    @Test
    void rejectsUnsupportedText() {
        assertThrows(IllegalArgumentException.class, () -> normalizer.parse("abc"));
    }
}
