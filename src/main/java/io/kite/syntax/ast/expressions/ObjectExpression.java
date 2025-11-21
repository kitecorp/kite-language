package io.kite.syntax.ast.expressions;

import io.kite.syntax.literals.ObjectLiteral;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ObjectExpression extends Expression {
    private List<ObjectLiteral> properties;

    public ObjectExpression() {
        this.properties = new ArrayList<>();
    }

    private ObjectExpression(ObjectLiteral property) {
        this();
        this.properties.add(property);
    }

    public static ObjectExpression objectExpression(ObjectLiteral literal) {
        return new ObjectExpression(literal);
    }

    public static ObjectExpression objectExpression(ObjectLiteral... id) {
        var expression = new ObjectExpression();
        expression.properties = List.of(id);
        return expression;
    }

    public static ObjectExpression objectExpression(List<ObjectLiteral> list) {
        var expression = new ObjectExpression();
        expression.properties = list;
        return expression;
    }

    public static ObjectExpression objectExpression() {
        return new ObjectExpression();
    }

    public static ObjectExpression object(List<ObjectLiteral> properties) {
        return ObjectExpression.objectExpression(properties);
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

}
