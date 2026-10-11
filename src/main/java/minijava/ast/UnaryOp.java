package minijava.ast;

/**
 * Unary arithmetic operators in MiniJava.
 */
public enum UnaryOp {
    NEG("-"),
    POS("+");

    private final String symbol;

    UnaryOp(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
