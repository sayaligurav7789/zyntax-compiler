package minijava.ir;

import minijava.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lowers MiniJava AST into Three-Address Code (TAC).
 */
public class IrGenerator implements AstVisitor<Operand, Void> {
    private final TempGenerator tempGenerator;
    private final List<TacInstruction> emittedInstructions = new ArrayList<>();

    public IrGenerator() {
        this(new TempGenerator());
    }

    public IrGenerator(TempGenerator tempGenerator) {
        this.tempGenerator = Objects.requireNonNull(tempGenerator, "TempGenerator cannot be null");
    }

    /**
     * Translates an AST Program into a TacProgram.
     */
    public TacProgram generate(Program program) {
        emittedInstructions.clear();
        tempGenerator.reset();

        for (Statement stmt : program.statements()) {
            stmt.accept(this, null);
        }

        return new TacProgram(emittedInstructions);
    }

    @Override
    public Operand visitProgram(Program program, Void context) {
        for (Statement stmt : program.statements()) {
            stmt.accept(this, context);
        }
        return null;
    }

    @Override
    public Operand visitVarDecl(VarDeclStatement statement, Void context) {
        if (statement.hasInitializer()) {
            Operand value = statement.initializer().accept(this, context);
            emittedInstructions.add(new TacInstruction.Assign(new Operand.Variable(statement.variableName()), value));
        }
        return null;
    }

    @Override
    public Operand visitAssign(AssignStatement statement, Void context) {
        Operand value = statement.expression().accept(this, context);
        emittedInstructions.add(new TacInstruction.Assign(new Operand.Variable(statement.variableName()), value));
        return null;
    }

    @Override
    public Operand visitPrint(PrintStatement statement, Void context) {
        Operand value = statement.expression().accept(this, context);
        emittedInstructions.add(new TacInstruction.Print(value));
        return null;
    }

    @Override
    public Operand visitIntegerLiteral(IntegerLiteral expression, Void context) {
        return new Operand.Constant(expression.value());
    }

    @Override
    public Operand visitVariableReference(VariableReference expression, Void context) {
        return new Operand.Variable(expression.variableName());
    }

    @Override
    public Operand visitBinaryExpression(BinaryExpression expression, Void context) {
        Operand left = expression.left().accept(this, context);
        Operand right = expression.right().accept(this, context);
        Operand.Temporary temp = tempGenerator.nextTemp();
        emittedInstructions.add(new TacInstruction.Binary(temp, expression.op(), left, right));
        return temp;
    }

    @Override
    public Operand visitUnaryExpression(UnaryExpression expression, Void context) {
        Operand operand = expression.operand().accept(this, context);
        Operand.Temporary temp = tempGenerator.nextTemp();
        emittedInstructions.add(new TacInstruction.Unary(temp, expression.op(), operand));
        return temp;
    }
}
