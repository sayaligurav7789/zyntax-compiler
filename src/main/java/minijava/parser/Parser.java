package minijava.parser;

import minijava.ast.*;
import minijava.lexer.SourceLocation;
import minijava.lexer.Token;
import minijava.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Handwritten recursive-descent parser for MiniJava.
 *
 * Grammar:
 * <pre>
 * Program            -> Statement* EOF
 * Statement          -> VarDeclStatement | AssignStatement | PrintStatement
 * VarDeclStatement   -> "int" IDENTIFIER ( "=" Expression )? ";"
 * AssignStatement    -> IDENTIFIER "=" Expression ";"
 * PrintStatement     -> "print" "(" Expression ")" ";"
 * Expression         -> AdditiveExpr
 * AdditiveExpr       -> MultiplicativeExpr ( ( "+" | "-" ) MultiplicativeExpr )*
 * MultiplicativeExpr -> UnaryExpr ( ( "*" | "/" | "%" ) UnaryExpr )*
 * UnaryExpr          -> ( "-" | "+" ) UnaryExpr | PrimaryExpr
 * PrimaryExpr        -> INT_LITERAL | IDENTIFIER | "(" Expression ")"
 * </pre>
 */
public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = Objects.requireNonNull(tokens, "Tokens list cannot be null");
    }

    /**
     * Parses the complete token stream into an Abstract Syntax Tree Program node.
     */
    public Program parse() {
        List<Statement> statements = new ArrayList<>();
        SourceLocation startLocation = peek().location();

        while (!isAtEnd()) {
            statements.add(statement());
        }

        return new Program(startLocation, statements);
    }

    private Statement statement() {
        if (check(TokenType.KEYWORD_INT)) {
            return varDeclaration();
        }
        if (check(TokenType.KEYWORD_PRINT)) {
            return printStatement();
        }
        if (check(TokenType.IDENTIFIER)) {
            return assignment();
        }

        Token token = peek();
        throw new ParseException("Expected statement ('int', 'print', or variable assignment), found " + token.type().getDescription() + " '" + token.lexeme() + "'", token.location());
    }

    private VarDeclStatement varDeclaration() {
        Token intToken = consume(TokenType.KEYWORD_INT, "Expected 'int' keyword");
        Token nameToken = consume(TokenType.IDENTIFIER, "Expected variable identifier after 'int'");
        Expression initializer = null;

        if (match(TokenType.ASSIGN)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new VarDeclStatement(intToken.location(), nameToken.lexeme(), initializer);
    }

    private AssignStatement assignment() {
        Token nameToken = consume(TokenType.IDENTIFIER, "Expected variable identifier in assignment");
        consume(TokenType.ASSIGN, "Expected '=' after variable name in assignment");
        Expression value = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after assignment expression");
        return new AssignStatement(nameToken.location(), nameToken.lexeme(), value);
    }

    private PrintStatement printStatement() {
        Token printToken = consume(TokenType.KEYWORD_PRINT, "Expected 'print' keyword");
        consume(TokenType.LPAREN, "Expected '(' after 'print'");
        Expression expr = expression();
        consume(TokenType.RPAREN, "Expected ')' after expression in print statement");
        consume(TokenType.SEMICOLON, "Expected ';' after print statement");
        return new PrintStatement(printToken.location(), expr);
    }

    private Expression expression() {
        return additive();
    }

    private Expression additive() {
        Expression expr = multiplicative();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            BinaryOp op = (operator.type() == TokenType.PLUS) ? BinaryOp.ADD : BinaryOp.SUB;
            Expression right = multiplicative();
            expr = new BinaryExpression(operator.location(), op, expr, right);
        }

        return expr;
    }

    private Expression multiplicative() {
        Expression expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token operator = previous();
            BinaryOp op = switch (operator.type()) {
                case STAR -> BinaryOp.MUL;
                case SLASH -> BinaryOp.DIV;
                case PERCENT -> BinaryOp.MOD;
                default -> throw new IllegalStateException("Unexpected operator: " + operator);
            };
            Expression right = unary();
            expr = new BinaryExpression(operator.location(), op, expr, right);
        }

        return expr;
    }

    private Expression unary() {
        if (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            UnaryOp op = (operator.type() == TokenType.MINUS) ? UnaryOp.NEG : UnaryOp.POS;
            Expression operand = unary();
            return new UnaryExpression(operator.location(), op, operand);
        }

        return primary();
    }

    private Expression primary() {
        if (match(TokenType.INT_LITERAL)) {
            Token token = previous();
            return new IntegerLiteral(token.location(), token.intValue());
        }

        if (match(TokenType.IDENTIFIER)) {
            Token token = previous();
            return new VariableReference(token.location(), token.lexeme());
        }

        if (match(TokenType.LPAREN)) {
            SourceLocation parenLoc = previous().location();
            Expression expr = expression();
            consume(TokenType.RPAREN, "Expected ')' to close grouped expression starting at " + parenLoc);
            return expr;
        }

        Token token = peek();
        throw new ParseException("Expected expression (integer literal, identifier, or '('), found " + token.type().getDescription() + " '" + token.lexeme() + "'", token.location());
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return type == TokenType.EOF;
        }
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        if (current >= tokens.size()) {
            return tokens.getLast();
        }
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        Token currentToken = peek();
        throw new ParseException(message + ", but found " + currentToken.type().getDescription() + " '" + currentToken.lexeme() + "'", currentToken.location());
    }
}
