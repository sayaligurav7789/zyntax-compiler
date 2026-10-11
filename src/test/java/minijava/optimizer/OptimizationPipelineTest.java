package minijava.optimizer;

import minijava.ast.Program;
import minijava.interpreter.ExecutionResult;
import minijava.interpreter.TacInterpreter;
import minijava.ir.IrGenerator;
import minijava.ir.TacProgram;
import minijava.lexer.Lexer;
import minijava.parser.Parser;
import minijava.semantic.SemanticAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OptimizationPipelineTest {

    private TacProgram compileToTac(String source) {
        Program ast = new Parser(new Lexer(source).tokenizeAll()).parse();
        new SemanticAnalyzer().analyze(ast);
        return new IrGenerator().generate(ast);
    }

    @Test
    @DisplayName("Verify user example: int a = 2 + 3; int b = a * 4; print(b); optimizes to output 20")
    void testUserExamplePipeline() {
        String source = """
                int a = 2 + 3;
                int b = a * 4;
                print(b);
                """;

        TacProgram unoptTac = compileToTac(source);
        assertEquals(5, unoptTac.size(), "Unoptimized TAC should contain 5 instructions");

        OptimizationPipeline pipeline = OptimizationPipeline.createDefault();
        OptimizationReport report = pipeline.optimize(unoptTac);

        // Verify significant instruction reduction
        assertTrue(report.finalCount() < report.initialCount(), "Optimized instruction count must be strictly less than unoptimized");
        assertTrue(report.reductionPercentage() >= 60.0, "Should achieve at least 60% reduction");

        // Verify runtime execution of optimized TAC produces exact output 20
        TacInterpreter interpreter = new TacInterpreter();
        ExecutionResult result = interpreter.execute(report.optimizedProgram());

        assertEquals(List.of("20"), result.outputLines(), "Optimized program output must be exactly 20");
    }

    @Test
    @DisplayName("Verify pipeline fixpoint convergence and stability")
    void testPipelineFixpoint() {
        String source = """
                int x = 10 + 20;
                int y = x * 1;
                int z = y - y;
                print(z);
                """;

        TacProgram unopt = compileToTac(source);
        OptimizationPipeline pipeline = OptimizationPipeline.createDefault();
        OptimizationReport report = pipeline.optimize(unopt);

        // Running another round of optimization on already-optimized program must make 0 changes
        OptimizationReport secondPass = pipeline.optimize(report.optimizedProgram());
        assertEquals(0, secondPass.steps().size(), "Second optimization pass should make 0 changes (fixpoint reached)");
        assertEquals(report.optimizedProgram(), secondPass.optimizedProgram());

        // Verify output is 0
        ExecutionResult res = new TacInterpreter().execute(report.optimizedProgram());
        assertEquals(List.of("0"), res.outputLines());
    }

    @Test
    @DisplayName("Verify comprehensive optimization of dead code, constants, and identities")
    void testComprehensiveOptimization() {
        String source = """
                int dead1 = 100 * 200;
                int dead2 = dead1 + 50;
                int live1 = 5;
                int live2 = live1 + 0;
                int live3 = live2 * 3;
                print(live3);
                """;

        TacProgram unopt = compileToTac(source);
        OptimizationPipeline pipeline = OptimizationPipeline.createDefault();
        OptimizationReport report = pipeline.optimize(unopt);

        ExecutionResult unoptResult = new TacInterpreter().execute(unopt);
        ExecutionResult optResult = new TacInterpreter().execute(report.optimizedProgram());

        assertEquals(List.of("15"), unoptResult.outputLines());
        assertEquals(List.of("15"), optResult.outputLines());
        assertTrue(report.finalCount() < report.initialCount());
    }
}
