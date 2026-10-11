package minijava.optimizer;

import minijava.ast.BinaryOp;
import minijava.ast.UnaryOp;
import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * Constant Folding optimization pass:
 * Computes compile-time constant arithmetic expressions and replaces them with
 * simple constant assignments.
 *
 * Safety guarantee: Division or remainder by zero is NOT folded, preserving
 * the exact runtime ArithmeticException semantics of the original program.
 */
public class ConstantFoldingPass implements OptimizationPass {

    @Override
    public String name() {
        return "Constant Folding";
    }

    @Override
    public PassResult run(TacProgram program) {
        List<TacInstruction> optimized = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        int changes = 0;

        for (TacInstruction inst : program.instructions()) {
            if (inst instanceof TacInstruction.Binary binary) {
                if (binary.left() instanceof Operand.Constant(int c1) &&
                    binary.right() instanceof Operand.Constant(int c2)) {

                    if (canFoldBinary(binary.op(), c2)) {
                        int result = evaluateBinary(binary.op(), c1, c2);
                        TacInstruction folded = new TacInstruction.Assign(binary.dest(), new Operand.Constant(result));
                        optimized.add(folded);
                        changes++;
                        logs.add(String.format("Folded binary expression: '%s' -> '%s'", binary.toTacString(), folded.toTacString()));
                        continue;
                    }
                }
            } else if (inst instanceof TacInstruction.Unary unary) {
                if (unary.operand() instanceof Operand.Constant(int c)) {
                    int result = evaluateUnary(unary.op(), c);
                    TacInstruction folded = new TacInstruction.Assign(unary.dest(), new Operand.Constant(result));
                    optimized.add(folded);
                    changes++;
                    logs.add(String.format("Folded unary expression: '%s' -> '%s'", unary.toTacString(), folded.toTacString()));
                    continue;
                }
            }

            optimized.add(inst);
        }

        return new PassResult(new TacProgram(optimized), changes, logs);
    }

    private boolean canFoldBinary(BinaryOp op, int rightValue) {
        if ((op == BinaryOp.DIV || op == BinaryOp.MOD) && rightValue == 0) {
            // Cannot fold division/modulo by zero: preserve runtime exception
            return false;
        }
        return true;
    }

    private int evaluateBinary(BinaryOp op, int left, int right) {
        return switch (op) {
            case ADD -> left + right;
            case SUB -> left - right;
            case MUL -> left * right;
            case DIV -> left / right;
            case MOD -> left % right;
        };
    }

    private int evaluateUnary(UnaryOp op, int value) {
        return switch (op) {
            case NEG -> -value;
            case POS -> value;
        };
    }
}
