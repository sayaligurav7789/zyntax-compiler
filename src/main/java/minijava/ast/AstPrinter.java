package minijava.ast;

import java.util.List;

/**
 * Pretty-printer for the MiniJava Abstract Syntax Tree.
 * Generates an ASCII tree structure showing hierarchy, types, operations, and source locations.
 */
public class AstPrinter implements AstVisitor<String, String> {

    public String print(AstNode node) {
        return node.accept(this, "");
    }

    @Override
    public String visitProgram(Program program, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Program [").append(program.location()).append("]\n");
        List<Statement> stmts = program.statements();
        for (int i = 0; i < stmts.size(); i++) {
            boolean isLast = (i == stmts.size() - 1);
            String branch = isLast ? "\\-- " : "+-- ";
            String childIndent = indent + (isLast ? "    " : "|   ");
            sb.append(indent).append(branch).append(stmts.get(i).accept(this, childIndent).trim()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String visitVarDecl(VarDeclStatement statement, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("VarDecl: int ").append(statement.variableName()).append(" [").append(statement.location()).append("]");
        if (statement.hasInitializer()) {
            sb.append("\n").append(indent).append("\\-- init: ")
              .append(statement.initializer().accept(this, indent + "    ").trim());
        }
        return sb.toString();
    }

    @Override
    public String visitAssign(AssignStatement statement, String indent) {
        return "Assign: " + statement.variableName() + " = [" + statement.location() + "]\n" +
                indent + "\\-- value: " + statement.expression().accept(this, indent + "    ").trim();
    }

    @Override
    public String visitPrint(PrintStatement statement, String indent) {
        return "Print [" + statement.location() + "]\n" +
                indent + "\\-- expr: " + statement.expression().accept(this, indent + "    ").trim();
    }

    @Override
    public String visitIntegerLiteral(IntegerLiteral expression, String indent) {
        return "IntLiteral(" + expression.value() + ") [" + expression.location() + "]";
    }

    @Override
    public String visitVariableReference(VariableReference expression, String indent) {
        return "VarRef(" + expression.variableName() + ") [" + expression.location() + "]";
    }

    @Override
    public String visitBinaryExpression(BinaryExpression expression, String indent) {
        return "Binary(" + expression.op() + ") [" + expression.location() + "]\n" +
                indent + "+-- left: " + expression.left().accept(this, indent + "|   ").trim() + "\n" +
                indent + "\\-- right: " + expression.right().accept(this, indent + "    ").trim();
    }

    @Override
    public String visitUnaryExpression(UnaryExpression expression, String indent) {
        return "Unary(" + expression.op() + ") [" + expression.location() + "]\n" +
                indent + "\\-- operand: " + expression.operand().accept(this, indent + "    ").trim();
    }
}
