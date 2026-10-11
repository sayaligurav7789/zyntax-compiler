package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Objects;

/**
 * Assignment statement: {@code x = <expr>;}.
 */
public record AssignStatement(SourceLocation location, String variableName, Expression expression) implements Statement {
    public AssignStatement {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(variableName, "Variable name cannot be null");
        Objects.requireNonNull(expression, "Expression cannot be null");
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitAssign(this, context);
    }
}
