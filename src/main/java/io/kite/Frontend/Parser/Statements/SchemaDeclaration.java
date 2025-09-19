package io.kite.Frontend.Parser.Statements;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "schema")
public final class SchemaDeclaration extends Statement {
    private Identifier name;
    private List<SchemaProperty> properties;
    private Set<AnnotationDeclaration> annotations;

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

    public SchemaDeclaration(TypeIdentifier name, @Nullable List<SchemaProperty> properties, Set<AnnotationDeclaration> annotations) {
        this(name, properties);
        this.annotations = annotations;
    }

    public SchemaDeclaration() {
    }

    public static Statement schema(Identifier name, @Nullable SchemaProperty... properties) {
        return new SchemaDeclaration(name, List.of(properties));
    }

    public static Statement schema(Identifier name, @Nullable List<SchemaProperty> properties) {
        return new SchemaDeclaration(name, properties);
    }

    public static Statement schema(Identifier name, @Nullable List<SchemaProperty> properties, AnnotationDeclaration... annotations) {
        return new SchemaDeclaration(name, properties, Set.of(annotations));
    }

    public record SchemaProperty(TypeIdentifier type,
                                 Identifier identifier,
                                 Expression init,
                                 Set<AnnotationDeclaration> annotation) {


        public SchemaProperty(TypeIdentifier typeIdentifier, Identifier identifier, Expression init) {
            this(typeIdentifier, identifier, init, Set.of());
        }

        public static SchemaProperty schemaProperty(TypeIdentifier typeIdentifier,
                                                    Identifier identifier,
                                                    Expression init,
                                                    Set<AnnotationDeclaration> annotation) {
            return new SchemaProperty(typeIdentifier, identifier, init, annotation);
        }

        public static SchemaProperty schemaProperty(TypeIdentifier typeIdentifier,
                                                    Identifier identifier,
                                                    Expression init,
                                                    AnnotationDeclaration... annotation) {
            return new SchemaProperty(typeIdentifier, identifier, init, Set.of(annotation));
        }

        public static SchemaProperty schemaProperty(TypeIdentifier typeIdentifier, String identifier,
                                                    Expression init, Set<AnnotationDeclaration> annotation) {
            return new SchemaProperty(typeIdentifier, Identifier.id(identifier), init, annotation);
        }

        public static SchemaProperty schemaProperty(TypeIdentifier typeIdentifier, String identifier,
                                                    Expression init, AnnotationDeclaration... annotation) {
            return new SchemaProperty(typeIdentifier, Identifier.id(identifier), init, Set.of(annotation));
        }

        public static SchemaProperty schemaProperty(TypeIdentifier typeIdentifier, String identifier, int init,
                                                    AnnotationDeclaration... annotation) {
            return new SchemaProperty(typeIdentifier, Identifier.id(identifier), NumberLiteral.number(init), Set.of(annotation));
        }

        public static SchemaProperty schemaProperty(TypeIdentifier typeIdentifier, String identifier, int init) {
            return new SchemaProperty(typeIdentifier, Identifier.id(identifier), NumberLiteral.number(init));
        }

        public String name() {
            return identifier.string();
        }

        public boolean hasInit() {
            return init != null;
        }

        public boolean hasType() {
            return type != null;
        }
    }
}
