# MathGuard AI

**MathGuard AI** is a Java-based tool for evaluating numerical answers produced by AI systems.

It extracts the most likely final answer from an AI response, normalizes common mathematical formats, compares the result with an expected value, applies configurable numerical tolerance, and returns a structured evaluation report.

> This project does **not** contain or train an AI model. It is a deterministic evaluation tool for checking AI-generated mathematical answers.

## Why this project

The project combines three areas in one practical application:

- **Java** — object-oriented design, packages, records, enums, regex, `BigDecimal`, Maven and JUnit.
- **Mathematics** — numerical equivalence, fractions, percentages, tolerance and percentage error.
- **AI evaluation** — final-answer extraction, structured quality checks, error classification and reproducible scoring.

## Version 1.0 scope

MathGuard AI v1.0 supports:

- Integers
- Decimals
- Fractions
- Percentages, including optional spacing before `%`
- Positive and negative values, including the Unicode mathematical minus sign (`−`)
- Correctly grouped thousands separators
- Absolute tolerance
- Relative tolerance
- Percentage error
- Final-answer extraction
- Self-correcting AI responses using latest explicit answer markers
- Safer ambiguous-response and partial-token handling
- Structured error categories

## Example

### Input

```text
Question:
A tank contains 250 litres and loses 12%. How much remains?

Expected answer:
220

AI response:
12% of 250 = 30.
250 - 30 = 220.
Final answer: 220 litres.
```

### Output

```text
Result: PASS
Extracted answer: 220
Expected value: 220
Actual value: 220
Absolute difference: 0
Percentage error: 0%
Allowed tolerance: 0.0022
Classification: NONE
Message: The extracted answer exactly matches the expected value.
```

## A deliberate improvement over simple validators

A basic validator might compare raw strings:

```java
expected.equals(actual)
```

That incorrectly treats these as different:

```text
1/2
0.5
50%
```

MathGuard normalizes them before comparison, so all three can represent the same value.

It also avoids another common grading mistake: selecting an earlier answer when the model later corrects itself. The extractor chooses the **latest explicit answer marker** (`Final answer`, `Answer`, or `Therefore`) and uses safer fallbacks only when the response is unambiguous.

```text
Answer: 35

That was wrong.
Final answer: 30
```

MathGuard selects the latest explicit committed answer and returns `30`.

## Project structure

```text
src/main/java/com/mathguard/
├── app/
│   ├── Main.java
│   └── ResultFormatter.java
├── classification/
│   └── ErrorType.java
├── extractor/
│   └── AnswerExtractor.java
├── model/
│   ├── EvaluationResult.java
│   └── MathProblem.java
├── normalizer/
│   └── AnswerNormalizer.java
├── scoring/
│   └── MathGuardEvaluator.java
└── validator/
    └── NumericValidator.java
```

## Evaluation pipeline

```text
AI response
    ↓
AnswerExtractor
    ↓
AnswerNormalizer
    ↓
NumericValidator
    ↓
MathGuardEvaluator
    ↓
EvaluationResult
```

## Error classifications

Version 1.0 uses:

- `NONE`
- `ROUNDING_VARIANCE`
- `NUMERICAL_MISMATCH`
- `EXTRACTION_FAILURE`
- `INVALID_EXPECTED_ANSWER`
- `INVALID_AI_ANSWER`

The classifications are intentionally conservative. A wrong value is labelled `NUMERICAL_MISMATCH` rather than claiming a specific arithmetic cause that v1.0 cannot reliably infer.

## Numerical tolerance

The default validator uses:

```text
Absolute tolerance: 0.0001
Relative tolerance: 0.00001
```

The allowed tolerance is the larger of:

```text
absolute tolerance
```

and

```text
|expected value| × relative tolerance
```

This avoids direct floating-point equality while still keeping comparisons controlled and reproducible.

## Why `BigDecimal`

The project uses Java's `BigDecimal` instead of raw `double` equality for decimal comparison. This preserves finite decimal input exactly, gives explicit control over non-terminating division, and avoids relying on binary floating-point equality for grading decisions.

## Tests

The repository includes unit and parameterized tests covering:

- Equivalent decimals
- Fractions
- Percentages, including optional spacing before `%`
- Negative values
- Correctly grouped thousands separators
- Rounding tolerance
- Relative tolerance
- Incorrect numerical answers
- Self-correction
- Latest explicit answer-marker selection
- Blank responses
- Invalid expected values
- Zero-value percentage error handling
- Leading decimals such as `.5` and `-.5`
- Malformed comma grouping rejection
- Ambiguous multi-number response rejection
- Later explicit corrections overriding earlier answer markers
- Expression-style final answers such as `Final answer: 2 + 3 = 5`
- Unicode minus values such as `−5`
- Spaced percentages such as `50 %`
- Rejection of partial extraction from unsupported forms such as `1e3`, `1.2.3`, and `1/2/3`
- High-precision finite decimal preservation

Run the tests with:

```bash
mvn test
```

## Run the demo

Requirements:

- Java 21+
- Maven 3.9+

Compile and run:

```bash
mvn clean package
java -cp target/classes com.mathguard.app.Main
```

## Current limitations

Version 1.0 intentionally does **not** attempt to:

- Prove symbolic algebraic equivalence
- Validate units such as `m`, `kN`, or `MPa`
- Verify every intermediate reasoning step
- Understand arbitrary natural-language mathematical explanations
- Reliably infer a final answer from every unmarked response containing several numbers
- Parse scientific notation such as `1e3` in v1.0
- Judge subjective response quality
- Use an LLM as a grader

These limitations are documented rather than hidden because the goal of v1.0 is a small, testable numerical evaluation engine.

## Planned improvements

### v1.1
- Engineering unit normalization and conversion
- More engineering-style numerical test cases

### v1.2
- Additional extraction heuristics where they can be supported without unsafe guessing

### v2.0
- Symbolic expression equivalence

### v2.1
- Step-by-step mathematical consistency checks

### v3.0
- REST API and simple web interface

## Privacy

All examples in this repository are synthetic. No private AI-training prompts, internal project names, client datasets, confidential rubrics or proprietary task content are included.

## License

MIT License.
