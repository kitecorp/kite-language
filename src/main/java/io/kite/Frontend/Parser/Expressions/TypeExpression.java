package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.SymbolIdentifier;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.Statement;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Data
public final class TypeExpression extends Statement {
    @Nullable
    private Identifier name;
    private List<Expression> expression;

    private TypeExpression() {
        this.name = new SymbolIdentifier();
    }

    private TypeExpression(Identifier name, Expression expression) {
        this();
        this.name = name;
        this.expression = List.of(expression);
    }

    private TypeExpression(String name, Expression expression) {
        this(Identifier.id(name), expression);
    }

    private TypeExpression(Identifier name, List<Expression> expression) {
        this();
        this.name = name;
        this.expression = expression;
    }

    private TypeExpression(Identifier name, Expression... expression) {
        this(name, List.of(expression));
    }

    private TypeExpression(String name, Expression... expression) {
        this(Identifier.symbol(name), List.of(expression));
    }

    public static Statement type() {
        return new TypeExpression();
    }

    public static Statement type(Identifier name, Expression block) {
        return new TypeExpression(name, block);
    }

    public static Statement type(Identifier name, List<Expression> block) {
        return new TypeExpression(name, block);
    }

    public static Statement type(String name, Expression... block) {
        return new TypeExpression(name, block);
    }

    public static Statement type(String name, BlockExpression block) {
        return new TypeExpression(Identifier.id(name), block);
    }

    public static Statement type(String name, Expression block) {
        return new TypeExpression(Identifier.id(name), block);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeExpression that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getExpression(), that.getExpression());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName(), getExpression());
    }

    public String name() {
        return name.string();
    }


}
