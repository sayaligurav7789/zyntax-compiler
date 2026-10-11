package minijava.lexer;

/**
 * Token types recognized by the MiniJava lexer.
 */
public enum TokenType {
    // Keywords
    KEYWORD_INT("int"),
    KEYWORD_PRINT("print"),

    // Literals and Identifiers
    INT_LITERAL("integer literal"),
    IDENTIFIER("identifier"),

    // Arithmetic Operators
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),

    // Assignment Operator
    ASSIGN("="),

    // Delimiters and Punctuation
    LPAREN("("),
    RPAREN(")"),
    SEMICOLON(";"),

    // End of Input
    EOF("end of file");

    private final String description;

    TokenType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
