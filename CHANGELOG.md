# Changelog

## 1.0.0 - 2026-09-07

### Added
- Numerical answer extraction from AI responses.
- Latest-explicit-marker handling for `Final answer`, `Therefore`, and `Answer` corrections.
- Integer, decimal, leading-decimal, fraction, and percentage normalization.
- `BigDecimal`-based comparison.
- Configurable absolute and relative tolerance.
- Percentage error reporting.
- Structured result classification using conservative `NUMERICAL_MISMATCH` labelling.
- Strict validation of thousands-separator grouping.
- Safer fallback extraction that rejects ambiguous unmarked multi-number responses.
- Result-phrase and equals-sign extraction for common mathematical responses.
- JUnit 5 unit and parameterized tests.
- Synthetic command-line examples.

### Fixed during pre-publication review
- `.5` and `-.5` are now extracted and parsed correctly.
- Later explicit corrections can override earlier `Final answer` markers.
- Result phrases such as `result is 60` no longer default to the last unrelated number in the sentence.
- Malformed values such as `1,00` and `12,34,567` are rejected instead of silently normalized.
- Generic mismatches are no longer overclassified as arithmetic errors.
- Retesting hardened numeric-token boundaries so unsupported forms such as `1e3`, `3.5e2`, `1.2.3`, and `1/2/3` are not partially accepted.
- `Therefore, 2 + 3 = 5` and `Final answer: 2 + 3 = 5` now select the right-hand-side result.
- Later result phrases can supersede earlier unmarked equals-sign candidates.
- Unicode mathematical minus (`−`) is normalized safely.
- Spaced percentages such as `50 %` are handled correctly.
- Finite decimal input now retains all digits rather than being rounded to DECIMAL128 during parsing.
- Exact subtraction and tolerance multiplication no longer apply unnecessary MathContext rounding.

### Known limitations
- No symbolic algebra validation.
- No unit conversion.
- No step-by-step reasoning verification.
- No natural-language semantic evaluation.
