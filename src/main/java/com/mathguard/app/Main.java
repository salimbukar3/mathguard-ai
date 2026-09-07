package com.mathguard.app;

import com.mathguard.model.MathProblem;
import com.mathguard.scoring.MathGuardEvaluator;

import java.util.List;

/**
 * Small command-line demonstration using synthetic examples.
 */
public final class Main {

    public static void main(String[] args) {
        MathGuardEvaluator evaluator = new MathGuardEvaluator();
        ResultFormatter formatter = new ResultFormatter();

        List<MathProblem> examples = List.of(
                new MathProblem(
                        "A tank contains 250 litres and loses 12%. How much remains?",
                        "220",
                        "12% of 250 = 30. 250 - 30 = 220. Final answer: 220 litres."
                ),
                new MathProblem(
                        "What fraction is equal to 50%?",
                        "1/2",
                        "50% is half of the whole, so the final answer is 50%."
                ),
                new MathProblem(
                        "Calculate 25 × 16.",
                        "400",
                        "25 × 16 = 420. Final answer: 420"
                ),
                new MathProblem(
                        "What is 10 + 20?",
                        "30",
                        "I first wrote Answer: 35. Checking again, 10 + 20 = 30. Final answer: 30"
                )
        );

        for (int i = 0; i < examples.size(); i++) {
            System.out.println("Example " + (i + 1));
            System.out.println(formatter.format(evaluator.evaluate(examples.get(i))));
            System.out.println("-".repeat(52));
        }
    }
}
