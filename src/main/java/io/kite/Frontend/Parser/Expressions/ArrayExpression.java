package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.ArrayTypeIdentifier;
import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.Literal;
import io.kite.Frontend.Parser.Statements.ForStatement;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

import static io.kite.Frontend.Parse.Literals.BooleanLiteral.bool;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ArrayExpression extends Expression {
    /**
     * Literal or Identifeir (var/val)
     */
    private List<Expression> items;
    private ArrayTypeIdentifier type;
    private ForStatement forStatement;

    public ArrayExpression() {
        this.items = new ArrayList<>();
    }

    public ArrayExpression(ArrayTypeIdentifier type) {
        this.type = type;
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

    public static ArrayExpression array(ForStatement statement) {
        var expression = new ArrayExpression();
        expression.setForStatement(statement);
        return expression;
    }

    public static ArrayExpression array(Expression... id) {
        var expression = new ArrayExpression();
        expression.items = List.of(id);
        return expression;
    }

    public static ArrayExpression array(Identifier... id) {
        var expression = new ArrayExpression();
        expression.items = List.of(id);
        return expression;
    }

    public static ArrayExpression array(int... id) {
        var expression = new ArrayExpression();
        for (var i : id) {
            expression.add(number(i));
        }
        return expression;
    }

    public static ArrayExpression array(double... id) {
        var expression = new ArrayExpression();
        for (var i : id) {
            expression.add(number(i));
        }
        return expression;
    }

    public static ArrayExpression array(boolean... id) {
        var expression = new ArrayExpression();
        for (var i : id) {
            expression.add(bool(i));
        }
        return expression;
    }

    public static ArrayExpression array(String... id) {
        var expression = new ArrayExpression();
        for (var i : id) {
            expression.add(string(i));
        }
        return expression;
    }

    public static ArrayExpression array(ArrayTypeIdentifier type, String... id) {
        var expression = new ArrayExpression(type);
        for (var i : id) {
            expression.add(string(i));
        }
        return expression;
    }

    public static ArrayExpression array(List<Literal> list) {
        var expression = new ArrayExpression();
        expression.items.addAll(list);
        return expression;
    }

    public static ArrayExpression array() {
        return new ArrayExpression();
    }

    public static ArrayExpression array(ArrayTypeIdentifier type) {
        return new ArrayExpression(type);
    }

    public boolean isForStatement() {
        return this.forStatement != null;
    }

    public void add(Expression expression) {
        this.items.add(expression);
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public boolean hasItems() {
        return !this.items.isEmpty();
    }

    public Expression getFirst() {
        return items.getFirst();
    }

    public boolean hasForStatement() {
        return forStatement != null;
    }
}
