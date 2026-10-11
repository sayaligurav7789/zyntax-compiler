package minijava.semantic;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when semantic errors are discovered during analysis.
 */
public class SemanticException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final List<SemanticError> errors;

    public SemanticException(List<SemanticError> errors) {
        super(formatErrors(errors));
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    private static String formatErrors(List<SemanticError> errors) {
        if (errors.isEmpty()) {
            return "Unknown semantic error";
        }
        if (errors.size() == 1) {
            return errors.getFirst().toString();
        }
        StringBuilder sb = new StringBuilder("Multiple semantic errors found (" + errors.size() + "):\n");
        for (SemanticError error : errors) {
            sb.append("  - ").append(error).append("\n");
        }
        return sb.toString().trim();
    }
}
