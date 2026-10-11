package minijava.optimizer;

import minijava.ast.BinaryOp;
import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeadCodeEliminationPassTest {

    private final DeadCodeEliminationPass pass = new DeadCodeEliminationPass(true);

    @Test
    @DisplayName("Eliminate dead assignments that do not affect any print statement")
    void testEliminateDeadVariables() {
        // a = 10 (dead)
        // b = 20 (live)
        // print b
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Constant(10)),
                new TacInstruction.Assign(new Operand.Variable("b"), new Operand.Constant(20)),
                new TacInstruction.Print(new Operand.Variable("b"))
        ));

        PassResult result = pass.run(input);
        assertEquals(1, result.changeCount());
        assertEquals(2, result.program().size());

        assertEquals(new TacInstruction.Assign(new Operand.Variable("b"), new Operand.Constant(20)), result.program().get(0));
        assertEquals(new TacInstruction.Print(new Operand.Variable("b")), result.program().get(1));
    }

    @Test
    @DisplayName("Eliminate chained dead temporaries while preserving live computation")
    void testChainedDeadTemporaries() {
        // t0 = 1 + 2 (dead)
        // t1 = t0 * 3 (dead)
        // t2 = 100    (live)
        // print t2
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Binary(new Operand.Temporary(0), BinaryOp.ADD, new Operand.Constant(1), new Operand.Constant(2)),
                new TacInstruction.Binary(new Operand.Temporary(1), BinaryOp.MUL, new Operand.Temporary(0), new Operand.Constant(3)),
                new TacInstruction.Assign(new Operand.Temporary(2), new Operand.Constant(100)),
                new TacInstruction.Print(new Operand.Temporary(2))
        ));

        PassResult result = pass.run(input);
        assertEquals(2, result.changeCount());
        assertEquals(2, result.program().size());
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(2), new Operand.Constant(100)), result.program().get(0));
        assertEquals(new TacInstruction.Print(new Operand.Temporary(2)), result.program().get(1));
    }

    @Test
    @DisplayName("Preserve division by zero instruction even if destination is dead when exception preservation is enabled")
    void testPreservePotentialException() {
        // t0 = 10 / 0 (dead target, but faulting side-effect)
        // print 42
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Binary(new Operand.Temporary(0), BinaryOp.DIV, new Operand.Constant(10), new Operand.Constant(0)),
                new TacInstruction.Print(new Operand.Constant(42))
        ));

        PassResult result = pass.run(input);
        assertEquals(0, result.changeCount());
        assertEquals(2, result.program().size());
    }
}
