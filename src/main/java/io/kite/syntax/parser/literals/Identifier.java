package io.kite.syntax.parser.literals;

import io.kite.syntax.ast.expressions.Expression;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

public abstract sealed class Identifier extends Expression
        permits ParameterIdentifier, PathIdentifier, PluginIdentifier, SymbolIdentifier, TypeIdentifier {
    @Setter
    @Getter
    private Integer hops; // used to figure out the scope without creating extra classes

    public Identifier() {
    }

    public static Identifier id(String identifier) {
        return SymbolIdentifier.id(identifier);
    }

    public static List<Identifier> id(String... left) {
        return Arrays.stream(left).map(SymbolIdentifier::of).toList();
    }

    public abstract String string();

}
