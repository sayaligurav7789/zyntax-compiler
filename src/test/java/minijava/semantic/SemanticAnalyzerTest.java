package minijava.semantic;

import minijava.ast.Program;
import minijava.lexer.Lexer;
import minijava.parser.Parser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanticAnalyzerTest {

    private Program parse(String source) {
        return new Parser(new Lexer(source).tokenizeAll()).parse();
    }

    @Test
    @DisplayName("Pass valid program with proper declarations and initialization")
    void testValidProgram() {
        Program program = parse("""
                int a = 10;
                int b = a + 5;
                print(b);
                """);
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        SymbolTable table = assertDoesNotThrow(() -> analyzer.analyze(program));
        assertTrue(table.isDeclared("a"));
        assertTrue(table.isDeclared("b"));
    }

    @Test
    @DisplayName("Detect duplicate variable declaration in same scope")
    void testDuplicateDeclaration() {
        Program program = parse("""
                int a = 10;
                int a = 20;
                """);
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        SemanticException ex = assertThrows(SemanticException.class, () -> analyzer.analyze(program));
        assertTrue(ex.getMessage().contains("already declared"));
    }

    @Test
    @DisplayName("Detect assignment to undeclared variable")
    void testAssignmentToUndeclared() {
        Program program = parse("x = 42;");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        SemanticException ex = assertThrows(SemanticException.class, () -> analyzer.analyze(program));
        assertTrue(ex.getMessage().contains("Cannot assign to undeclared variable 'x'"));
    }

    @Test
    @DisplayName("Detect reference to undeclared variable in expression")
    void testReferenceToUndeclared() {
        Program program = parse("int a = b + 1;");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        SemanticException ex = assertThrows(SemanticException.class, () -> analyzer.analyze(program));
        assertTrue(ex.getMessage().contains("Undeclared variable 'b'"));
    }

    @Test
    @DisplayName("Detect use of uninitialized variable")
    void testUninitializedVariable() {
        Program program = parse("""
                int a;
                int b = a + 5;
                """);
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        SemanticException ex = assertThrows(SemanticException.class, () -> analyzer.analyze(program));
        assertTrue(ex.getMessage().contains("may not have been initialized"));
    }

    @Test
    @DisplayName("Detect self-referencing initializer (e.g. int a = a + 1;)")
    void testSelfReferencingInitializer() {
        Program program = parse("int a = a + 1;");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        SemanticException ex = assertThrows(SemanticException.class, () -> analyzer.analyze(program));
        assertTrue(ex.getMessage().contains("Undeclared variable 'a'"));
    }

    @Test
    @DisplayName("Optionally flag static division by zero when strict check is enabled")
    void testStaticDivisionByZero() {
        Program program = parse("int a = 10 / 0;");
        SemanticAnalyzer strictAnalyzer = new SemanticAnalyzer(true);
        SemanticException ex = assertThrows(SemanticException.class, () -> strictAnalyzer.analyze(program));
        assertTrue(ex.getMessage().contains("division or modulo by zero"));
    }
}
