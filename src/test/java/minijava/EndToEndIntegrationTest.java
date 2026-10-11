package minijava;

import minijava.ast.Program;
import minijava.interpreter.ExecutionResult;
import minijava.interpreter.SemanticEquivalenceVerifier;
import minijava.interpreter.TacInterpreter;
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

class EndToEndIntegrationTest {

    private ExecutionResult runPipeline(String source) {
        Program ast = new Parser(new Lexer(source).tokenizeAll()).parse();
        new SemanticAnalyzer().analyze(ast);
        TacProgram unopt = new IrGenerator().generate(ast);
        OptimizationReport report = OptimizationPipeline.createDefault().optimize(unopt);
        return new TacInterpreter().execute(report.optimizedProgram());
    }

    @Test
    @DisplayName("End-to-End: Complex arithmetic expression pipeline")
    void testComplexArithmetic() {
        String source = """
                int a = 10;
                int b = 20;
                int c = 30;
                int result = (a + b) * c - (b / 2);
                print(result);
                """;

        ExecutionResult result = runPipeline(source);
        // (10 + 20) * 30 - (20 / 2) = 30 * 30 - 10 = 900 - 10 = 890
        assertEquals(List.of("890"), result.outputLines());
    }

    @Test
    @DisplayName("End-to-End: Multiple print statements with variable reuse")
    void testMultiplePrintsWithReuse() {
        String source = """
                int x = 5;
                print(x);
                x = x * 2;
                print(x);
                x = x + 10;
                print(x);
                """;

        ExecutionResult result = runPipeline(source);
        assertEquals(List.of("5", "10", "20"), result.outputLines());
    }

    @Test
    @DisplayName("End-to-End: Equivalence preservation across large pipeline")
    void testEquivalenceAcrossPipeline() {
        String source = """
                int p = 4;
                int q = p * 2;
                int r = q + 0;
                int s = r - r;
                int dead = 999 * 888;
                print(q);
                print(s);
                """;

        Program ast = new Parser(new Lexer(source).tokenizeAll()).parse();
        new SemanticAnalyzer().analyze(ast);
        TacProgram unopt = new IrGenerator().generate(ast);
        OptimizationReport report = OptimizationPipeline.createDefault().optimize(unopt);

        SemanticEquivalenceVerifier verifier = new SemanticEquivalenceVerifier();
        SemanticEquivalenceVerifier.VerificationReport vReport = verifier.verify(ast, unopt, report.optimizedProgram());

        assertTrue(vReport.isEquivalent());
        assertEquals(List.of("8", "0"), vReport.optimizedTacResult().outputLines());
    }
}
