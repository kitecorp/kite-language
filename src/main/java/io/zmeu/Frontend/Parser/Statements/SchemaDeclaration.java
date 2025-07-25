package io.zmeu.Frontend.Parser.Statements;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.TypeIdentifier;
import io.zmeu.Frontend.Parser.Expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static io.zmeu.Frontend.Parse.Literals.NumberLiteral.number;

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

    public record SchemaProperty(Identifier name, TypeIdentifier type, Expression defaultValue) {

        public SchemaProperty(Identifier name, String type) {
            this(name, TypeIdentifier.type(type));
        }

        public SchemaProperty(Identifier name, TypeIdentifier type) {
            this(name, type, null);
        }

        public SchemaProperty(Identifier name, String type, Expression defaultValue) {
            this(name, TypeIdentifier.type(type), defaultValue);
        }


        public static SchemaProperty schemaProperty(Identifier name, String type) {
            return new SchemaProperty(name, type);
        }

        public static SchemaProperty schemaProperty(Identifier name, TypeIdentifier type) {
            return new SchemaProperty(name, type);
        }

        public static SchemaProperty schemaProperty(Identifier name, TypeIdentifier type, Expression defaultValue) {
            return new SchemaProperty(name, type, defaultValue);
        }

        public static SchemaProperty schemaProperty(String name, TypeIdentifier type, Expression defaultValue) {
            return schemaProperty(Identifier.id(name), type, defaultValue);
        }

        public static SchemaProperty schemaProperty(String name, TypeIdentifier type, int defaultValue) {
            return schemaProperty(Identifier.id(name), type, number(defaultValue));
        }

        public static SchemaProperty schemaProperty(Identifier name, String type, Expression defaultValue) {
            return new SchemaProperty(name, type, defaultValue);
        }
    }
}
