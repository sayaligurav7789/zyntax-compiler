package minijava.interpreter;

import minijava.ast.Program;
import minijava.ir.IrGenerator;
import minijava.ir.TacProgram;
import minijava.lexer.Lexer;
import minijava.optimizer.OptimizationPipeline;
import minijava.optimizer.OptimizationReport;
import minijava.parser.Parser;
import minijava.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private Program parseAndCheck(String source) {
        Program program = new Parser(new Lexer(source).tokenizeAll()).parse();
        new SemanticAnalyzer().analyze(program);
        return program;
    }

    @Test
    @DisplayName("Execute program via AST Interpreter")
    void testAstInterpreter() {
        Program program = parseAndCheck("""
                int a = 15;
                int b = 3;
                print(a / b);
                print(a % b);
                """);

        AstInterpreter interpreter = new AstInterpreter();
        ExecutionResult result = interpreter.execute(program);

        assertEquals(List.of("5", "0"), result.outputLines());
        assertEquals(15, result.environment().get("a"));
        assertEquals(3, result.environment().get("b"));
    }

    @Test
    @DisplayName("Execute program via TAC Interpreter")
    void testTacInterpreter() {
        Program program = parseAndCheck("""
                int a = 10;
                int b = 20;
                int c = a + b;
                print(c);
                """);

        TacProgram tac = new IrGenerator().generate(program);
        TacInterpreter interpreter = new TacInterpreter();
        ExecutionResult result = interpreter.execute(tac);

        assertEquals(List.of("30"), result.outputLines());
    }

    @Test
    @DisplayName("Verify semantic equivalence across AST, unoptimized TAC, and optimized TAC")
    void testEquivalenceVerification() {
        String source = """
                int x = 7;
                int y = 8;
                int z = (x + 3) * (y - 2);
                print(z);
                """;

        Program ast = parseAndCheck(source);
        TacProgram unoptTac = new IrGenerator().generate(ast);
        OptimizationReport report = OptimizationPipeline.createDefault().optimize(unoptTac);
        TacProgram optTac = report.optimizedProgram();

        SemanticEquivalenceVerifier verifier = new SemanticEquivalenceVerifier();
        SemanticEquivalenceVerifier.VerificationReport vReport = verifier.verify(ast, unoptTac, optTac);

        assertTrue(vReport.isEquivalent(), "All execution layers must produce strictly equivalent results");
        assertEquals(List.of("60"), vReport.optimizedTacResult().outputLines());
    }

    @Test
    @DisplayName("Verify that runtime division by zero exception is preserved across all engines")
    void testDivisionByZeroRuntimeEquivalence() {
        String source = """
                int a = 0;
                int b = 42 / a;
                print(b);
                """;

        Program ast = parseAndCheck(source);
        TacProgram unoptTac = new IrGenerator().generate(ast);
        OptimizationReport report = OptimizationPipeline.createDefault().optimize(unoptTac);
        TacProgram optTac = report.optimizedProgram();

        SemanticEquivalenceVerifier verifier = new SemanticEquivalenceVerifier();
        SemanticEquivalenceVerifier.VerificationReport vReport = verifier.verify(ast, unoptTac, optTac);

        assertTrue(vReport.isEquivalent(), "Division by zero exception must be identically preserved across AST and TAC");
    }
}
