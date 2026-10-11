package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Objects;

/**
 * Binary arithmetic expression: {@code <left> <op> <right>}.
 */
public record BinaryExpression(SourceLocation location, BinaryOp op, Expression left, Expression right) implements Expression {
    public BinaryExpression {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(op, "Operator cannot be null");
        Objects.requireNonNull(left, "Left operand cannot be null");
        Objects.requireNonNull(right, "Right operand cannot be null");
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitBinaryExpression(this, context);
    }
}
