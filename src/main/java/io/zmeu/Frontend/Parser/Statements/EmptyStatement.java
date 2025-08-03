package io.kite.Frontend.Parser.Statements;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * EmptyStatement
 * : '\n'
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class EmptyStatement extends Statement {

    public EmptyStatement() {
    }

    public static Statement of() {
        return new EmptyStatement();
    }

}
