package minijava.optimizer;

import minijava.ast.BinaryOp;
import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstantPropagationPassTest {

    private final ConstantPropagationPass pass = new ConstantPropagationPass();

    @Test
    @DisplayName("Propagate constant assignment forward into binary and print instructions")
    void testForwardPropagation() {
        // a = 5
        // b = a + 2
        // print a
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Constant(5)),
                new TacInstruction.Binary(new Operand.Variable("b"), BinaryOp.ADD, new Operand.Variable("a"), new Operand.Constant(2)),
                new TacInstruction.Print(new Operand.Variable("a"))
        ));

        PassResult result = pass.run(input);
        assertEquals(2, result.changeCount());

        List<TacInstruction> instrs = result.program().instructions();
        // b = 5 + 2
        TacInstruction.Binary bInst = assertInstanceOf(TacInstruction.Binary.class, instrs.get(1));
        assertEquals(new Operand.Constant(5), bInst.left());

        // print 5
        TacInstruction.Print printInst = assertInstanceOf(TacInstruction.Print.class, instrs.get(2));
        assertEquals(new Operand.Constant(5), printInst.operand());
    }

    @Test
    @DisplayName("Invalidate constant when destination variable is reassigned to unknown value")
    void testInvalidateOnReassignment() {
        // a = 5
        // a = unknown_var + 1
        // print a
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Constant(5)),
                new TacInstruction.Binary(new Operand.Variable("a"), BinaryOp.ADD, new Operand.Variable("u"), new Operand.Constant(1)),
                new TacInstruction.Print(new Operand.Variable("a"))
        ));

        PassResult result = pass.run(input);
        // Only the first instruction defines a=5, but print a is after a is reassigned to unknown!
        // So print a should NOT be replaced by 5.
        TacInstruction.Print printInst = assertInstanceOf(TacInstruction.Print.class, result.program().get(2));
        assertEquals(new Operand.Variable("a"), printInst.operand());
    }
}
