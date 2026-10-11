package minijava.parser;

import minijava.ast.*;
import minijava.lexer.Lexer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Program parse(String source) {
        return new Parser(new Lexer(source).tokenizeAll()).parse();
    }

    @Test
    @DisplayName("Parse variable declarations with and without initializers")
    void testVariableDeclarations() {
        Program program = parse("int a = 10; int b;");
        assertEquals(2, program.statements().size());

        VarDeclStatement s1 = (VarDeclStatement) program.statements().get(0);
        assertEquals("a", s1.variableName());
        assertTrue(s1.hasInitializer());
        assertInstanceOf(IntegerLiteral.class, s1.initializer());

        VarDeclStatement s2 = (VarDeclStatement) program.statements().get(1);
        assertEquals("b", s2.variableName());
        assertFalse(s2.hasInitializer());
    }

    @Test
    @DisplayName("Parse variable assignment and print statements")
    void testAssignmentAndPrint() {
        Program program = parse("x = 5; print(x);");
        assertEquals(2, program.statements().size());

        AssignStatement assign = (AssignStatement) program.statements().get(0);
        assertEquals("x", assign.variableName());

        PrintStatement print = (PrintStatement) program.statements().get(1);
        assertInstanceOf(VariableReference.class, print.expression());
    }

    @Test
    @DisplayName("Verify operator precedence: multiplication binds tighter than addition")
    void testOperatorPrecedence() {
        Program program = parse("int r = 2 + 3 * 4;");
        VarDeclStatement decl = (VarDeclStatement) program.statements().getFirst();
        BinaryExpression root = assertInstanceOf(BinaryExpression.class, decl.initializer());

        assertEquals(BinaryOp.ADD, root.op());
        assertInstanceOf(IntegerLiteral.class, root.left()); // 2

        BinaryExpression rightMult = assertInstanceOf(BinaryExpression.class, root.right());
        assertEquals(BinaryOp.MUL, rightMult.op()); // 3 * 4
    }

    @Test
    @DisplayName("Verify left associativity: 10 - 4 - 2 is parsed as (10 - 4) - 2")
    void testLeftAssociativity() {
        Program program = parse("int r = 10 - 4 - 2;");
        VarDeclStatement decl = (VarDeclStatement) program.statements().getFirst();
        BinaryExpression root = assertInstanceOf(BinaryExpression.class, decl.initializer());

        assertEquals(BinaryOp.SUB, root.op());
        BinaryExpression leftSub = assertInstanceOf(BinaryExpression.class, root.left());
        assertEquals(BinaryOp.SUB, leftSub.op());
        assertInstanceOf(IntegerLiteral.class, root.right());
    }

    @Test
    @DisplayName("Parse parenthesized expressions overriding default precedence")
    void testParenthesizedExpressions() {
        Program program = parse("int r = (2 + 3) * 4;");
        VarDeclStatement decl = (VarDeclStatement) program.statements().getFirst();
        BinaryExpression root = assertInstanceOf(BinaryExpression.class, decl.initializer());

        assertEquals(BinaryOp.MUL, root.op());
        BinaryExpression leftGroup = assertInstanceOf(BinaryExpression.class, root.left());
        assertEquals(BinaryOp.ADD, leftGroup.op());
    }

    @Test
    @DisplayName("Parse unary expressions correctly")
    void testUnaryExpressions() {
        Program program = parse("int r = -(-5);");
        VarDeclStatement decl = (VarDeclStatement) program.statements().getFirst();
        UnaryExpression outer = assertInstanceOf(UnaryExpression.class, decl.initializer());
        assertEquals(UnaryOp.NEG, outer.op());

        UnaryExpression inner = assertInstanceOf(UnaryExpression.class, outer.operand());
        assertEquals(UnaryOp.NEG, inner.op());
    }

    @Test
    @DisplayName("Throw ParseException on missing semicolon")
    void testMissingSemicolon() {
        ParseException ex = assertThrows(ParseException.class, () -> parse("int a = 5"));
        assertTrue(ex.getMessage().contains("Expected ';'"));
    }

    @Test
    @DisplayName("Throw ParseException on unclosed parenthesis")
    void testUnclosedParenthesis() {
        ParseException ex = assertThrows(ParseException.class, () -> parse("int a = (2 + 3;"));
        assertTrue(ex.getMessage().contains("Expected ')'"));
    }

    @Test
    @DisplayName("Throw ParseException on unexpected statement start")
    void testInvalidStatement() {
        ParseException ex = assertThrows(ParseException.class, () -> parse("+ 5;"));
        assertTrue(ex.getMessage().contains("Expected statement"));
    }
}
