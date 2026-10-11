package minijava.ast;

import minijava.lexer.SourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Root node representing an entire MiniJava program.
 */
public record Program(SourceLocation location, List<Statement> statements) implements AstNode {
    public Program {
        Objects.requireNonNull(location, "Location cannot be null");
        Objects.requireNonNull(statements, "Statements cannot be null");
        statements = Collections.unmodifiableList(statements);
    }

    public Program(List<Statement> statements) {
        this(statements.isEmpty() ? SourceLocation.UNKNOWN : statements.getFirst().location(), statements);
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context) {
        return visitor.visitProgram(this, context);
    }
}
