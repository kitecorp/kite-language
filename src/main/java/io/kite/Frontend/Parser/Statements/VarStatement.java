package io.kite.Frontend.Parser.Statements;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parser.Expressions.VarDeclaration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static io.kite.Frontend.Parser.Expressions.VarDeclaration.var;

/**
 * <p>
 * VariableStatement
 * : var Identity (, Identity)* Assignment Expression
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public final class VarStatement extends Statement {
    private List<VarDeclaration> declarations;

    private VarStatement(@Nullable List<VarDeclaration> declarations) {
        this.declarations = declarations;
    }

    private VarStatement() {
        this(Collections.emptyList());
    }

    public static Statement varStatement(VarDeclaration... expression) {
        return new VarStatement(List.of(expression));
    }

    public static Statement statement(VarDeclaration... expression) {
        return new VarStatement(List.of(expression));
    }

    public static Statement statement(Identifier expression) {
        return new VarStatement(List.of(var(expression)));
    }

    public static VarStatement varStatement(List<VarDeclaration> expression) {
        return new VarStatement(expression);
    }

    public static Statement varStatement() {
        return new VarStatement();
    }

}
