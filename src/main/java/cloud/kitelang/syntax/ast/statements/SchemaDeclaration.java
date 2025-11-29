package cloud.kitelang.syntax.ast.statements;

import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.SchemaType;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.annotations.Annotatable;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.TypeIdentifier;
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
        return SchemaType.INSTANCE;
    }

    @Override
    public boolean hasAnnotations() {
        return annotations != null;
    }
}
