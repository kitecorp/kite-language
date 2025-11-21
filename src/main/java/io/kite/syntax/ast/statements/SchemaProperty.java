package io.kite.syntax.ast.statements;

import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.Type;
import io.kite.syntax.annotations.Annotatable;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.literals.Identifier;
import io.kite.syntax.literals.NumberLiteral;
import io.kite.syntax.literals.TypeIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@AllArgsConstructor(staticName = "schemaProperty")
@Data
public class SchemaProperty implements Annotatable {
    private TypeIdentifier type;
    private Identifier identifier;
    private Expression init;
    private Set<AnnotationDeclaration> annotations;


    public SchemaProperty(TypeIdentifier typeIdentifier, Identifier identifier, Expression init) {
        this(typeIdentifier, identifier, init, Set.of());
    }

    public static SchemaProperty schemaProperty(TypeIdentifier typeIdentifier,
                                                Identifier identifier,
                                                Expression init,
                                                AnnotationDeclaration... annotation) {
        return new SchemaProperty(typeIdentifier, identifier, init, Set.of(annotation));
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

    public Expression init() {
        return init;
    }

    public TypeIdentifier type() {
        return type;
    }

    @Override
    public DecoratorType.Target getTarget() {
        return DecoratorType.Target.SCHEMA_PROPERTY;
    }

    @Override
    public Type targetType() {
        return type.getType();
    }

    @Override
    public boolean hasAnnotations() {
        return annotations != null;
    }
}
