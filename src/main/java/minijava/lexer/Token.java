package minijava.lexer;

import java.util.Objects;

/**
 * Represents a lexical token emitted by the MiniJava lexer.
 */
public record Token(TokenType type, String lexeme, Object literalValue, SourceLocation location) {
    public Token {
        Objects.requireNonNull(type, "Token type cannot be null");
        Objects.requireNonNull(lexeme, "Lexeme cannot be null");
        Objects.requireNonNull(location, "Location cannot be null");
    }

    public Token(TokenType type, String lexeme, SourceLocation location) {
        this(type, lexeme, null, location);
    }

    public int intValue() {
        if (literalValue instanceof Integer i) {
            return i;
        }
        throw new IllegalStateException("Token does not hold an integer literal value: " + this);
    }

    @Override
    public String toString() {
        if (literalValue != null) {
            return String.format("%s('%s', %s) at %s", type, lexeme, literalValue, location);
        }
        return String.format("%s('%s') at %s", type, lexeme, location);
    }
}
