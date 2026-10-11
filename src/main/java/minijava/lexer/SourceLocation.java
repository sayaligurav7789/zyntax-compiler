package minijava.lexer;

import java.io.Serializable;

/**
 * Represents a location in source code with 1-based line and column indices.
 */
public record SourceLocation(int line, int column) implements Serializable {
    private static final long serialVersionUID = 1L;

    public SourceLocation {
        if (line < 1 || column < 1) {
            throw new IllegalArgumentException("Line and column must be 1-based positive integers, got: " + line + ":" + column);
        }
    }

    public static final SourceLocation UNKNOWN = new SourceLocation(1, 1);

    @Override
    public String toString() {
        return line + ":" + column;
    }
}
