package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parse.Literals.Literal;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ArrayExpression extends Expression {
    private List<Literal> items;

    public ArrayExpression() {
        this.items = new ArrayList<>();
    }

    private ArrayExpression(Literal property) {
        this();
        this.items.add(property);
    }

    public static ArrayExpression array(Literal literal) {
        return new ArrayExpression(literal);
    }

    public static ArrayExpression array(Literal... id) {
        var expression = new ArrayExpression();
        expression.items = List.of(id);
        return expression;
    }

    public static ArrayExpression array(List<Literal> list) {
        var expression = new ArrayExpression();
        expression.items = list;
        return expression;
    }

    public static ArrayExpression array() {
        return new ArrayExpression();
    }

}
