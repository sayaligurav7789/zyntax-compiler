package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * Variable declaration statement: {@code int x = <expr>;} or {@code int x;}.
 */
public record VarDeclStatement(SourceLocation location, String variableName, Expression initializer) implements Statement {
    public VarDeclStatement {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(variableName, "Variable name cannot be null");
    }

    public Optional<Expression> getInitializer() {
        return Optional.ofNullable(initializer);
    }

    public boolean hasInitializer() {
        return initializer != null;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitVarDecl(this, context);
    }
}
