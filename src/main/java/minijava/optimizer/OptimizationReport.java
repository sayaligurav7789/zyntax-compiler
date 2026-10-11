package minijava.optimizer;

import minijava.ir.TacProgram;

import java.util.Collections;
import java.util.List;

/**
 * Detailed report containing optimization pipeline statistics, intermediate steps,
 * and instruction diffs.
 */
public record OptimizationReport(
        TacProgram originalProgram,
        TacProgram optimizedProgram,
        int iterations,
        List<OptimizationReport.Step> steps
) {
    public OptimizationReport {
        steps = Collections.unmodifiableList(steps);
    }

    public record Step(
            int iteration,
            String passName,
            int changesCount,
            List<String> logs,
            TacProgram programAfter
    ) {}

    public int initialCount() {
        return originalProgram.size();
    }

    public int finalCount() {
        return optimizedProgram.size();
    }

    public int totalEliminated() {
        return Math.max(0, initialCount() - finalCount());
    }

    public double reductionPercentage() {
        if (initialCount() == 0) return 0.0;
        return (totalEliminated() * 100.0) / initialCount();
    }

    public String summary() {
        return String.format(
                "Optimization Summary: %d -> %d instructions (%.1f%% reduction) across %d iteration(s)",
                initialCount(), finalCount(), reductionPercentage(), iterations
        );
    }

    public String detailedLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Optimization Pipeline Report ===\n");
        sb.append(summary()).append("\n\n");

        if (steps.isEmpty()) {
            sb.append("No optimization steps executed.\n");
            return sb.toString();
        }

        int currentIter = -1;
        for (Step step : steps) {
            if (step.iteration() != currentIter) {
                currentIter = step.iteration();
                sb.append(String.format("--- Iteration %d ---\n", currentIter));
            }
            sb.append(String.format("  [%s]: %d change(s)\n", step.passName(), step.changesCount()));
            for (String log : step.logs()) {
                sb.append("    * ").append(log).append("\n");
            }
        }
        return sb.toString();
    }
}
