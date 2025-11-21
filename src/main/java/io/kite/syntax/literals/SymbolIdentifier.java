package io.kite.syntax.literals;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Acts as VariableExpression without creating a new node
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class SymbolIdentifier extends Identifier {
    private String symbol;

    public SymbolIdentifier() {
        super();
    }

    public SymbolIdentifier(String symbol) {
        this();
        this.symbol = symbol;
    }

    public SymbolIdentifier(String symbol, Integer hops) {
        this();
        this.symbol = symbol;
        setHops(hops);
    }

    public SymbolIdentifier(Object symbol) {
        this();
        if (symbol instanceof String s) {
            this.symbol = s;
        }
    }

    public SymbolIdentifier(boolean symbol) {
        this();
        this.symbol = String.valueOf(symbol);
    }

    public static Identifier of(String left) {
        return new SymbolIdentifier(left);
    }

    public static SymbolIdentifier id(String left) {
        return new SymbolIdentifier(left);
    }

    public static Identifier symbol(String identifier, int hops) {
        return new SymbolIdentifier(identifier, hops);
    }

    @Override
    public String string() {
        return symbol;
    }


}
