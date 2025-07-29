package io.zmeu.Frontend.Parser.Statements;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.TypeIdentifier;
import io.zmeu.Frontend.Parser.Expressions.VarDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public final class SchemaDeclaration extends Statement {
    private Identifier name;
    private List<SchemaProperty> properties;

    public SchemaDeclaration(Identifier name, @Nullable List<SchemaProperty> properties) {
        this();
        this.name = name;
        this.properties = properties;
    }

    public SchemaDeclaration(TypeIdentifier name, @Nullable List<SchemaProperty> properties) {
        this();
        this.name = name;
        this.properties = properties;
    }

    public SchemaDeclaration(TypeIdentifier name, @Nullable SchemaProperty... properties) {
        this(name, List.of(properties));
    }

    public SchemaDeclaration() {
    }

    public static Statement schema(Identifier name, @Nullable SchemaProperty... properties) {
        return new SchemaDeclaration(name, List.of(properties));
    }

    public static Statement schema(Identifier name, @Nullable List<SchemaProperty> properties) {
        return new SchemaDeclaration(name, properties);
    }

    public record SchemaProperty(VarDeclaration declaration) {
        public static SchemaProperty schemaProperty(VarDeclaration declaration) {
            return new SchemaProperty(declaration);
        }
    }
}
