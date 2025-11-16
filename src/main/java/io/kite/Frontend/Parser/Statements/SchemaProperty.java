package io.kite.Frontend.Parser.Statements;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.Type;
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
