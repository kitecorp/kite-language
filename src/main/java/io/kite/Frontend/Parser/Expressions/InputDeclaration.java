package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Statements.Statement;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;

@Data
@EqualsAndHashCode(callSuper = true)
public final class InputDeclaration extends Statement {
    private Identifier id;
    private Expression init;
    private TypeIdentifier type;

    public InputDeclaration() {
    }

    private InputDeclaration(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private InputDeclaration(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private InputDeclaration(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private InputDeclaration(Expression id) {
        this(id, null);
    }

    public static InputDeclaration input(Expression id, Expression init) {
        return new InputDeclaration(id, init);
    }

    public static InputDeclaration input(String id, Expression init) {
        return new InputDeclaration(Identifier.id(id), init);
    }

    public static InputDeclaration input(String id, int init) {
        return new InputDeclaration(Identifier.id(id), number(init));
    }

    public static InputDeclaration input(String id, String init) {
        return new InputDeclaration(Identifier.id(id), string(init));
    }

    public static InputDeclaration input(String id, double init) {
        return new InputDeclaration(Identifier.id(id), number(init));
    }

    public static InputDeclaration input(String id, TypeIdentifier type) {
        return new InputDeclaration(Identifier.id(id), type);
    }

    public static InputDeclaration input(String id) {
        return new InputDeclaration(Identifier.id(id));
    }

    public static InputDeclaration input(Expression id, TypeIdentifier type, Expression init) {
        return new InputDeclaration(id, type, init);
    }

    public static InputDeclaration input(Expression id, TypeIdentifier type) {
        return new InputDeclaration(id, type);
    }

    public static InputDeclaration input(Expression id) {
        if (!(id instanceof Identifier)) {
            throw new IllegalArgumentException("Identifier expected but got: " + id.getClass().getSimpleName());
        }
        return new InputDeclaration(id);
    }

    public static InputDeclaration input(String id, TypeIdentifier type, Expression init) {
        return InputDeclaration.input(Identifier.id(id), type, init);
    }

    public static InputDeclaration input(String id, TypeIdentifier type, int init) {
        return InputDeclaration.input(Identifier.id(id), type, number(init));
    }

    public String name() {
        return id.string();
    }

    public boolean hasInit() {
        return init != null;
    }

    public boolean hasType() {
        return type != null;
    }
}
