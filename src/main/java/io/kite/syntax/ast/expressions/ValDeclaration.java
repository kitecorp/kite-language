package io.kite.syntax.ast.expressions;

import io.kite.syntax.literals.Identifier;
import io.kite.syntax.literals.TypeIdentifier;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static io.kite.syntax.literals.NumberLiteral.number;
import static io.kite.syntax.literals.StringLiteral.string;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ValDeclaration extends Expression {
    private Identifier id;
    private Expression init;
    private TypeIdentifier type;

    public ValDeclaration() {
    }

    private ValDeclaration(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private ValDeclaration(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private ValDeclaration(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private ValDeclaration(Expression id) {
        this(id, null);
    }


    public static ValDeclaration val(Expression id, Expression init) {
        return new ValDeclaration(id, init);
    }

    public static ValDeclaration val(String id, Expression init) {
        return new ValDeclaration(Identifier.id(id), init);
    }

    public static ValDeclaration val(String id, int init) {
        return new ValDeclaration(Identifier.id(id), number(init));
    }

    public static ValDeclaration val(String id, double init) {
        return new ValDeclaration(Identifier.id(id), number(init));
    }

    public static ValDeclaration val(String id, String init) {
        return new ValDeclaration(Identifier.id(id), string(init));
    }

    public static ValDeclaration val(String id, TypeIdentifier type) {
        return new ValDeclaration(Identifier.id(id), type);
    }


    public static ValDeclaration val(Expression id, TypeIdentifier type, Expression init) {
        return new ValDeclaration(id, type, init);
    }

    public static ValDeclaration val(Expression id, TypeIdentifier type) {
        return new ValDeclaration(id, type);
    }

    public static ValDeclaration val(Expression id) {
        return new ValDeclaration(id);
    }

    public static ValDeclaration val(String id) {
        return new ValDeclaration(Identifier.id(id));
    }


    public static ValDeclaration val(String id, TypeIdentifier type, Expression init) {
        return ValDeclaration.val(Identifier.id(id), type, init);
    }

    public boolean hasInit() {
        return init != null;
    }

    public boolean hasType() {
        return type != null;
    }
}
