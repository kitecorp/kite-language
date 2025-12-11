package cloud.kitelang.syntax.ast.statements;

import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.annotations.Annotatable;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.NumberLiteral;
import cloud.kitelang.syntax.literals.TypeIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * AST node for struct properties.
 * A struct property has a type, name, optional default value, and optional annotations.
 */
@AllArgsConstructor(staticName = "structProperty")
@Data
public class StructProperty implements Annotatable {

    private TypeIdentifier type;
    private Identifier identifier;
    private Expression init;
    private Set<AnnotationDeclaration> annotations;

    public StructProperty(TypeIdentifier typeIdentifier, Identifier identifier, Expression init) {
        this(typeIdentifier, identifier, init, Set.of());
    }

    public static StructProperty structProperty(TypeIdentifier typeIdentifier,
                                                Identifier identifier,
                                                Expression init,
                                                AnnotationDeclaration... annotation) {
        return new StructProperty(typeIdentifier, identifier, init, Set.of(annotation));
    }

    public static StructProperty structProperty(TypeIdentifier typeIdentifier, String identifier,
                                                Expression init, AnnotationDeclaration... annotation) {
        return new StructProperty(typeIdentifier, Identifier.id(identifier), init, Set.of(annotation));
    }

    public static StructProperty structProperty(TypeIdentifier typeIdentifier, String identifier, int init,
                                                AnnotationDeclaration... annotation) {
        return new StructProperty(typeIdentifier, Identifier.id(identifier), NumberLiteral.number(init), Set.of(annotation));
    }

    public static StructProperty structProperty(TypeIdentifier typeIdentifier, String identifier, int init) {
        return new StructProperty(typeIdentifier, Identifier.id(identifier), NumberLiteral.number(init), Set.of());
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

    public Expression init() {
        return init;
    }

    public TypeIdentifier type() {
        return type;
    }

    @Override
    public DecoratorType.Target getTarget() {
        return DecoratorType.Target.STRUCT_PROPERTY;
    }

    @Override
    public Type targetType() {
        return type.getType();
    }

    @Override
    public boolean hasAnnotations() {
        return annotations != null;
    }

    /**
     * Returns true if this property has the @cloud annotation,
     * indicating it's set by the cloud provider after apply.
     */
    public boolean isCloudGenerated() {
        return annotations != null && annotations.stream()
                .anyMatch(a -> "cloud".equals(a.name()));
    }
}
