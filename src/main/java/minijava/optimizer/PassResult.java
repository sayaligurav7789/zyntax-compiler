package minijava.optimizer;

import minijava.ir.TacProgram;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result returned by an optimization pass.
 */
public record PassResult(TacProgram program, int changeCount, List<String> logs) {
    public PassResult {
        Objects.requireNonNull(program, "Program cannot be null");
        Objects.requireNonNull(logs, "Logs cannot be null");
        logs = Collections.unmodifiableList(logs);
    }

    public boolean hasChanges() {
        return changeCount > 0;
    }
}
