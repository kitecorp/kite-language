package io.zmeu.Frontend.Parser.Statements;

import io.zmeu.Frontend.Parser.Expressions.Expression;
import io.zmeu.Frontend.Parser.Literals.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
public final class SchemaDeclaration extends Statement {
    private Identifier name;
    private Statement body;

    public SchemaDeclaration(Identifier name, @Nullable Statement body) {
        this();
        this.name = name;
        this.body = body;
    }

    public SchemaDeclaration(TypeIdentifier name, @Nullable Statement body) {
        this();
        this.name = name;
        this.body = body;
    }

    public SchemaDeclaration(Identifier name, @Nullable Expression body) {
        this(name, ExpressionStatement.expressionStatement(body));
    }

    public SchemaDeclaration() {
    }

    public static Statement of(Identifier test, Expression body) {
        return new SchemaDeclaration(test, body);
    }

    public static Statement of(Identifier test, Statement body) {
        return new SchemaDeclaration(test, body);
    }

    public static Statement of(Identifier test, int value) {
        return new SchemaDeclaration(test, NumberLiteral.of(value));
    }

    public static Statement of(Identifier test, double value) {
        return new SchemaDeclaration(test, NumberLiteral.of(value));
    }

    public static Statement of() {
        return new SchemaDeclaration();
    }

    public static Statement of(Identifier test, float value) {
        return new SchemaDeclaration(test, NumberLiteral.of(value));
    }

    public static Statement of(Identifier test, String value) {
        return new SchemaDeclaration(test, StringLiteral.of(value));
    }

}
