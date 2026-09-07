package com.mathguard.extractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class AnswerExtractorTest {

    private final AnswerExtractor extractor = new AnswerExtractor();

    @ParameterizedTest
    @CsvSource({
            "'Final answer: 400', '400'",
            "'The final answer is 42.', '42'",
            "'Answer: 25', '25'",
            "'Therefore, the answer is 60 kN.', '60'",
            "'Final answer: 50%', '50%'",
            "'Final answer: -5', '-5'",
            "'Final answer: .5', '.5'",
            "'Final answer: -.5', '-.5'",
            "'The calculation gives 10 + 20 = 30', '30'",
            "'12 times 5 gives 60', '60'",
            "'So the result is 60 kN across 2 beams', '60'",
            "'Therefore, 2 + 3 = 5', '5'",
            "'Final answer: 2 + 3 = 5', '5'",
            "'2 + 2 = 4. The result is 5', '5'",
            "'Final answer: 50 %', '50 %'",
            "'Final answer: −5', '-5'"
    })
    void extractsExpectedValue(String response, String expected) {
        assertEquals(expected, extractor.extract(response).orElseThrow());
    }

    @Test
    void prefersLatestFinalAnswerMarker() {
        String response = "Final answer: 41. I rechecked it. Final answer: 42.";
        assertEquals("42", extractor.extract(response).orElseThrow());
    }

    @Test
    void laterExplicitAnswerCanSupersedeEarlierFinalAnswer() {
        String response = "Final answer: 35. Actually, that was wrong. Answer: 30.";
        assertEquals("30", extractor.extract(response).orElseThrow());
    }

    @Test
    void finalAnswerOutranksEarlierAnswerMarkerByBeingLater() {
        String response = "Answer: 35. That was wrong. Final answer: 30.";
        assertEquals("30", extractor.extract(response).orElseThrow());
    }

    @Test
    void usesLastAnswerWhenNoFinalMarkerExists() {
        String response = "Answer: 18. Rechecking gives Answer: 20.";
        assertEquals("20", extractor.extract(response).orElseThrow());
    }

    @Test
    void ambiguousUnmarkedMultipleNumbersReturnEmpty() {
        String response = "The values considered were 10, 20 and 30.";
        assertTrue(extractor.extract(response).isEmpty());
    }

    @Test
    void aSingleUnmarkedNumberCanBeUsedAsFallback() {
        assertEquals("42", extractor.extract("The value obtained was 42").orElseThrow());
    }


    @ParameterizedTest
    @CsvSource({
            "'Final answer: 1.2.3'",
            "'Final answer: 1/2/3'",
            "'Final answer: 1e3'",
            "'Final answer: 3.5e2'"
    })
    void unsupportedCompoundFormsAreNotPartiallyExtracted(String response) {
        assertTrue(extractor.extract(response).isEmpty());
    }

    @Test
    void blankResponseReturnsEmpty() {
        assertTrue(extractor.extract("   ").isEmpty());
    }
}
