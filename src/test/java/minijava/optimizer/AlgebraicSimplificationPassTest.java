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

class AlgebraicSimplificationPassTest {

    private final AlgebraicSimplificationPass pass = new AlgebraicSimplificationPass();

    @Test
    @DisplayName("Simplify additions with zero identity (x + 0 and 0 + x)")
    void testAdditionIdentities() {
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Binary(new Operand.Variable("a"), BinaryOp.ADD, new Operand.Variable("x"), new Operand.Constant(0)),
                new TacInstruction.Binary(new Operand.Variable("b"), BinaryOp.ADD, new Operand.Constant(0), new Operand.Variable("y"))
        ));

        PassResult result = pass.run(input);
        assertEquals(2, result.changeCount());

        assertEquals(new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Variable("x")), result.program().get(0));
        assertEquals(new TacInstruction.Assign(new Operand.Variable("b"), new Operand.Variable("y")), result.program().get(1));
    }

    @Test
    @DisplayName("Simplify subtractions (x - 0 -> x, x - x -> 0)")
    void testSubtractionIdentities() {
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Binary(new Operand.Variable("a"), BinaryOp.SUB, new Operand.Variable("x"), new Operand.Constant(0)),
                new TacInstruction.Binary(new Operand.Variable("b"), BinaryOp.SUB, new Operand.Variable("x"), new Operand.Variable("x"))
        ));

        PassResult result = pass.run(input);
        assertEquals(2, result.changeCount());

        assertEquals(new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Variable("x")), result.program().get(0));
        assertEquals(new TacInstruction.Assign(new Operand.Variable("b"), new Operand.Constant(0)), result.program().get(1));
    }

    @Test
    @DisplayName("Simplify multiplications (x * 1, 1 * x, x * 0, 0 * x)")
    void testMultiplicationIdentities() {
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Binary(new Operand.Variable("a"), BinaryOp.MUL, new Operand.Variable("x"), new Operand.Constant(1)),
                new TacInstruction.Binary(new Operand.Variable("b"), BinaryOp.MUL, new Operand.Constant(1), new Operand.Variable("y")),
                new TacInstruction.Binary(new Operand.Variable("c"), BinaryOp.MUL, new Operand.Variable("x"), new Operand.Constant(0)),
                new TacInstruction.Binary(new Operand.Variable("d"), BinaryOp.MUL, new Operand.Constant(0), new Operand.Variable("y"))
        ));

        PassResult result = pass.run(input);
        assertEquals(4, result.changeCount());

        assertEquals(new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Variable("x")), result.program().get(0));
        assertEquals(new TacInstruction.Assign(new Operand.Variable("b"), new Operand.Variable("y")), result.program().get(1));
        assertEquals(new TacInstruction.Assign(new Operand.Variable("c"), new Operand.Constant(0)), result.program().get(2));
        assertEquals(new TacInstruction.Assign(new Operand.Variable("d"), new Operand.Constant(0)), result.program().get(3));
    }

    @Test
    @DisplayName("Simplify division and modulo with 1 (x / 1 -> x, x % 1 -> 0)")
    void testDivisionAndModuloIdentities() {
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Binary(new Operand.Variable("a"), BinaryOp.DIV, new Operand.Variable("x"), new Operand.Constant(1)),
                new TacInstruction.Binary(new Operand.Variable("b"), BinaryOp.MOD, new Operand.Variable("x"), new Operand.Constant(1))
        ));

        PassResult result = pass.run(input);
        assertEquals(2, result.changeCount());

        assertEquals(new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Variable("x")), result.program().get(0));
        assertEquals(new TacInstruction.Assign(new Operand.Variable("b"), new Operand.Constant(0)), result.program().get(1));
    }

    @Test
    @DisplayName("Simplify unary positive operator (+x -> x)")
    void testUnaryPositiveIdentity() {
        TacProgram input = new TacProgram(List.of(
                new TacInstruction.Unary(new Operand.Variable("a"), UnaryOp.POS, new Operand.Variable("x"))
        ));

        PassResult result = pass.run(input);
        assertEquals(1, result.changeCount());
        assertEquals(new TacInstruction.Assign(new Operand.Variable("a"), new Operand.Variable("x")), result.program().get(0));
    }
}
