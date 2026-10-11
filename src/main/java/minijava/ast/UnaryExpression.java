package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Objects;

/**
 * Unary arithmetic expression: {@code -<expr>} or {@code +<expr>}.
 */
public record UnaryExpression(SourceLocation location, UnaryOp op, Expression operand) implements Expression {
    public UnaryExpression {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(op, "Operator cannot be null");
        Objects.requireNonNull(operand, "Operand cannot be null");
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitUnaryExpression(this, context);
    }
}
