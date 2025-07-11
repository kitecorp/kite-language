package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.SymbolIdentifier;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.Statement;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Data
public final class TypeExpression extends Statement {
    @Nullable
    private Identifier name;
    private Expression expression;

    private TypeExpression() {
        this.name = new SymbolIdentifier();
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

    private TypeExpression(Identifier name, Expression expression) {
        this();
        this.name = name;
        this.expression = expression;
    }

    public static Statement type() {
        return new TypeExpression();
    }

    public static Statement type(Identifier name, Expression block) {
        return new TypeExpression(name, block);
    }

    public static Statement type(String name, BlockExpression block) {
        return new TypeExpression(Identifier.id(name), block);
    }

    public static Statement type(String name, Expression block) {
        return new TypeExpression(Identifier.id(name), block);
    }

    public String name() {
        return name.string();
    }


}
