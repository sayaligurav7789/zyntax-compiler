package minijava.optimizer;

import minijava.ast.BinaryOp;
import minijava.ast.UnaryOp;
import minijava.ir.Operand;
import minijava.ir.TacInstruction;
import minijava.ir.TacProgram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConstantFoldingPassTest {

    private final ConstantFoldingPass pass = new ConstantFoldingPass();

    @Test
    @DisplayName("Fold basic arithmetic operations (add, sub, mul, div, mod)")
    void testBasicFolding() {
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Binary(new Operand.Temporary(0), BinaryOp.ADD, new Operand.Constant(10), new Operand.Constant(20)),
                new TacInstruction.Binary(new Operand.Temporary(1), BinaryOp.SUB, new Operand.Constant(50), new Operand.Constant(8)),
                new TacInstruction.Binary(new Operand.Temporary(2), BinaryOp.MUL, new Operand.Constant(6), new Operand.Constant(7)),
                new TacInstruction.Binary(new Operand.Temporary(3), BinaryOp.DIV, new Operand.Constant(100), new Operand.Constant(4)),
                new TacInstruction.Binary(new Operand.Temporary(4), BinaryOp.MOD, new Operand.Constant(17), new Operand.Constant(5))
        ));

        PassResult result = pass.run(input);
        assertEquals(5, result.changeCount());

        List<TacInstruction> instrs = result.program().instructions();
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(0), new Operand.Constant(30)), instrs.get(0));
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(1), new Operand.Constant(42)), instrs.get(1));
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(2), new Operand.Constant(42)), instrs.get(2));
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(3), new Operand.Constant(25)), instrs.get(3));
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(4), new Operand.Constant(2)), instrs.get(4));
    }

    @Test
    @DisplayName("Preserve division and modulo by zero to maintain runtime exception semantics")
    void testPreserveDivisionByZero() {
        TacInstruction.Binary divZero = new TacInstruction.Binary(
                new Operand.Temporary(0), BinaryOp.DIV, new Operand.Constant(10), new Operand.Constant(0)
        );
        TacInstruction.Binary modZero = new TacInstruction.Binary(
                new Operand.Temporary(1), BinaryOp.MOD, new Operand.Constant(10), new Operand.Constant(0)
        );
        TacProgram input = new TacProgram(List.of(divZero, modZero));

        PassResult result = pass.run(input);
        assertEquals(0, result.changeCount());
        assertEquals(input, result.program());
    }

    @Test
    @DisplayName("Fold unary negation on constant literal")
    void testUnaryFolding() {
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Unary(new Operand.Temporary(0), UnaryOp.NEG, new Operand.Constant(42)),
                new TacInstruction.Unary(new Operand.Temporary(1), UnaryOp.POS, new Operand.Constant(99))
        ));

        PassResult result = pass.run(input);
        assertEquals(2, result.changeCount());

        List<TacInstruction> instrs = result.program().instructions();
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(0), new Operand.Constant(-42)), instrs.get(0));
        assertEquals(new TacInstruction.Assign(new Operand.Temporary(1), new Operand.Constant(99)), instrs.get(1));
    }
}
