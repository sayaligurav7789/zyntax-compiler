package minijava.semantic;

import minijava.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Performs semantic analysis on the MiniJava AST:
 * 1. Tracks identifier declarations in a symbol table.
 * 2. Detects duplicate variable declarations.
 * 3. Detects use of undeclared variables.
 * 4. Detects reads from uninitialized variables.
 * 5. Optionally flags static division-by-zero diagnostics.
 */
public class SemanticAnalyzer implements AstVisitor<Void, Void> {
    private final SymbolTable symbolTable = new SymbolTable();
    private final List<SemanticError> errors = new ArrayList<>();
    private final boolean rejectStaticDivisionByZero;

    public SemanticAnalyzer() {
        this(false);
    }

    public SemanticAnalyzer(boolean rejectStaticDivisionByZero) {
        this.rejectStaticDivisionByZero = rejectStaticDivisionByZero;
    }

    /**
     * Analyzes the given program and returns the populated symbol table if valid.
     *
     * @throws SemanticException if any semantic errors are found.
     */
    public SymbolTable analyze(Program program) {
        errors.clear();
        program.accept(this, null);

        if (!errors.isEmpty()) {
            throw new SemanticException(errors);
        }
        return symbolTable;
    }

    public List<SemanticError> getErrors() {
        return errors;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    @Override
    public Void visitProgram(Program program, Void context) {
        for (Statement stmt : program.statements()) {
            stmt.accept(this, context);
        }
        return null;
    }

    @Override
    public Void visitVarDecl(VarDeclStatement statement, Void context) {
        String name = statement.variableName();

        if (statement.hasInitializer()) {
            // First analyze the initializer expression before declaring the variable
            // to prevent recursive references like: int a = a + 1;
            statement.initializer().accept(this, context);
        }

        boolean declared = symbolTable.declare(name, statement.location(), statement.hasInitializer());
        if (!declared) {
            Symbol existing = symbolTable.lookup(name).orElseThrow();
            errors.add(new SemanticError(
                    statement.location(),
                    String.format("Variable '%s' is already declared at [%s]", name, existing.getDeclaredLocation())
            ));
        }

        return null;
    }

    @Override
    public Void visitAssign(AssignStatement statement, Void context) {
        String name = statement.variableName();
        Optional<Symbol> symbolOpt = symbolTable.lookup(name);

        if (symbolOpt.isEmpty()) {
            errors.add(new SemanticError(
                    statement.location(),
                    String.format("Cannot assign to undeclared variable '%s'", name)
            ));
        }

        // Analyze expression
        statement.expression().accept(this, context);

        // Mark variable as initialized after assignment
        symbolTable.markInitialized(name);
        return null;
    }

    @Override
    public Void visitPrint(PrintStatement statement, Void context) {
        statement.expression().accept(this, context);
        return null;
    }

    @Override
    public Void visitIntegerLiteral(IntegerLiteral expression, Void context) {
        // Literals are always semantically valid
        return null;
    }

    @Override
    public Void visitVariableReference(VariableReference expression, Void context) {
        String name = expression.variableName();
        Optional<Symbol> symbolOpt = symbolTable.lookup(name);

        if (symbolOpt.isEmpty()) {
            errors.add(new SemanticError(
                    expression.location(),
                    String.format("Undeclared variable '%s' referenced in expression", name)
            ));
        } else {
            Symbol symbol = symbolOpt.get();
            if (!symbol.isInitialized()) {
                errors.add(new SemanticError(
                        expression.location(),
                        String.format("Variable '%s' may not have been initialized before use", name)
                ));
            }
        }
        return null;
    }

    @Override
    public Void visitBinaryExpression(BinaryExpression expression, Void context) {
        expression.left().accept(this, context);
        expression.right().accept(this, context);

        if (rejectStaticDivisionByZero && (expression.op() == BinaryOp.DIV || expression.op() == BinaryOp.MOD)) {
            if (expression.right() instanceof IntegerLiteral lit && lit.value() == 0) {
                errors.add(new SemanticError(
                        expression.location(),
                        "Static division or modulo by zero"
                ));
            }
        }
        return null;
    }

    @Override
    public Void visitUnaryExpression(UnaryExpression expression, Void context) {
        expression.operand().accept(this, context);
        return null;
    }
}
