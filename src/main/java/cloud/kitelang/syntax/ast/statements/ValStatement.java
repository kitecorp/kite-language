package cloud.kitelang.syntax.ast.statements;

import cloud.kitelang.syntax.ast.expressions.ValDeclaration;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * VariableStatement
 * : var Identity (, Identity)* Assignment Expression
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public final class ValStatement extends Statement {
    private List<ValDeclaration> declarations;

    private ValStatement(@Nullable List<ValDeclaration> declarations) {
        this.declarations = declarations;
    }

    private ValStatement() {
        this(Collections.emptyList());
    }

    public static Statement valStatement(ValDeclaration... expression) {
        return new ValStatement(List.of(expression));
    }

    public static Statement valStatement(List<ValDeclaration> expression) {
        return new ValStatement(expression);
    }

    public static Statement valStatement() {
        return new ValStatement();
    }

}
