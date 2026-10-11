package minijava.semantic;

import minijava.lexer.SourceLocation;

import java.util.*;

/**
 * Symbol table maintaining declarations and states of identifiers.
 */
public class SymbolTable {
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    public boolean declare(String name, SourceLocation location, boolean initialized) {
        if (symbols.containsKey(name)) {
            return false;
        }
        symbols.put(name, new Symbol(name, location, initialized));
        return true;
    }

    public Optional<Symbol> lookup(String name) {
        return Optional.ofNullable(symbols.get(name));
    }

    public boolean isDeclared(String name) {
        return symbols.containsKey(name);
    }

    public void markInitialized(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            symbol.setInitialized(true);
        }
    }

    public Collection<Symbol> getSymbols() {
        return Collections.unmodifiableCollection(symbols.values());
    }
}
