package minijava.lexer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    @DisplayName("Tokenize keywords, identifiers, and literals")
    void testBasicTokens() {
        String source = "int score = 42;\nprint(score);";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenizeAll();

        assertEquals(11, tokens.size());
        assertEquals(TokenType.KEYWORD_INT, tokens.get(0).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        assertEquals("score", tokens.get(1).lexeme());
        assertEquals(TokenType.ASSIGN, tokens.get(2).type());
        assertEquals(TokenType.INT_LITERAL, tokens.get(3).type());
        assertEquals(42, tokens.get(3).intValue());
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type());

        assertEquals(TokenType.KEYWORD_PRINT, tokens.get(5).type());
        assertEquals(TokenType.LPAREN, tokens.get(6).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(7).type());
        assertEquals(TokenType.RPAREN, tokens.get(8).type());
        assertEquals(TokenType.SEMICOLON, tokens.get(9).type());
        assertEquals(TokenType.EOF, tokens.get(10).type());
    }

    @Test
    @DisplayName("Track source line and column coordinates accurately")
    void testSourceLocations() {
        String source = "int a = 10;\n  print(a);";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenizeAll();

        // Line 1
        assertEquals(new SourceLocation(1, 1), tokens.get(0).location()); // int
        assertEquals(new SourceLocation(1, 5), tokens.get(1).location()); // a
        assertEquals(new SourceLocation(1, 7), tokens.get(2).location()); // =
        assertEquals(new SourceLocation(1, 9), tokens.get(3).location()); // 10
        assertEquals(new SourceLocation(1, 11), tokens.get(4).location()); // ;

        // Line 2 (indented with 2 spaces -> col 3)
        assertEquals(new SourceLocation(2, 3), tokens.get(5).location()); // print
        assertEquals(new SourceLocation(2, 8), tokens.get(6).location()); // (
        assertEquals(new SourceLocation(2, 9), tokens.get(7).location()); // a
        assertEquals(new SourceLocation(2, 10), tokens.get(8).location()); // )
    }

    @Test
    @DisplayName("Skip single-line and multi-line comments properly")
    void testComments() {
        String source = """
                // Initial configuration
                int a = 5; /* inline comment */
                /*
                 * Multi-line explanation
                 */
                print(a); // trailing comment
                """;
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenizeAll();

        assertEquals(TokenType.KEYWORD_INT, tokens.get(0).type());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        assertEquals(TokenType.ASSIGN, tokens.get(2).type());
        assertEquals(TokenType.INT_LITERAL, tokens.get(3).type());
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type());
        assertEquals(TokenType.KEYWORD_PRINT, tokens.get(5).type());
        assertEquals(TokenType.EOF, tokens.getLast().type());
    }

    @Test
    @DisplayName("Tokenize arithmetic operators (+, -, *, /, %)")
    void testOperators() {
        String source = "+ - * / %";
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenizeAll();

        assertEquals(List.of(
                TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.PERCENT, TokenType.EOF
        ), tokens.stream().map(Token::type).toList());
    }

    @Test
    @DisplayName("Throw LexerException on unterminated block comment")
    void testUnterminatedBlockComment() {
        String source = "int a = 5; /* never closed";
        Lexer lexer = new Lexer(source);
        LexerException ex = assertThrows(LexerException.class, lexer::tokenizeAll);
        assertTrue(ex.getMessage().contains("Unterminated block comment"));
    }

    @Test
    @DisplayName("Throw LexerException on unrecognized characters")
    void testUnexpectedCharacter() {
        String source = "int a = @42;";
        Lexer lexer = new Lexer(source);
        LexerException ex = assertThrows(LexerException.class, lexer::tokenizeAll);
        assertTrue(ex.getMessage().contains("Unexpected character '@'"));
    }

    @Test
    @DisplayName("Throw LexerException on integer overflow")
    void testIntegerOverflow() {
        String source = "int a = 9999999999999999999999999;";
        Lexer lexer = new Lexer(source);
        LexerException ex = assertThrows(LexerException.class, lexer::tokenizeAll);
        assertTrue(ex.getMessage().contains("exceeds 32-bit signed range"));
    }
}
