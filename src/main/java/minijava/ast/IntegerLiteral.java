package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Objects;

/**
 * 32-bit signed integer literal expression.
 */
public record IntegerLiteral(SourceLocation location, int value) implements Expression {
    public IntegerLiteral {
        Objects.requireNonNull(location, "Location cannot be null");
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitIntegerLiteral(this, context);
    }
}
