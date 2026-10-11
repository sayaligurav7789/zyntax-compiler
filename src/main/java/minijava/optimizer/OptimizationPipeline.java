package minijava.optimizer;

import minijava.ir.TacProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Optimization Pipeline coordinator.
 * Chains multiple compiler passes and drives them to a fixpoint (convergence),
 * collecting detailed metrics and intermediate transformation steps.
 */
public class OptimizationPipeline {
    private static final int DEFAULT_MAX_ITERATIONS = 20;

    private final List<OptimizationPass> passes;
    private final int maxIterations;

    public OptimizationPipeline(List<OptimizationPass> passes, int maxIterations) {
        this.passes = List.copyOf(Objects.requireNonNull(passes, "Passes cannot be null"));
        this.maxIterations = maxIterations;
    }

    public OptimizationPipeline(List<OptimizationPass> passes) {
        this(passes, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Creates a default standard optimization pipeline consisting of:
     * 1. Constant Folding
     * 2. Constant Propagation
     * 3. Algebraic Simplification
     * 4. Dead Code Elimination
     */
    public static OptimizationPipeline createDefault() {
        return new OptimizationPipeline(List.of(
                new ConstantFoldingPass(),
                new ConstantPropagationPass(),
                new AlgebraicSimplificationPass(),
                new DeadCodeEliminationPass()
        ));
    }

    /**
     * Executes the optimization pipeline until convergence or until maxIterations is reached.
     *
     * @param initialProgram input TAC program
     * @return optimization report containing before/after programs and step traces
     */
    public OptimizationReport optimize(TacProgram initialProgram) {
        TacProgram current = initialProgram;
        List<OptimizationReport.Step> steps = new ArrayList<>();
        int iteration = 0;

        while (iteration < maxIterations) {
            iteration++;
            int iterationChanges = 0;

            for (OptimizationPass pass : passes) {
                PassResult result = pass.run(current);
                if (result.hasChanges()) {
                    iterationChanges += result.changeCount();
                    steps.add(new OptimizationReport.Step(
                            iteration,
                            pass.name(),
                            result.changeCount(),
                            result.logs(),
                            result.program()
                    ));
                    current = result.program();
                }
            }

            // Fixpoint reached: no pass made any change during this entire iteration
            if (iterationChanges == 0) {
                break;
            }
        }

        return new OptimizationReport(initialProgram, current, iteration, steps);
    }
}
