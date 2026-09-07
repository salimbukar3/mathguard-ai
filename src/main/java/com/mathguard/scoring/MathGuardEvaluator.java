package com.mathguard.scoring;

import com.mathguard.classification.ErrorType;
import com.mathguard.extractor.AnswerExtractor;
import com.mathguard.model.EvaluationResult;
import com.mathguard.model.MathProblem;
import com.mathguard.normalizer.AnswerNormalizer;
import com.mathguard.validator.NumericValidator;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Coordinates extraction, normalization, numerical comparison and
 * high-level error classification.
 */
public final class MathGuardEvaluator {

    private final AnswerExtractor extractor;
    private final AnswerNormalizer normalizer;
    private final NumericValidator validator;

    public MathGuardEvaluator() {
        this(new AnswerExtractor(), new AnswerNormalizer(), NumericValidator.defaultValidator());
    }

    public MathGuardEvaluator(
            AnswerExtractor extractor,
            AnswerNormalizer normalizer,
            NumericValidator validator
    ) {
        this.extractor = extractor;
        this.normalizer = normalizer;
        this.validator = validator;
    }

    public EvaluationResult evaluate(MathProblem problem) {
        BigDecimal expected;
        try {
            expected = normalizer.parse(problem.expectedAnswer());
        } catch (IllegalArgumentException ex) {
            return failed(
                    null,
                    null,
                    null,
                    null,
                    null,
                    ErrorType.INVALID_EXPECTED_ANSWER,
                    "Expected answer could not be parsed: " + ex.getMessage()
            );
        }

        Optional<String> extracted = extractor.extract(problem.aiResponse());
        if (extracted.isEmpty()) {
            return failed(
                    null,
                    expected,
                    null,
                    null,
                    null,
                    ErrorType.EXTRACTION_FAILURE,
                    "No numerical answer could be extracted from the AI response."
            );
        }

        BigDecimal actual;
        try {
            actual = normalizer.parse(extracted.get());
        } catch (IllegalArgumentException ex) {
            return failed(
                    extracted.get(),
                    expected,
                    null,
                    null,
                    null,
                    ErrorType.INVALID_AI_ANSWER,
                    "Extracted answer could not be parsed: " + ex.getMessage()
            );
        }

        NumericValidator.ValidationOutcome outcome = validator.compare(expected, actual);

        ErrorType type;
        String message;
        if (outcome.equivalent() && outcome.exact()) {
            type = ErrorType.NONE;
            message = "The extracted answer exactly matches the expected value.";
        } else if (outcome.equivalent()) {
            type = ErrorType.ROUNDING_VARIANCE;
            message = "The extracted answer is within the configured numerical tolerance.";
        } else {
            type = ErrorType.NUMERICAL_MISMATCH;
            message = "The extracted answer differs numerically from the expected value beyond tolerance.";
        }

        return new EvaluationResult(
                outcome.equivalent(),
                extracted.get(),
                expected,
                actual,
                outcome.absoluteDifference(),
                outcome.percentageError(),
                outcome.allowedTolerance(),
                type,
                message
        );
    }

    private EvaluationResult failed(
            String extracted,
            BigDecimal expected,
            BigDecimal actual,
            BigDecimal difference,
            BigDecimal percentageError,
            ErrorType type,
            String message
    ) {
        return new EvaluationResult(
                false,
                extracted,
                expected,
                actual,
                difference,
                percentageError,
                null,
                type,
                message
        );
    }
}
