package io.kite.syntax.ast.expressions;

import io.kite.execution.values.DependencyHolder;
import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.Type;
import io.kite.syntax.annotations.Annotatable;
import io.kite.syntax.ast.statements.VarStatement;
import io.kite.syntax.parser.literals.Identifier;
import io.kite.syntax.parser.literals.TypeIdentifier;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

import static io.kite.syntax.parser.literals.NumberLiteral.number;
import static io.kite.syntax.parser.literals.StringLiteral.string;

@Data
@EqualsAndHashCode(callSuper = true)
public final class VarDeclaration extends Expression implements DependencyHolder, Annotatable {
    private Identifier id;
    private Expression init;
    private TypeIdentifier type;
    private Set<AnnotationDeclaration> annotations;

    public VarDeclaration() {
        this.annotations = Set.of();
    }

    private VarDeclaration(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private VarDeclaration(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private VarDeclaration(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private VarDeclaration(Expression id, TypeIdentifier type, Expression init, AnnotationDeclaration... annotations) {
        this(annotations);
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private VarDeclaration(Expression id) {
        this(id, null);
    }

    public VarDeclaration(AnnotationDeclaration... annotations) {
        this.annotations = Set.of(annotations);
    }

    public static VarDeclaration of(Expression id, Expression init) {
        return new VarDeclaration(id, init);
    }

    public static VarDeclaration var(Expression id, Expression init) {
        return new VarDeclaration(id, init);
    }

    public static VarDeclaration var(String id, Expression init) {
        return new VarDeclaration(Identifier.id(id), init);
    }

    public static VarDeclaration var(String id, int init) {
        return new VarDeclaration(Identifier.id(id), number(init));
    }

    public static VarDeclaration var(String id, String init) {
        return new VarDeclaration(Identifier.id(id), string(init));
    }

    public static VarDeclaration var(String id, double init) {
        return new VarDeclaration(Identifier.id(id), number(init));
    }

    public static VarDeclaration var(String id, TypeIdentifier type) {
        return new VarDeclaration(Identifier.id(id), type);
    }

    public static VarDeclaration var(String id) {
        return new VarDeclaration(Identifier.id(id));
    }

    public static VarDeclaration of(Expression id, TypeIdentifier type, Expression init) {
        return new VarDeclaration(id, type, init);
    }

    public static VarDeclaration of(Expression id, TypeIdentifier type, Expression init, AnnotationDeclaration... annotations) {
        return new VarDeclaration(id, type, init, annotations);
    }

    public static VarDeclaration of(Expression id, TypeIdentifier type) {
        return new VarDeclaration(id, type);
    }

    public static VarDeclaration of(Expression id) {
        return new VarDeclaration(id);
    }

    public static VarDeclaration var(Expression id) {
        if (!(id instanceof Identifier)) {
            throw new IllegalArgumentException("Identifier expected but got: " + id.getClass().getSimpleName());
        }
        return new VarDeclaration(id);
    }

    public static VarDeclaration of(String id) {
        return new VarDeclaration(Identifier.id(id));
    }

    public static VarStatement var(Identifier id, TypeIdentifier type, Expression init) {
        return (VarStatement) VarStatement.varStatement(of(id, type, init));
    }

    public static VarDeclaration var(String id, TypeIdentifier type, Expression init) {
        return VarDeclaration.of(Identifier.id(id), type, init);
    }

    public static VarDeclaration var(String id, TypeIdentifier type, Expression init, AnnotationDeclaration... annotations) {
        return VarDeclaration.of(Identifier.id(id), type, init, annotations);
    }

    public static VarDeclaration var(String id, TypeIdentifier type, int init) {
        return VarDeclaration.of(Identifier.id(id), type, number(init));
    }

    public String name() {
        return id.string();
    }

    public boolean hasInit() {
        return init != null;
    }

    public boolean hasType() {
        return type != null;
    }

    @Override
    public DecoratorType.Target getTarget() {
        return DecoratorType.Target.VAR;
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
