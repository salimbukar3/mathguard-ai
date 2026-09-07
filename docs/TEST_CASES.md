# Representative Test Cases

MathGuard AI v1.0 focuses on **numerical equivalence**, not general mathematical reasoning.

| Expected | AI response excerpt | Expected outcome | Reason |
|---|---|---|---|
| `400` | `Final answer: 400` | PASS | Exact match |
| `0.5` | `Final answer: 1/2` | PASS | Fraction equivalence |
| `0.5` | `Final answer: 50%` | PASS | Percentage equivalence |
| `2.50` | `Final answer: 2.5` | PASS | Scale differences ignored |
| `-5` | `Final answer: 5` | FAIL | Sign matters |
| `3.14159` | `Final answer: 3.1416` | PASS* | Within configured tolerance |
| `400` | `Final answer: 420` | FAIL | Outside tolerance |
| `30` | `Answer: 35 ... Final answer: 30` | PASS | Final committed answer preferred |
| `10` | blank response | FAIL | Extraction failure |
| `0.5` | `Final answer: .5` | PASS | Leading decimal supported |
| `30` | `Final answer: 35 ... Answer: 30` | PASS | Latest explicit correction wins |
| `100` | `Final answer: 1,00` | FAIL | Malformed comma grouping rejected |
| `30` | `The values were 10, 20 and 30` | FAIL | Ambiguous unmarked response is not guessed |
| `60` | `The result is 60 kN across 2 beams` | PASS | Result phrase preferred over unrelated trailing number |
| `5` | `Therefore, 2 + 3 = 5` | PASS | Equation result extracted safely |
| `5` | `Final answer: 2 + 3 = 5` | PASS | Right-hand side of explicit final expression |
| `-5` | `Final answer: −5` | PASS | Unicode mathematical minus supported |
| `0.5` | `Final answer: 50 %` | PASS | Spaced percentage supported |
| `1000` | `Final answer: 1e3` | FAIL | Scientific notation is unsupported and not partially parsed |

`*` Depends on the configured tolerance.

## Synthetic-only examples

The examples in this repository are intentionally synthetic. They do not contain private client data, internal project prompts, confidential rubrics, or proprietary AI-training material.
