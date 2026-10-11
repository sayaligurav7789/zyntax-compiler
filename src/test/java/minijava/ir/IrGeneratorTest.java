package minijava.ir;

import minijava.ast.BinaryOp;
import minijava.ast.Program;
import minijava.lexer.Lexer;
import minijava.parser.Parser;
import minijava.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IrGeneratorTest {

    private TacProgram generateIr(String source) {
        Program program = new Parser(new Lexer(source).tokenizeAll()).parse();
        new SemanticAnalyzer().analyze(program);
        return new IrGenerator().generate(program);
    }

    @Test
    @DisplayName("Generate linear TAC for assignment of binary expression")
    void testBinaryLowering() {
        TacProgram tac = generateIr("int a = 2 + 3;");
        assertEquals(2, tac.size());

        // t0 = 2 + 3
        TacInstruction.Binary bin = assertInstanceOf(TacInstruction.Binary.class, tac.get(0));
        assertEquals(new Operand.Temporary(0), bin.dest());
        assertEquals(BinaryOp.ADD, bin.op());
        assertEquals(new Operand.Constant(2), bin.left());
        assertEquals(new Operand.Constant(3), bin.right());

        // a = t0
        TacInstruction.Assign assign = assertInstanceOf(TacInstruction.Assign.class, tac.get(1));
        assertEquals(new Operand.Variable("a"), assign.dest());
        assertEquals(new Operand.Temporary(0), assign.src());
    }

    @Test
    @DisplayName("Generate TAC for print statement")
    void testPrintLowering() {
        TacProgram tac = generateIr("print(42);");
        assertEquals(1, tac.size());
        TacInstruction.Print print = assertInstanceOf(TacInstruction.Print.class, tac.get(0));
        assertEquals(new Operand.Constant(42), print.operand());
    }

    @Test
    @DisplayName("Generate TAC for nested expressions allocating sequential temporaries")
    void testNestedTemporaries() {
        TacProgram tac = generateIr("int res = (1 + 2) * (3 - 4);");
        // t0 = 1 + 2
        // t1 = 3 - 4
        // t2 = t0 * t1
        // res = t2
        assertEquals(4, tac.size());
        assertEquals(new Operand.Temporary(0), ((TacInstruction.Binary) tac.get(0)).dest());
        assertEquals(new Operand.Temporary(1), ((TacInstruction.Binary) tac.get(1)).dest());
        assertEquals(new Operand.Temporary(2), ((TacInstruction.Binary) tac.get(2)).dest());
        assertEquals(new Operand.Variable("res"), ((TacInstruction.Assign) tac.get(3)).dest());
    }
}
