package com.mathguard.scoring;

import com.mathguard.classification.ErrorType;
import com.mathguard.extractor.AnswerExtractor;
import com.mathguard.model.MathProblem;
import com.mathguard.normalizer.AnswerNormalizer;
import com.mathguard.validator.NumericValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MathGuardEvaluatorTest {

    private final MathGuardEvaluator evaluator = new MathGuardEvaluator();

    @ParameterizedTest
    @CsvSource({
            "'400', 'Final answer: 400'",
            "'0.5', 'Final answer: 1/2'",
            "'1/2', 'Final answer: 50%'",
            "'2.50', 'Final answer: 2.5'",
            "'-5', 'Final answer: -5.0'",
            "'1000', 'Final answer: 1,000'"
    })
    void equivalentRepresentationsPass(String expected, String response) {
        var result = evaluator.evaluate(new MathProblem("Synthetic question", expected, response));
        assertTrue(result.correct());
    }

    @Test
    void wrongArithmeticFails() {
        var result = evaluator.evaluate(new MathProblem(
                "Calculate 25 x 16",
                "400",
                "25 x 16 = 420. Final answer: 420"
        ));
        assertFalse(result.correct());
        assertEquals(ErrorType.NUMERICAL_MISMATCH, result.errorType());
        assertEquals(0, result.percentageError().compareTo(new BigDecimal("5")));
    }

    @Test
    void selfCorrectionUsesFinalCommittedAnswer() {
        var result = evaluator.evaluate(new MathProblem(
                "What is 10 + 20?",
                "30",
                "I first wrote Answer: 35. That was wrong. Final answer: 30"
        ));
        assertTrue(result.correct());
        assertEquals("30", result.extractedAnswer());
    }

    @Test
    void blankAiResponseCausesExtractionFailure() {
        var result = evaluator.evaluate(new MathProblem("Question", "10", " "));
        assertFalse(result.correct());
        assertEquals(ErrorType.EXTRACTION_FAILURE, result.errorType());
    }

    @Test
    void invalidExpectedAnswerIsReported() {
        var result = evaluator.evaluate(new MathProblem("Question", "ten", "Final answer: 10"));
        assertFalse(result.correct());
        assertEquals(ErrorType.INVALID_EXPECTED_ANSWER, result.errorType());
    }

    @Test
    void roundedAnswerCanPassWithinTolerance() {
        var custom = new MathGuardEvaluator(
                new AnswerExtractor(),
                new AnswerNormalizer(),
                new NumericValidator(new BigDecimal("0.001"), BigDecimal.ZERO)
        );
        var result = custom.evaluate(new MathProblem("Approximate pi", "3.14159", "Final answer: 3.1416"));
        assertTrue(result.correct());
        assertEquals(ErrorType.ROUNDING_VARIANCE, result.errorType());
    }

    @Test
    void numberWithoutExplicitMarkerUsesFallback() {
        var result = evaluator.evaluate(new MathProblem(
                "What is 12 times 5?",
                "60",
                "12 times 5 gives 60"
        ));
        assertTrue(result.correct());
        assertEquals("60", result.extractedAnswer());
    }

    @Test
    void leadingDecimalIsHandledCorrectly() {
        var result = evaluator.evaluate(new MathProblem("Half", "0.5", "Final answer: .5"));
        assertTrue(result.correct());
        assertEquals(".5", result.extractedAnswer());
    }

    @Test
    void laterExplicitAnswerSupersedesEarlierFinalAnswer() {
        var result = evaluator.evaluate(new MathProblem(
                "Synthetic question",
                "30",
                "Final answer: 35. Actually, that was wrong. Answer: 30."
        ));
        assertTrue(result.correct());
        assertEquals("30", result.extractedAnswer());
    }

    @Test
    void ambiguousUnmarkedResponseFailsExtractionInsteadOfGuessing() {
        var result = evaluator.evaluate(new MathProblem(
                "Synthetic question",
                "30",
                "The candidate values were 10, 20 and 30."
        ));
        assertFalse(result.correct());
        assertEquals(ErrorType.EXTRACTION_FAILURE, result.errorType());
    }

    @Test
    void malformedCommaGroupingIsRejected() {
        var result = evaluator.evaluate(new MathProblem(
                "Synthetic question",
                "100",
                "Final answer: 1,00"
        ));
        assertFalse(result.correct());
        assertEquals(ErrorType.INVALID_AI_ANSWER, result.errorType());
    }
}
