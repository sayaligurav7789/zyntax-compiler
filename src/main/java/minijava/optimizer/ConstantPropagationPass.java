package minijava.optimizer;

import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constant Propagation optimization pass:
 * Replaces variable and temporary uses with known compile-time constant values
 * discovered through previous assignments in the instruction stream.
 */
public class ConstantPropagationPass implements OptimizationPass {

    @Override
    public String name() {
        return "Constant Propagation";
    }

    @Override
    public PassResult run(TacProgram program) {
        List<TacInstruction> optimized = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        Map<Operand.Dest, Integer> knownConstants = new HashMap<>();
        int changes = 0;

        for (TacInstruction inst : program.instructions()) {
            // 1. Substitute operands with known constants
            TacInstruction substituted = inst.replaceUses(operand -> {
                if (operand instanceof Operand.Dest dest && knownConstants.containsKey(dest)) {
                    return new Operand.Constant(knownConstants.get(dest));
                }
                return operand;
            });

            if (!substituted.equals(inst)) {
                changes++;
                logs.add(String.format("Propagated constant: '%s' -> '%s'", inst.toTacString(), substituted.toTacString()));
            }

            // 2. Update known constants map based on the resulting instruction
            if (substituted instanceof TacInstruction.Assign assign && assign.src() instanceof Operand.Constant(int val)) {
                knownConstants.put(assign.dest(), val);
            } else if (substituted.destination().isPresent()) {
                knownConstants.remove(substituted.destination().get());
            }

            optimized.add(substituted);
        }

        return new PassResult(new TacProgram(optimized), changes, logs);
    }
}
