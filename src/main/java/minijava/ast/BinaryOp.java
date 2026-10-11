package minijava.ast;

/**
 * Binary arithmetic operators in MiniJava.
 */
public enum BinaryOp {
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%");

    private final String symbol;

    BinaryOp(String symbol) {
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
