package minijava.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import minijava.ast.AstPrinter;
import minijava.ast.Program;
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
import minijava.semantic.Symbol;
import minijava.semantic.SymbolTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server hosting the MiniJava Optimization Learning Visualizer.
 * Built entirely using Java 21 standard library (com.sun.net.httpserver) with
 * zero external dependencies.
 */
public class MiniJavaWebServer {
    private final int port;
    private HttpServer server;

    public MiniJavaWebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticPageHandler());
        server.createContext("/api/examples", new ExamplesHandler());
        server.createContext("/api/compile", new CompileHandler());
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPort() {
        return (server != null) ? server.getAddress().getPort() : port;
    }

    private static class StaticPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] response = WebUiAssets.HTML_PAGE.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

    private static class ExamplesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder("[");
            List<Path> exampleFiles = new ArrayList<>();
            Path examplesDir = Path.of("examples");

            if (Files.exists(examplesDir)) {
                try (var stream = Files.list(examplesDir)) {
                    exampleFiles = stream.filter(p -> p.toString().endsWith(".minij")).sorted().toList();
                }
            }

            for (int i = 0; i < exampleFiles.size(); i++) {
                Path p = exampleFiles.get(i);
                String code = Files.readString(p);
                sb.append("{")
                  .append(Json.string("name", p.getFileName().toString())).append(",")
                  .append(Json.string("code", code))
                  .append("}");
                if (i < exampleFiles.size() - 1) sb.append(",");
            }
            sb.append("]");

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static class CompileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body;
            try (InputStream is = exchange.getRequestBody()) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            String source = extractJsonField(body, "source");
            String passFilter = extractJsonField(body, "passFilter");
            if (passFilter == null || passFilter.isBlank()) passFilter = "all";

            String jsonResponse;
            try {
                // 1. Lexing
                Lexer lexer = new Lexer(source);
                List<Token> tokens = lexer.tokenizeAll();

                // 2. Parsing
                Parser parser = new Parser(tokens);
                Program program = parser.parse();
                String astPrint = new AstPrinter().print(program);

                // 3. Semantic Analysis
                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                SymbolTable symbolTable = analyzer.analyze(program);

                // 4. IR Generation
                TacProgram unoptTac = new IrGenerator().generate(program);

                // 5. Optimization
                OptimizationPipeline pipeline = switch (passFilter.toLowerCase()) {
                    case "cf" -> new OptimizationPipeline(List.of(new ConstantFoldingPass()));
                    case "cp" -> new OptimizationPipeline(List.of(new ConstantPropagationPass()));
                    case "as" -> new OptimizationPipeline(List.of(new AlgebraicSimplificationPass()));
                    case "dce" -> new OptimizationPipeline(List.of(new DeadCodeEliminationPass()));
                    default -> OptimizationPipeline.createDefault();
                };

                OptimizationReport report = pipeline.optimize(unoptTac);
                TacProgram optTac = report.optimizedProgram();

                // 6. Execution & Verification
                TacInterpreter interpreter = new TacInterpreter();
                ExecutionResult execResult = interpreter.execute(optTac);

                SemanticEquivalenceVerifier verifier = new SemanticEquivalenceVerifier();
                SemanticEquivalenceVerifier.VerificationReport vReport = verifier.verify(program, unoptTac, optTac);

                // Build Success JSON
                jsonResponse = buildSuccessJson(tokens, astPrint, symbolTable, unoptTac, optTac, report, execResult, vReport);
            } catch (Exception ex) {
                jsonResponse = "{" +
                        Json.bool("success", false) + "," +
                        Json.string("errorType", ex.getClass().getSimpleName()) + "," +
                        Json.string("message", ex.getMessage() != null ? ex.getMessage() : ex.toString()) +
                        "}";
            }

            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String buildSuccessJson(
                List<Token> tokens,
                String astPrint,
                SymbolTable symbols,
                TacProgram unopt,
                TacProgram opt,
                OptimizationReport report,
                ExecutionResult execResult,
                SemanticEquivalenceVerifier.VerificationReport vReport
        ) {
            StringBuilder sb = new StringBuilder("{");
            sb.append(Json.bool("success", true)).append(",");

            // Tokens
            sb.append("\"tokens\":[");
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                sb.append("{")
                  .append(Json.string("type", t.type().name())).append(",")
                  .append(Json.string("lexeme", t.lexeme())).append(",")
                  .append(Json.string("literal", t.literalValue() != null ? String.valueOf(t.literalValue()) : "-")).append(",")
                  .append(Json.string("location", t.location().toString()))
                  .append("}");
                if (i < tokens.size() - 1) sb.append(",");
            }
            sb.append("],");

            // AST
            sb.append(Json.string("ast", astPrint)).append(",");

            // Symbols
            sb.append("\"symbols\":[");
            List<Symbol> symList = new ArrayList<>(symbols.getSymbols());
            for (int i = 0; i < symList.size(); i++) {
                Symbol s = symList.get(i);
                sb.append("{")
                  .append(Json.string("name", s.getName())).append(",")
                  .append(Json.string("location", s.getDeclaredLocation().toString())).append(",")
                  .append(Json.bool("initialized", s.isInitialized()))
                  .append("}");
                if (i < symList.size() - 1) sb.append(",");
            }
            sb.append("],");

            // TAC & Metrics
            sb.append(Json.string("unoptimizedTac", unopt.toPrettyString())).append(",");
            sb.append(Json.number("unoptimizedCount", unopt.size())).append(",");
            sb.append(Json.string("optimizedTac", opt.toPrettyString())).append(",");
            sb.append(Json.number("optimizedCount", opt.size())).append(",");
            sb.append(Json.number("reductionPercentage", report.reductionPercentage())).append(",");
            sb.append(Json.number("iterations", report.iterations())).append(",");

            // Steps
            sb.append("\"steps\":[");
            List<OptimizationReport.Step> steps = report.steps();
            for (int i = 0; i < steps.size(); i++) {
                OptimizationReport.Step step = steps.get(i);
                sb.append("{")
                  .append(Json.number("iteration", step.iteration())).append(",")
                  .append(Json.string("passName", step.passName())).append(",")
                  .append(Json.number("changes", step.changesCount())).append(",")
                  .append("\"logs\":").append(Json.stringList(step.logs())).append(",")
                  .append(Json.string("tac", step.programAfter().toPrettyString()))
                  .append("}");
                if (i < steps.size() - 1) sb.append(",");
            }
            sb.append("],");

            // Execution Output
            sb.append("\"output\":").append(Json.stringList(execResult.outputLines())).append(",");
            sb.append(Json.bool("equivalent", vReport.isEquivalent())).append(",");
            sb.append(Json.string("verificationMessage", vReport.message()));

            sb.append("}");
            return sb.toString();
        }

        private String extractJsonField(String json, String field) {
            String pattern = "\"" + field + "\"\\s*:\\s*\"";
            int idx = json.indexOf("\"" + field + "\"");
            if (idx == -1) return "";
            int colon = json.indexOf(':', idx);
            if (colon == -1) return "";
            int startQuote = json.indexOf('"', colon);
            if (startQuote == -1) return "";
            StringBuilder sb = new StringBuilder();
            boolean escape = false;
            for (int i = startQuote + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escape) {
                    if (c == 'n') sb.append('\n');
                    else if (c == 'r') sb.append('\r');
                    else if (c == 't') sb.append('\t');
                    else sb.append(c);
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
