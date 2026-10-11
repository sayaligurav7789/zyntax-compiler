package minijava.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handwritten lexical analyzer for MiniJava.
 * Converts raw source text into a stream of tokens while maintaining precise
 * line and column numbers.
 */
public class Lexer {
    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "int", TokenType.KEYWORD_INT,
            "print", TokenType.KEYWORD_PRINT
    );

    private final String source;
    private int cursor = 0;
    private int line = 1;
    private int column = 1;

    public Lexer(String source) {
        this.source = Objects.requireNonNull(source, "Source code cannot be null");
    }

    /**
     * Scans and returns all tokens up to and including the EOF token.
     */
    public List<Token> tokenizeAll() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            Token token = nextToken();
            tokens.add(token);
            if (token.type() == TokenType.EOF) {
                break;
            }
        }
        return tokens;
    }

    /**
     * Reads the next available token from the source code.
     */
    public Token nextToken() {
        skipWhitespaceAndComments();

        if (isAtEnd()) {
            return new Token(TokenType.EOF, "", new SourceLocation(line, column));
        }

        int startLine = line;
        int startColumn = column;
        SourceLocation tokenLocation = new SourceLocation(startLine, startColumn);
        char c = advance();

        switch (c) {
            case '+' -> {
                return new Token(TokenType.PLUS, "+", tokenLocation);
            }
            case '-' -> {
                return new Token(TokenType.MINUS, "-", tokenLocation);
            }
            case '*' -> {
                return new Token(TokenType.STAR, "*", tokenLocation);
            }
            case '/' -> {
                return new Token(TokenType.SLASH, "/", tokenLocation);
            }
            case '%' -> {
                return new Token(TokenType.PERCENT, "%", tokenLocation);
            }
            case '=' -> {
                return new Token(TokenType.ASSIGN, "=", tokenLocation);
            }
            case '(' -> {
                return new Token(TokenType.LPAREN, "(", tokenLocation);
            }
            case ')' -> {
                return new Token(TokenType.RPAREN, ")", tokenLocation);
            }
            case ';' -> {
                return new Token(TokenType.SEMICOLON, ";", tokenLocation);
            }
            default -> {
                if (isDigit(c)) {
                    return scanNumber(c, tokenLocation);
                }
                if (isIdentifierStart(c)) {
                    return scanIdentifierOrKeyword(c, tokenLocation);
                }
                throw new LexerException("Unexpected character '" + c + "'", tokenLocation);
            }
        }
    }

    private Token scanNumber(char firstChar, SourceLocation location) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);

        while (!isAtEnd() && isDigit(peek())) {
            sb.append(advance());
        }

        String lexeme = sb.toString();
        try {
            int value = Integer.parseInt(lexeme);
            return new Token(TokenType.INT_LITERAL, lexeme, value, location);
        } catch (NumberFormatException e) {
            throw new LexerException("Integer literal '" + lexeme + "' exceeds 32-bit signed range", location);
        }
    }

    private Token scanIdentifierOrKeyword(char firstChar, SourceLocation location) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstChar);

        while (!isAtEnd() && isIdentifierPart(peek())) {
            sb.append(advance());
        }

        String lexeme = sb.toString();
        TokenType keywordType = KEYWORDS.get(lexeme);
        if (keywordType != null) {
            return new Token(keywordType, lexeme, location);
        }
        return new Token(TokenType.IDENTIFIER, lexeme, location);
    }

    private void skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else if (c == '\n') {
                advance();
            } else if (c == '/') {
                if (peekNext() == '/') {
                    // Single-line comment: skip until newline or EOF
                    advance(); // skip '/'
                    advance(); // skip '/'
                    while (!isAtEnd() && peek() != '\n') {
                        advance();
                    }
                } else if (peekNext() == '*') {
                    // Multi-line comment: skip until '*/'
                    SourceLocation commentStart = new SourceLocation(line, column);
                    advance(); // skip '/'
                    advance(); // skip '*'
                    boolean closed = false;
                    while (!isAtEnd()) {
                        if (peek() == '*' && peekNext() == '/') {
                            advance(); // skip '*'
                            advance(); // skip '/'
                            closed = true;
                            break;
                        }
                        advance();
                    }
                    if (!closed) {
                        throw new LexerException("Unterminated block comment starting at " + commentStart, commentStart);
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private boolean isAtEnd() {
        return cursor >= source.length();
    }

    private char advance() {
        char c = source.charAt(cursor++);
        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(cursor);
    }

    private char peekNext() {
        if (cursor + 1 >= source.length()) return '\0';
        return source.charAt(cursor + 1);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || isDigit(c);
    }
}
