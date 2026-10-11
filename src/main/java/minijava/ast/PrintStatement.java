package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Objects;

/**
 * Print statement: {@code print(<expr>);}.
 */
public record PrintStatement(SourceLocation location, Expression expression) implements Statement {
    public PrintStatement {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(expression, "Expression cannot be null");
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitPrint(this, context);
    }
}
