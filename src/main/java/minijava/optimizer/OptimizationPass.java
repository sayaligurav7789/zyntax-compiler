package minijava.optimizer;

import minijava.ir.TacProgram;

/**
 * Interface implemented by compiler optimization passes.
 */
public interface OptimizationPass {
    /**
     * Unique descriptive name of the optimization pass.
     */
    String name();

    /**
     * Executes the pass on the provided TAC program.
     *
     * @param program input Three-Address Code program
     * @return result containing transformed program and metrics
     */
    PassResult run(TacProgram program);
}
