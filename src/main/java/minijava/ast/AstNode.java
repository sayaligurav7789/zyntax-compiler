package minijava.ast;

import minijava.lexer.SourceLocation;

/**
 * Root interface for all Abstract Syntax Tree nodes.
 */
public interface AstNode {
    /**
     * Source code location where this node originated.
     */
    SourceLocation location();

    /**
     * Accepts an AST visitor.
     */
    <R, C> R accept(AstVisitor<R, C> visitor, C context);
}
