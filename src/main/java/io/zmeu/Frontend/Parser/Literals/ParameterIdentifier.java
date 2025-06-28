package io.zmeu.Frontend.Parser.Literals;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A path is a sequence of one or more path segments logically separated by a package qualifier (.)
 * If a path has multiple segments it always refers to an item. If there are no multiple segments it refers to an
 * item in the local scope.
 * This usually holds a package name when importing a package or when using a schema/resource
 * PathIdentifier:
 * ; PathIdentifier?.TypeIdentifier
 */
//@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true )
@Data
@EqualsAndHashCode(callSuper = true)
public final class ParameterIdentifier extends Identifier {
    private TypeIdentifier type;
    private SymbolIdentifier name;

    private ParameterIdentifier() {
        super();
    }

    private ParameterIdentifier(SymbolIdentifier name, TypeIdentifier type) {
        this.type = type;
        this.name = name;
    }

    private ParameterIdentifier(SymbolIdentifier name) {
        this.name = name;
    }

    public static ParameterIdentifier param(String name, String type) {
        return new ParameterIdentifier(SymbolIdentifier.id(name), TypeIdentifier.type(type));
    }

    public static ParameterIdentifier param(String name) {
        return new ParameterIdentifier(SymbolIdentifier.id(name));
    }

    public static ParameterIdentifier param(SymbolIdentifier name) {
        return new ParameterIdentifier(name);
    }

    public static ParameterIdentifier param(SymbolIdentifier name, TypeIdentifier type) {
        return new ParameterIdentifier(name, type);
    }

    public static ParameterIdentifier param(String name, TypeIdentifier type) {
        return new ParameterIdentifier(SymbolIdentifier.id(name), type);
    }

    @Override
    public String string() {
        if (type == null) {
            return name.string();
        }
        return name.string() + " :" + type.string();
    }

}
