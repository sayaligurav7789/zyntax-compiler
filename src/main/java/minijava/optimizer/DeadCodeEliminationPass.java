package minijava.optimizer;

import minijava.ast.BinaryOp;
import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;

import java.util.*;

/**
 * Dead Code Elimination (DCE) optimization pass:
 * Uses backward liveness analysis to eliminate assignments to variables and
 * temporaries that are never observed or used in subsequent live computations.
 *
 * Observable side effects (such as print statements) are strictly preserved.
 * Optionally preserves potentially faulting instructions (e.g. division by unknown
 * or zero divisors) to strictly maintain runtime exception semantics.
 */
public class DeadCodeEliminationPass implements OptimizationPass {
    private final boolean preservePotentialExceptions;

    public DeadCodeEliminationPass() {
        this(true);
    }

    public DeadCodeEliminationPass(boolean preservePotentialExceptions) {
        this.preservePotentialExceptions = preservePotentialExceptions;
    }

    @Override
    public String name() {
        return "Dead Code Elimination";
    }

    @Override
    public PassResult run(TacProgram program) {
        List<TacInstruction> instructions = program.instructions();
        Set<Operand.Dest> live = new HashSet<>();
        List<TacInstruction> kept = new ArrayList<>();
        List<String> logs = new ArrayList<>();
        int changes = 0;

        // Backward liveness analysis from last instruction to first
        for (int i = instructions.size() - 1; i >= 0; i--) {
            TacInstruction inst = instructions.get(i);

            if (inst instanceof TacInstruction.Print print) {
                // Print is always an observable side effect and live
                kept.add(print);
                addUsesToLive(print, live);
            } else if (inst.destination().isPresent()) {
                Operand.Dest dest = inst.destination().get();

                if (live.contains(dest)) {
                    // Variable/temporary is live
                    kept.add(inst);
                    live.remove(dest); // Definition kills earlier liveness
                    addUsesToLive(inst, live); // Operands become live
                } else {
                    // Destination is dead
                    if (preservePotentialExceptions && canThrowException(inst)) {
                        // Preserved to maintain potential runtime exception side effect
                        kept.add(inst);
                        addUsesToLive(inst, live);
                    } else {
                        // Safely eliminated!
                        changes++;
                        logs.add(String.format("Eliminated dead instruction: '%s'", inst.toTacString()));
                    }
                }
            } else {
                kept.add(inst);
            }
        }

        // Instructions were collected in reverse order
        Collections.reverse(kept);

        return new PassResult(new TacProgram(kept), changes, logs);
    }

    private void addUsesToLive(TacInstruction inst, Set<Operand.Dest> live) {
        for (Operand use : inst.uses()) {
            if (use instanceof Operand.Dest dest) {
                live.add(dest);
            }
        }
    }

    private boolean canThrowException(TacInstruction inst) {
        if (inst instanceof TacInstruction.Binary binary) {
            if (binary.op() == BinaryOp.DIV || binary.op() == BinaryOp.MOD) {
                // If divisor is a non-zero constant, division by zero is impossible in integer arithmetic
                if (binary.right() instanceof Operand.Constant(int val) && val != 0) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
