package io.zmeu.Frontend.Parser.Literals;

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

    @Override
    public String string() {
        return symbol;
    }

    public SymbolIdentifier(String symbol) {
        this();
        this.symbol = symbol;
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


}
