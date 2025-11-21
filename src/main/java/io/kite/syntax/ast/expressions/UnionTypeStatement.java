package io.kite.syntax.ast.expressions;

import io.kite.syntax.ast.statements.BlockExpression;
import io.kite.syntax.ast.statements.Statement;
import io.kite.syntax.parser.literals.Identifier;
import io.kite.syntax.parser.literals.SymbolIdentifier;
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
        this(Identifier.id(name), List.of(expressions));
    }

    public static Statement union() {
        return new UnionTypeStatement();
    }

    public static Statement union(Identifier name, Expression block) {
        return new UnionTypeStatement(name, block);
    }

    public static UnionTypeStatement union(Identifier name, List<Expression> block) {
        return new UnionTypeStatement(name, block);
    }

    public static Statement union(String name, Expression... block) {
        return new UnionTypeStatement(name, block);
    }

    public static Statement union(String name, BlockExpression block) {
        return new UnionTypeStatement(Identifier.id(name), block);
    }

    public static Statement union(String name, Expression block) {
        return new UnionTypeStatement(Identifier.id(name), block);
    }

    public String name() {
        return name.string();
    }


}
