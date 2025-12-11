package cloud.kitelang.syntax.ast.statements;

import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.StructType;
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

/**
 * AST node for struct declarations.
 * Structs are nominal typed data containers with explicit property types.
 *
 * Block style:
 *   struct Point {
 *       number x
 *       number y = 0
 *   }
 *
 * Inline style:
 *   struct Point { number x, number y = 0 }
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "struct")
public final class StructDeclaration extends Statement implements Annotatable {
    private Identifier name;
    private List<StructProperty> properties;
    private Set<AnnotationDeclaration> annotations;

    public StructDeclaration(Identifier name, @Nullable List<StructProperty> properties) {
        this();
        this.name = name;
        this.properties = properties;
    }

    public StructDeclaration(TypeIdentifier name, @Nullable List<StructProperty> properties) {
        this();
        this.name = name;
        this.properties = properties;
    }

    public StructDeclaration(TypeIdentifier name, @Nullable StructProperty... properties) {
        this(name, List.of(properties));
    }

    public StructDeclaration(TypeIdentifier name, @Nullable List<StructProperty> properties, Set<AnnotationDeclaration> annotations) {
        this(name, properties);
        this.annotations = annotations;
    }

    public StructDeclaration() {
        this.annotations = Set.of();
    }

    public static Statement struct(Identifier name, @Nullable StructProperty... properties) {
        return new StructDeclaration(name, List.of(properties));
    }

    public static Statement struct(Identifier name, @Nullable List<StructProperty> properties) {
        return new StructDeclaration(name, properties);
    }

    public static Statement struct(Identifier name, @Nullable List<StructProperty> properties, AnnotationDeclaration... annotations) {
        return new StructDeclaration(name, properties, Set.of(annotations));
    }

    @Override
    public DecoratorType.Target getTarget() {
        return DecoratorType.Target.STRUCT;
    }

    @Override
    public Type targetType() {
        return StructType.INSTANCE;
    }

    @Override
    public boolean hasAnnotations() {
        return annotations != null;
    }
}
