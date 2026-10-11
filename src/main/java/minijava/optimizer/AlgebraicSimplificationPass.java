package minijava.optimizer;

import minijava.ast.BinaryOp;
import minijava.ast.UnaryOp;
import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;

import java.util.ArrayList;
import java.util.List;

/**
 * Algebraic Simplification optimization pass:
 * Applies safe algebraic identity transformations to eliminate unnecessary operations.
 *
 * Supported identities:
 * <ul>
 *   <li>{@code x + 0} &rarr; {@code x}</li>
 *   <li>{@code 0 + x} &rarr; {@code x}</li>
 *   <li>{@code x - 0} &rarr; {@code x}</li>
 *   <li>{@code x - x} &rarr; {@code 0}</li>
 *   <li>{@code x * 1} &rarr; {@code x}</li>
 *   <li>{@code 1 * x} &rarr; {@code x}</li>
 *   <li>{@code x * 0} &rarr; {@code 0}</li>
 *   <li>{@code 0 * x} &rarr; {@code 0}</li>
 *   <li>{@code x / 1} &rarr; {@code x}</li>
 *   <li>{@code x % 1} &rarr; {@code 0}</li>
 *   <li>{@code +x} &rarr; {@code x}</li>
 * </ul>
 */
public class AlgebraicSimplificationPass implements OptimizationPass {

    @Override
    public String name() {
        return "Algebraic Simplification";
    }

    @Override
    public PassResult run(TacProgram program) {
        List<TacInstruction> optimized = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        int changes = 0;

        for (TacInstruction inst : program.instructions()) {
            if (inst instanceof TacInstruction.Binary binary) {
                TacInstruction simplified = simplifyBinary(binary);
                if (simplified != null) {
                    changes++;
                    logs.add(String.format("Algebraic simplification: '%s' -> '%s'", binary.toTacString(), simplified.toTacString()));
                    optimized.add(simplified);
                    continue;
                }
            } else if (inst instanceof TacInstruction.Unary unary) {
                if (unary.op() == UnaryOp.POS) {
                    TacInstruction simplified = new TacInstruction.Assign(unary.dest(), unary.operand());
                    changes++;
                    logs.add(String.format("Algebraic simplification: '%s' -> '%s'", unary.toTacString(), simplified.toTacString()));
                    optimized.add(simplified);
                    continue;
                }
            }

            optimized.add(inst);
        }

        return new PassResult(new TacProgram(optimized), changes, logs);
    }

    private TacInstruction simplifyBinary(TacInstruction.Binary binary) {
        Operand.Dest dest = binary.dest();
        BinaryOp op = binary.op();
        Operand left = binary.left();
        Operand right = binary.right();

        boolean leftIsZero = isConstant(left, 0);
        boolean rightIsZero = isConstant(right, 0);
        boolean leftIsOne = isConstant(left, 1);
        boolean rightIsOne = isConstant(right, 1);

        switch (op) {
            case ADD -> {
                // x + 0 -> x
                if (rightIsZero) {
                    return new TacInstruction.Assign(dest, left);
                }
                // 0 + x -> x
                if (leftIsZero) {
                    return new TacInstruction.Assign(dest, right);
                }
            }
            case SUB -> {
                // x - 0 -> x
                if (rightIsZero) {
                    return new TacInstruction.Assign(dest, left);
                }
                // x - x -> 0
                if (left.equals(right)) {
                    return new TacInstruction.Assign(dest, new Operand.Constant(0));
                }
            }
            case MUL -> {
                // x * 1 -> x
                if (rightIsOne) {
                    return new TacInstruction.Assign(dest, left);
                }
                // 1 * x -> x
                if (leftIsOne) {
                    return new TacInstruction.Assign(dest, right);
                }
                // x * 0 -> 0
                if (rightIsZero) {
                    return new TacInstruction.Assign(dest, new Operand.Constant(0));
                }
                // 0 * x -> 0
                if (leftIsZero) {
                    return new TacInstruction.Assign(dest, new Operand.Constant(0));
                }
            }
            case DIV -> {
                // x / 1 -> x
                if (rightIsOne) {
                    return new TacInstruction.Assign(dest, left);
                }
            }
            case MOD -> {
                // x % 1 -> 0
                if (rightIsOne) {
                    return new TacInstruction.Assign(dest, new Operand.Constant(0));
                }
            }
        }

        return null;
    }

    private boolean isConstant(Operand operand, int expectedValue) {
        return operand instanceof Operand.Constant(int val) && val == expectedValue;
    }
}
