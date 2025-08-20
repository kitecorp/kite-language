package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.SymbolIdentifier;
import io.kite.Frontend.Parser.Statements.BlockExpression;
import io.kite.Frontend.Parser.Statements.Statement;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public final class UnionTypeStatement extends Statement {
    @Nullable
    private Identifier name;
    private List<Expression> expressions;

    private UnionTypeStatement() {
        this.name = new SymbolIdentifier();
    }

    private UnionTypeStatement(Identifier name, Expression expressions) {
        this();
        this.name = name;
        this.expressions = List.of(expressions);
    }

    private UnionTypeStatement(String name, Expression expressions) {
        this(Identifier.id(name), expressions);
    }

    private UnionTypeStatement(Identifier name, List<Expression> expressions) {
        this();
        this.name = name;
        this.expressions = expressions;
    }

    private UnionTypeStatement(Identifier name, Expression... expressions) {
        this(name, List.of(expressions));
    }

    private UnionTypeStatement(String name, Expression... expressions) {
        this(Identifier.symbol(name), List.of(expressions));
    }

    public static Statement type() {
        return new UnionTypeStatement();
    }

    public static Statement type(Identifier name, Expression block) {
        return new UnionTypeStatement(name, block);
    }

    public static Statement type(Identifier name, List<Expression> block) {
        return new UnionTypeStatement(name, block);
    }

    public static Statement type(String name, Expression... block) {
        return new UnionTypeStatement(name, block);
    }

    public static Statement type(String name, BlockExpression block) {
        return new UnionTypeStatement(Identifier.id(name), block);
    }

    public static Statement type(String name, Expression block) {
        return new UnionTypeStatement(Identifier.id(name), block);
    }

    public String name() {
        return name.string();
    }


}
