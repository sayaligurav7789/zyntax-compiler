package minijava.cli;

import minijava.ast.AstPrinter;
import minijava.ast.Program;
import minijava.interpreter.AstInterpreter;
import minijava.interpreter.ExecutionResult;
import minijava.interpreter.SemanticEquivalenceVerifier;
import minijava.interpreter.TacInterpreter;
import minijava.ir.IrGenerator;
import minijava.ir.TacProgram;
import minijava.lexer.Lexer;
import minijava.lexer.Token;
import minijava.optimizer.*;
import minijava.parser.Parser;
import minijava.semantic.SemanticAnalyzer;
import minijava.semantic.SymbolTable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Command-Line Interface for the MiniJava Optimizer learning system.
 */
public class MiniJavaCli {

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();
        try {
            switch (command) {
                case "tokenize" -> handleTokenize(args);
                case "parse", "ast" -> handleParse(args);
                case "check" -> handleCheck(args);
                case "ir" -> handleIr(args);
                case "optimize" -> handleOptimize(args);
                case "run" -> handleRun(args);
                case "compare" -> handleCompare(args);
                case "repl" -> handleRepl();
                case "help", "--help", "-h" -> printHelp();
                default -> {
                    System.err.println("Unknown command: '" + command + "'");
                    printHelp();
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleTokenize(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: minijava tokenize <source-file>");
            return;
        }
        String source = readFile(args[1]);
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenizeAll();

        System.out.printf("%-18s %-16s %-12s %s\n", "TOKEN TYPE", "LEXEME", "LITERAL", "LOCATION");
        System.out.println("=".repeat(60));
        for (Token t : tokens) {
            String lit = t.literalValue() != null ? String.valueOf(t.literalValue()) : "-";
            System.out.printf("%-18s %-16s %-12s %s\n", t.type().name(), "'" + t.lexeme() + "'", lit, t.location());
        }
    }

    private static void handleParse(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: minijava parse <source-file>");
            return;
        }
        String source = readFile(args[1]);
        Program program = parseSource(source);
        AstPrinter printer = new AstPrinter();
        System.out.println(printer.print(program));
    }

    private static void handleCheck(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: minijava check <source-file>");
            return;
        }
        String source = readFile(args[1]);
        Program program = parseSource(source);
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        SymbolTable symbolTable = analyzer.analyze(program);

        System.out.println("Semantic Analysis: PASSED");
        System.out.println("Declared Symbols:");
        symbolTable.getSymbols().forEach(s ->
                System.out.printf("  * %s (declared at %s, initialized: %s)\n", s.getName(), s.getDeclaredLocation(), s.isInitialized())
        );
    }

    private static void handleIr(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: minijava ir <source-file>");
            return;
        }
        String source = readFile(args[1]);
        Program program = parseSource(source);
        new SemanticAnalyzer().analyze(program);
        TacProgram tac = new IrGenerator().generate(program);

        System.out.println("=== Generated Three-Address Code (TAC) ===");
        System.out.print(tac.toPrettyString());
    }

    private static void handleOptimize(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: minijava optimize <source-file> [--pass=cf|cp|as|dce|all] [--steps]");
            return;
        }
        String source = readFile(args[1]);
        Program program = parseSource(source);
        new SemanticAnalyzer().analyze(program);
        TacProgram tac = new IrGenerator().generate(program);

        String passFilter = "all";
        boolean showSteps = false;

        for (int i = 2; i < args.length; i++) {
            if (args[i].startsWith("--pass=")) {
                passFilter = args[i].substring("--pass=".length()).toLowerCase();
            } else if (args[i].equals("--steps")) {
                showSteps = true;
            }
        }

        OptimizationPipeline pipeline = switch (passFilter) {
            case "cf" -> new OptimizationPipeline(List.of(new ConstantFoldingPass()));
            case "cp" -> new OptimizationPipeline(List.of(new ConstantPropagationPass()));
            case "as" -> new OptimizationPipeline(List.of(new AlgebraicSimplificationPass()));
            case "dce" -> new OptimizationPipeline(List.of(new DeadCodeEliminationPass()));
            default -> OptimizationPipeline.createDefault();
        };

        OptimizationReport report = pipeline.optimize(tac);

        System.out.println("=== Unoptimized Three-Address Code ===");
        System.out.print(report.originalProgram().toPrettyString());
        System.out.println("\n=== Optimized Three-Address Code ===");
        System.out.print(report.optimizedProgram().toPrettyString());
        System.out.println("\n" + report.summary());

        if (showSteps) {
            System.out.println("\n" + report.detailedLog());
        }
    }

    private static void handleRun(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: minijava run <source-file> [--optimized]");
            return;
        }
        String source = readFile(args[1]);
        Program program = parseSource(source);
        new SemanticAnalyzer().analyze(program);

        boolean runOptimized = false;
        for (int i = 2; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--optimized")) {
                runOptimized = true;
            }
        }

        TacProgram tac = new IrGenerator().generate(program);
        if (runOptimized) {
            OptimizationReport report = OptimizationPipeline.createDefault().optimize(tac);
            tac = report.optimizedProgram();
        }

        TacInterpreter interpreter = new TacInterpreter();
        ExecutionResult result = interpreter.execute(tac);

        for (String line : result.outputLines()) {
            System.out.println(line);
        }
    }

    private static void handleCompare(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: minijava compare <source-file>");
            return;
        }
        String source = readFile(args[1]);
        Program program = parseSource(source);
        new SemanticAnalyzer().analyze(program);

        TacProgram unoptTac = new IrGenerator().generate(program);
        OptimizationReport report = OptimizationPipeline.createDefault().optimize(unoptTac);
        TacProgram optTac = report.optimizedProgram();

        SemanticEquivalenceVerifier verifier = new SemanticEquivalenceVerifier();
        SemanticEquivalenceVerifier.VerificationReport verification = verifier.verify(program, unoptTac, optTac);

        System.out.println("=================================================");
        System.out.println("       MINIJAVA OPTIMIZATION COMPARISON          ");
        System.out.println("=================================================");
        System.out.println("\n--- SOURCE CODE ---");
        System.out.println(source.trim());

        System.out.println("\n--- ORIGINAL TAC (" + unoptTac.size() + " instructions) ---");
        System.out.print(unoptTac.toPrettyString());

        System.out.println("\n--- OPTIMIZED TAC (" + optTac.size() + " instructions) ---");
        System.out.print(optTac.toPrettyString());

        System.out.println("\n--- OPTIMIZATION METRICS ---");
        System.out.println(report.summary());

        System.out.println("\n--- SEMANTIC EQUIVALENCE & OUTPUT ---");
        System.out.println(verification.formatSummary());
    }

    private static void handleRepl() throws Exception {
        System.out.println("=================================================");
        System.out.println("MiniJava Optimizer Interactive REPL (Java 21)");
        System.out.println("Type MiniJava statements (e.g. 'int a = 2 + 3; print(a);').");
        System.out.println("Type 'exit' to quit.");
        System.out.println("=================================================");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder snippet = new StringBuilder();

        while (true) {
            System.out.print("minijava> ");
            String line = reader.readLine();
            if (line == null || line.trim().equalsIgnoreCase("exit")) {
                break;
            }
            if (line.trim().isEmpty()) {
                continue;
            }

            try {
                Program program = parseSource(line);
                new SemanticAnalyzer().analyze(program);
                TacProgram unopt = new IrGenerator().generate(program);
                OptimizationReport opt = OptimizationPipeline.createDefault().optimize(unopt);
                ExecutionResult res = new TacInterpreter().execute(opt.optimizedProgram());

                System.out.println("  Original TAC:  " + unopt.instructions());
                System.out.println("  Optimized TAC: " + opt.optimizedProgram().instructions());
                System.out.println("  Program Output: " + res.outputLines());
            } catch (Exception e) {
                System.out.println("  Error: " + e.getMessage());
            }
        }
    }

    private static Program parseSource(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenizeAll();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    private static String readFile(String filePath) throws Exception {
        return Files.readString(Path.of(filePath));
    }

    private static void printHelp() {
        System.out.println("""
                MiniJava Optimizer — Compiler Optimization Learning System (Java 21)
                
                USAGE:
                  mvn compile exec:java -Dexec.args="<command> [arguments]"
                  or java -jar target/minijava-optimizer-1.0.0.jar <command> [arguments]

                COMMANDS:
                  tokenize <file>               Tokenize source and show tokens with line:col
                  parse <file>                  Parse source and display the Abstract Syntax Tree
                  check <file>                  Run semantic analysis and print symbol table
                  ir <file>                     Generate Three-Address Code (TAC)
                  optimize <file> [--pass=...]  Run optimization passes (cf, cp, as, dce, all)
                  run <file> [--optimized]      Execute program and show standard output
                  compare <file>                Compare original vs optimized IR and verify equivalence
                  repl                          Start interactive MiniJava optimization shell
                  help                          Show this help message
                """);
    }
}
