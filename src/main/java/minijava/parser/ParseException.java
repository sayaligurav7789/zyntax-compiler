package minijava.parser;

import minijava.lexer.SourceLocation;

/**
 * Exception thrown when a syntax error is encountered during parsing.
 */
public class ParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final SourceLocation location;

    public ParseException(String message, SourceLocation location) {
        super(String.format("Syntax error at [%s]: %s", location, message));
        this.location = location;
    }

    public SourceLocation getLocation() {
        return location;
    }
}
