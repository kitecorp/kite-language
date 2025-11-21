package io.kite.frontend.parser.expressions;

import io.kite.frontend.annotations.Annotatable;
import io.kite.frontend.parse.literals.Identifier;
import io.kite.frontend.parse.literals.TypeIdentifier;
import io.kite.frontend.parser.statements.Statement;
import io.kite.runtime.values.DependencyHolder;
import io.kite.typechecker.types.DecoratorType;
import io.kite.typechecker.types.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

import static io.kite.frontend.parse.literals.BooleanLiteral.bool;
import static io.kite.frontend.parse.literals.NumberLiteral.number;
import static io.kite.frontend.parse.literals.StringLiteral.string;

@Data
@EqualsAndHashCode(callSuper = true)
public final class OutputDeclaration extends Statement implements DependencyHolder, Annotatable {
    private Identifier id;
    private Expression init;
    private Object resolvedValue;
    private TypeIdentifier type;
    private Set<AnnotationDeclaration> annotations;
    private boolean sensitive;

    public OutputDeclaration() {
        annotations = Set.of();
    }

    private OutputDeclaration(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private OutputDeclaration(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private OutputDeclaration(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private OutputDeclaration(Expression id) {
        this(id, null);
    }

    public OutputDeclaration(Expression id, TypeIdentifier type, Expression init, Set<AnnotationDeclaration> annotations) {
        this(id, type, init);
        this.annotations = annotations;
    }

    public static OutputDeclaration output(Expression id, Expression init) {
        return new OutputDeclaration(id, init);
    }

    public static OutputDeclaration output(String id, Expression init) {
        return new OutputDeclaration(Identifier.id(id), init);
    }

    public static OutputDeclaration output(String id, int init) {
        return new OutputDeclaration(Identifier.id(id), number(init));
    }

    public static OutputDeclaration output(String id, String init) {
        return new OutputDeclaration(Identifier.id(id), string(init));
    }

    public static OutputDeclaration output(String id, double init) {
        return new OutputDeclaration(Identifier.id(id), number(init));
    }

    public static OutputDeclaration output(String id, TypeIdentifier type) {
        return new OutputDeclaration(Identifier.id(id), type);
    }

    public static OutputDeclaration output(String id) {
        return new OutputDeclaration(Identifier.id(id));
    }

    public static OutputDeclaration output(Expression id, TypeIdentifier type, Expression init) {
        return new OutputDeclaration(id, type, init);
    }

    public static OutputDeclaration output(Expression id, TypeIdentifier type, Expression init, Set<AnnotationDeclaration> annotations) {
        return new OutputDeclaration(id, type, init, annotations);
    }

    public static OutputDeclaration output(Expression id, TypeIdentifier type) {
        return new OutputDeclaration(id, type);
    }

    public static OutputDeclaration output(Expression id) {
        if (!(id instanceof Identifier)) {
            throw new IllegalArgumentException("Identifier expected but got: " + id.getClass().getSimpleName());
        }
        return new OutputDeclaration(id);
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, Expression init) {
        return OutputDeclaration.output(Identifier.id(id), type, init);
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, int init) {
        return OutputDeclaration.output(Identifier.id(id), type, number(init));
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, String init) {
        return OutputDeclaration.output(Identifier.id(id), type, string(init));
    }

    public static OutputDeclaration output(String id, TypeIdentifier type, boolean init) {
        return OutputDeclaration.output(Identifier.id(id), type, bool(init));
    }

    public Object resolvedValue() {
        return resolvedValue != null ? resolvedValue : init;
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
        return DecoratorType.Target.OUTPUT;
    }

    @Override
    public Type targetType() {
        return type.getType();
    }

    @Override
    public boolean hasAnnotations() {
        return annotations != null;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public boolean isSensitive() {
        return sensitive;
    }
}
