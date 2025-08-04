package io.kite.Frontend.Parse.Literals;

import io.kite.Frontend.Parser.Expressions.Expression;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

public abstract sealed class Identifier extends Expression
        permits ParameterIdentifier, PathIdentifier, PluginIdentifier, SymbolIdentifier, TypeIdentifier{
    @Setter
    @Getter
    private Integer hops; // used to figure out the scope without creating extra classes

    public Identifier() {
    }

    public abstract String string();

    public static Identifier id(String identifier) {
        return SymbolIdentifier.id(identifier);
    }

    public static List<Identifier> id(String... left) {
        return Arrays.stream(left).map(SymbolIdentifier::of).toList();
    }

    public static Identifier symbol(String identifier) {
        return SymbolIdentifier.id(identifier);
    }


}
