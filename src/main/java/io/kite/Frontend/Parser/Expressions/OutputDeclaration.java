package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Runtime.Values.DependencyHolder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static io.kite.Frontend.Parse.Literals.BooleanLiteral.bool;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;

@Data
@EqualsAndHashCode(callSuper = true)
public final class OutputDeclaration extends Statement implements DependencyHolder {
    private Identifier id;
    private Expression init;
    private TypeIdentifier type;

    public OutputDeclaration() {
    }

    private OutputDeclaration(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private OutputDeclaration(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private OutputDeclaration(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private OutputDeclaration(Expression id) {
        this(id, null);
    }

    public static OutputDeclaration output(Expression id, Expression init) {
        return new OutputDeclaration(id, init);
    }

    public static OutputDeclaration output(String id, Expression init) {
        return new OutputDeclaration(Identifier.id(id), init);
    }

    public static OutputDeclaration output(String id, int init) {
        return new OutputDeclaration(Identifier.id(id), number(init));
    }

    public static OutputDeclaration output(String id, String init) {
        return new OutputDeclaration(Identifier.id(id), string(init));
    }

    public static OutputDeclaration output(String id, double init) {
        return new OutputDeclaration(Identifier.id(id), number(init));
    }

    public static OutputDeclaration output(String id, TypeIdentifier type) {
        return new OutputDeclaration(Identifier.id(id), type);
    }

    public static OutputDeclaration output(String id) {
        return new OutputDeclaration(Identifier.id(id));
    }

    public static OutputDeclaration output(Expression id, TypeIdentifier type, Expression init) {
        return new OutputDeclaration(id, type, init);
    }

    public static OutputDeclaration output(Expression id, TypeIdentifier type) {
        return new OutputDeclaration(id, type);
    }

    public static OutputDeclaration output(Expression id) {
        if (!(id instanceof Identifier)) {
            throw new IllegalArgumentException("Identifier expected but got: " + id.getClass().getSimpleName());
        }
        return new OutputDeclaration(id);
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, Expression init) {
        return OutputDeclaration.output(Identifier.id(id), type, init);
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, int init) {
        return OutputDeclaration.output(Identifier.id(id), type, number(init));
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, String init) {
        return OutputDeclaration.output(Identifier.id(id), type, string(init));
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, boolean init) {
        return OutputDeclaration.output(Identifier.id(id), type, bool(init));
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
