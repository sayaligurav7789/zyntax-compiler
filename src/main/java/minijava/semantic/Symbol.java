package minijava.semantic;

import minijava.lexer.SourceLocation;

import java.util.Objects;

/**
 * Symbol entry in the symbol table representing a declared MiniJava variable.
 */
public class Symbol {
    private final String name;
    private final SourceLocation declaredLocation;
    private boolean initialized;

    public Symbol(String name, SourceLocation declaredLocation, boolean initialized) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.declaredLocation = Objects.requireNonNull(declaredLocation, "Location cannot be null");
        this.initialized = initialized;
    }

    public String getName() {
        return name;
    }

    public SourceLocation getDeclaredLocation() {
        return declaredLocation;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        return String.format("Symbol{name='%s', declaredAt=%s, initialized=%s}", name, declaredLocation, initialized);
    }
}
