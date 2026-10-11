package minijava.interpreter;

import minijava.ast.Program;
import minijava.ir.TacProgram;

import java.util.Objects;

/**
 * Verifier that formally compares the execution outputs of:
 * 1. Ground-truth AST interpretation
 * 2. Unoptimized Three-Address Code interpretation
 * 3. Optimized Three-Address Code interpretation
 *
 * Guarantees that optimization transformations strictly preserve observable
 * program semantics and exception behavior.
 */
public class SemanticEquivalenceVerifier {
    private final AstInterpreter astInterpreter = new AstInterpreter();
    private final TacInterpreter tacInterpreter = new TacInterpreter();

    public record VerificationReport(
            boolean isEquivalent,
            ExecutionResult astResult,
            ExecutionResult unoptimizedTacResult,
            ExecutionResult optimizedTacResult,
            String message
    ) {
        public String formatSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Semantic Equivalence Verification ===\n");
            sb.append("Status: ").append(isEquivalent ? "PASSED (Observable behavior strictly preserved)" : "FAILED (Semantic divergence detected)").append("\n");
            sb.append("Details: ").append(message).append("\n");
            if (unoptimizedTacResult != null && optimizedTacResult != null) {
                sb.append(String.format("Original TAC steps: %d | Optimized TAC steps: %d (Delta: %d steps)\n",
                        unoptimizedTacResult.stepsExecuted(),
                        optimizedTacResult.stepsExecuted(),
                        unoptimizedTacResult.stepsExecuted() - optimizedTacResult.stepsExecuted()
                ));
            }
            if (optimizedTacResult != null && !optimizedTacResult.outputLines().isEmpty()) {
                sb.append("Standard output produced:\n");
                for (String line : optimizedTacResult.outputLines()) {
                    sb.append("  > ").append(line).append("\n");
                }
            }
            return sb.toString();
        }
    }

    public VerificationReport verify(Program ast, TacProgram unoptimizedTac, TacProgram optimizedTac) {
        Objects.requireNonNull(ast, "AST program cannot be null");
        Objects.requireNonNull(unoptimizedTac, "Unoptimized TAC cannot be null");
        Objects.requireNonNull(optimizedTac, "Optimized TAC cannot be null");

        ExecutionResult astResult;
        ExecutionResult unoptResult;
        ExecutionResult optResult;

        try {
            astResult = astInterpreter.execute(ast);
        } catch (Exception e) {
            return verifyExceptionEquivalence(e, unoptimizedTac, optimizedTac);
        }

        try {
            unoptResult = tacInterpreter.execute(unoptimizedTac);
        } catch (Exception e) {
            return new VerificationReport(false, astResult, null, null,
                    "AST executed successfully, but unoptimized TAC threw: " + e.getMessage());
        }

        try {
            optResult = tacInterpreter.execute(optimizedTac);
        } catch (Exception e) {
            return new VerificationReport(false, astResult, unoptResult, null,
                    "Unoptimized TAC executed successfully, but optimized TAC threw: " + e.getMessage());
        }

        if (!astResult.hasMatchingOutput(unoptResult)) {
            return new VerificationReport(false, astResult, unoptResult, optResult,
                    String.format("Output divergence between AST (%s) and unoptimized TAC (%s)",
                            astResult.outputLines(), unoptResult.outputLines()));
        }

        if (!unoptResult.hasMatchingOutput(optResult)) {
            return new VerificationReport(false, astResult, unoptResult, optResult,
                    String.format("Output divergence between unoptimized TAC (%s) and optimized TAC (%s)",
                            unoptResult.outputLines(), optResult.outputLines()));
        }

        return new VerificationReport(true, astResult, unoptResult, optResult,
                "All outputs match identically across AST, unoptimized TAC, and optimized TAC.");
    }

    private VerificationReport verifyExceptionEquivalence(Exception expectedException, TacProgram unopt, TacProgram opt) {
        Class<?> expectedClass = expectedException.getClass();

        // Check unoptimized TAC
        try {
            tacInterpreter.execute(unopt);
            return new VerificationReport(false, null, null, null,
                    "AST threw " + expectedClass.getSimpleName() + ", but unoptimized TAC succeeded without error");
        } catch (Exception e) {
            if (!expectedClass.isInstance(e)) {
                return new VerificationReport(false, null, null, null,
                        "AST threw " + expectedClass.getSimpleName() + ", but unoptimized TAC threw " + e.getClass().getSimpleName());
            }
        }

        // Check optimized TAC
        try {
            tacInterpreter.execute(opt);
            return new VerificationReport(false, null, null, null,
                    "AST threw " + expectedClass.getSimpleName() + ", but optimized TAC succeeded without error");
        } catch (Exception e) {
            if (!expectedClass.isInstance(e)) {
                return new VerificationReport(false, null, null, null,
                        "AST threw " + expectedClass.getSimpleName() + ", but optimized TAC threw " + e.getClass().getSimpleName());
            }
        }

        return new VerificationReport(true, null, null, null,
                "Identical runtime exception (" + expectedClass.getSimpleName() + ") preserved across all execution engines.");
    }
}
