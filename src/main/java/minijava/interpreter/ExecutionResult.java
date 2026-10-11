package minijava.interpreter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Result of executing a MiniJava program either via AST interpretation or TAC execution.
 */
public record ExecutionResult(
        List<String> outputLines,
        Map<String, Integer> environment,
        int stepsExecuted,
        long executionTimeNanos
) {
    public ExecutionResult {
        Objects.requireNonNull(outputLines, "Output lines cannot be null");
        Objects.requireNonNull(environment, "Environment cannot be null");
        outputLines = Collections.unmodifiableList(outputLines);
        environment = Collections.unmodifiableMap(environment);
    }

    public String outputText() {
        return String.join("\n", outputLines);
    }

    public boolean hasMatchingOutput(ExecutionResult other) {
        return this.outputLines.equals(other.outputLines);
    }
}
