package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parser.Literals.Identifier;
import io.zmeu.Frontend.Parser.Literals.TypeIdentifier;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ObjectExpression extends Expression {
    private Identifier id;
    private Expression init;
    private TypeIdentifier type;

    public ObjectExpression() {
    }

    private ObjectExpression(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private ObjectExpression(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private ObjectExpression(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private ObjectExpression(Expression id) {
        this(id, null);
    }

    public static ObjectExpression object(Expression id, Expression init) {
        return new ObjectExpression(id, init);
    }

    public static ObjectExpression object() {
        return new ObjectExpression();
    }

    public static ObjectExpression object(String id, Expression init) {
        return new ObjectExpression(Identifier.id(id), init);
    }

    public static ObjectExpression object(String id, TypeIdentifier type) {
        return new ObjectExpression(Identifier.id(id), type);
    }

    public static ObjectExpression object(String id) {
        return new ObjectExpression(Identifier.id(id));
    }

    public static ObjectExpression of(Expression id, TypeIdentifier type, Expression init) {
        return new ObjectExpression(id, type, init);
    }

    public static ObjectExpression of(Expression id, TypeIdentifier type) {
        return new ObjectExpression(id, type);
    }

    public static ObjectExpression of(Expression id) {
        return new ObjectExpression(id);
    }

    public static ObjectExpression object(Expression id) {
        if (!(id instanceof Identifier)) {
            throw new IllegalArgumentException("Identifier expected but got: " + id.getClass().getSimpleName());
        }
        return new ObjectExpression(id);
    }

    public static ObjectExpression of(String id) {
        return new ObjectExpression(Identifier.id(id));
    }

    public static ObjectExpression object(String id, TypeIdentifier type, Expression init) {
        return ObjectExpression.of(Identifier.id(id), type, init);
    }

    public boolean hasInit() {
        return init != null;
    }

    public boolean hasType() {
        return type != null;
    }
}
