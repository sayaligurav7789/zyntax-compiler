package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Objects;

/**
 * Variable reference expression (identifier usage).
 */
public record VariableReference(SourceLocation location, String variableName) implements Expression {
    public VariableReference {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(variableName, "Variable name cannot be null");
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitVariableReference(this, context);
    }
}
