package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.TypeIdentifier;
import io.zmeu.Frontend.Parser.Statements.VarStatement;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class VarDeclaration extends Expression {
    private Identifier id;
    private Expression init;
    private TypeIdentifier type;

    public VarDeclaration() {
    }

    private VarDeclaration(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private VarDeclaration(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private VarDeclaration(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private VarDeclaration(Expression id) {
        this(id, null);
    }

    public static VarDeclaration of(Expression id, Expression init) {
        return new VarDeclaration(id, init);
    }

    public static VarDeclaration var(Expression id, Expression init) {
        return new VarDeclaration(id, init);
    }

    public static VarDeclaration var(String id, Expression init) {
        return new VarDeclaration(Identifier.id(id), init);
    }

    public static VarDeclaration var(String id, TypeIdentifier type) {
        return new VarDeclaration(Identifier.id(id), type);
    }

    public static VarDeclaration var(String id) {
        return new VarDeclaration(Identifier.id(id));
    }

    public static VarDeclaration of(Expression id, TypeIdentifier type, Expression init) {
        return new VarDeclaration(id, type, init);
    }

    public static VarDeclaration of(Expression id, TypeIdentifier type) {
        return new VarDeclaration(id, type);
    }

    public static VarDeclaration of(Expression id) {
        return new VarDeclaration(id);
    }

    public static VarDeclaration var(Expression id) {
        if (!(id instanceof Identifier)) {
            throw new IllegalArgumentException("Identifier expected but got: " + id.getClass().getSimpleName());
        }
        return new VarDeclaration(id);
    }

    public static VarDeclaration of(String id) {
        return new VarDeclaration(Identifier.id(id));
    }

    public static VarStatement var(Identifier id, TypeIdentifier type, Expression init) {
        return (VarStatement) VarStatement.varStatement(of(id, type, init));
    }

    public static VarDeclaration var(String id, TypeIdentifier type, Expression init) {
        return VarDeclaration.of(Identifier.id(id), type, init);
    }

    public boolean hasInit() {
        return init != null;
    }

    public boolean hasType() {
        return type != null;
    }
}
