package io.kite.Frontend.Parser.Statements;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.SchemaType;
import io.kite.TypeChecker.Types.Type;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "schema")
public final class SchemaDeclaration extends Statement implements Annotatable {
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
        this.annotations = Set.of();
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

    @Override
    public DecoratorType.Target getTarget() {
        return DecoratorType.Target.SCHEMA;
    }

    @Override
    public Type targetType() {
        return SchemaType.Schema;
    }
}
