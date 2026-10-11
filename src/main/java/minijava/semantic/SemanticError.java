package minijava.semantic;

import minijava.lexer.SourceLocation;

import java.io.Serializable;

/**
 * Diagnostic error recorded during semantic analysis.
 */
public record SemanticError(SourceLocation location, String message) implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return String.format("Semantic error at [%s]: %s", location, message);
    }
}
