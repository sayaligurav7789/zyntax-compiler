package minijava.ast;

/**
 * Visitor interface for traversing the MiniJava Abstract Syntax Tree.
 *
 * @param <R> Return type of the visitor operations.
 * @param <C> Context type passed along during AST traversal.
 */
public interface AstVisitor<R, C> {
    R visitProgram(Program program, C context);

    R visitVarDecl(VarDeclStatement statement, C context);

    R visitAssign(AssignStatement statement, C context);

    R visitPrint(PrintStatement statement, C context);

    R visitIntegerLiteral(IntegerLiteral expression, C context);

    R visitVariableReference(VariableReference expression, C context);

    R visitBinaryExpression(BinaryExpression expression, C context);

    R visitUnaryExpression(UnaryExpression expression, C context);
}
