package com.mathguard.model;

import java.util.Objects;

/**
 * Represents one evaluation item.
 */
public record MathProblem(
        String question,
        String expectedAnswer,
        String aiResponse
) {
    public MathProblem {
        Objects.requireNonNull(question, "question must not be null");
        Objects.requireNonNull(expectedAnswer, "expectedAnswer must not be null");
        Objects.requireNonNull(aiResponse, "aiResponse must not be null");
    }
}
