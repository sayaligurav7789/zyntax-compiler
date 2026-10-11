package minijava.interpreter;

import minijava.ast.*;

import java.util.*;

/**
 * Direct tree-walking interpreter for the MiniJava AST.
 * Serves as the ground-truth reference implementation of program semantics.
 */
public class AstInterpreter implements AstVisitor<Integer, Void> {
    private final Map<String, Integer> environment = new LinkedHashMap<>();
    private final List<String> outputLines = new ArrayList<>();
    private int statementCount = 0;

    public ExecutionResult execute(Program program) {
        environment.clear();
        outputLines.clear();
        statementCount = 0;

        long startTime = System.nanoTime();
        for (Statement stmt : program.statements()) {
            statementCount++;
            stmt.accept(this, null);
        }
        long duration = System.nanoTime() - startTime;

        return new ExecutionResult(outputLines, environment, statementCount, duration);
    }

    @Override
    public Integer visitProgram(Program program, Void context) {
        for (Statement stmt : program.statements()) {
            stmt.accept(this, context);
        }
        return null;
    }

    @Override
    public Integer visitVarDecl(VarDeclStatement statement, Void context) {
        int value = 0;
        if (statement.hasInitializer()) {
            value = statement.initializer().accept(this, context);
        }
        environment.put(statement.variableName(), value);
        return value;
    }

    @Override
    public Integer visitAssign(AssignStatement statement, Void context) {
        int value = statement.expression().accept(this, context);
        environment.put(statement.variableName(), value);
        return value;
    }

    @Override
    public Integer visitPrint(PrintStatement statement, Void context) {
        int value = statement.expression().accept(this, context);
        outputLines.add(String.valueOf(value));
        return value;
    }

    @Override
    public Integer visitIntegerLiteral(IntegerLiteral expression, Void context) {
        return expression.value();
    }

    @Override
    public Integer visitVariableReference(VariableReference expression, Void context) {
        String name = expression.variableName();
        Integer value = environment.get(name);
        if (value == null) {
            throw new RuntimeException("Runtime error: Variable '" + name + "' is undefined in current environment");
        }
        return value;
    }

    @Override
    public Integer visitBinaryExpression(BinaryExpression expression, Void context) {
        int left = expression.left().accept(this, context);
        int right = expression.right().accept(this, context);

        return switch (expression.op()) {
            case ADD -> left + right;
            case SUB -> left - right;
            case MUL -> left * right;
            case DIV -> {
                if (right == 0) {
                    throw new ArithmeticException("Division by zero at [" + expression.location() + "]");
                }
                yield left / right;
            }
            case MOD -> {
                if (right == 0) {
                    throw new ArithmeticException("Modulo by zero at [" + expression.location() + "]");
                }
                yield left % right;
            }
        };
    }

    @Override
    public Integer visitUnaryExpression(UnaryExpression expression, Void context) {
        int operand = expression.operand().accept(this, context);
        return switch (expression.op()) {
            case NEG -> -operand;
            case POS -> operand;
        };
    }
}
