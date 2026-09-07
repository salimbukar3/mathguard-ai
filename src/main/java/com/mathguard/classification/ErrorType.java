package com.mathguard.classification;

/**
 * High-level outcomes supported by MathGuard AI v1.0.
 */
public enum ErrorType {
    NONE,
    ROUNDING_VARIANCE,
    NUMERICAL_MISMATCH,
    EXTRACTION_FAILURE,
    INVALID_EXPECTED_ANSWER,
    INVALID_AI_ANSWER
}
