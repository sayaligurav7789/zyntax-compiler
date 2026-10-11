package minijava.lexer;

/**
 * Exception thrown during lexical analysis.
 */
public class LexerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final SourceLocation location;

    public LexerException(String message, SourceLocation location) {
        super(String.format("Lexical error at [%s]: %s", location, message));
        this.location = location;
    }

    public SourceLocation getLocation() {
        return location;
    }
}
