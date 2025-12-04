package cloud.kitelang.syntax.ast.expressions;

import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.annotations.Annotatable;
import cloud.kitelang.syntax.ast.statements.Statement;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.TypeIdentifier;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

import static cloud.kitelang.syntax.literals.BooleanLiteral.bool;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.StringLiteral.string;

@Data
@EqualsAndHashCode(callSuper = true)
public final class InputDeclaration extends Statement implements Annotatable {
    private Identifier id;
    private Expression init;
    private TypeIdentifier type;
    private Set<AnnotationDeclaration> annotations;
    private boolean sensitive;

    public InputDeclaration() {
        this.annotations = Set.of();
    }

    private InputDeclaration(Expression id, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
    }

    private InputDeclaration(Expression id, TypeIdentifier type) {
        this();
        this.id = (Identifier) id;
        this.type = type;
    }

    private InputDeclaration(Expression id, TypeIdentifier type, Expression init) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
    }

    private InputDeclaration(Expression id, TypeIdentifier type, Expression init, AnnotationDeclaration... annotations) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
        this.annotations = Set.of(annotations);
    }
    private InputDeclaration(Expression id, TypeIdentifier type, Expression init, Set<AnnotationDeclaration> annotations) {
        this();
        this.id = (Identifier) id;
        this.init = init;
        this.type = type;
        this.annotations = annotations;
    }

    private InputDeclaration(Expression id) {
        this(id, null);
    }

    public static InputDeclaration input(Expression id, Expression init) {
        return new InputDeclaration(id, init);
    }

    public static InputDeclaration input(String id, Expression init) {
        return new InputDeclaration(Identifier.id(id), init);
    }

    public static InputDeclaration input(String id, int init) {
        return new InputDeclaration(Identifier.id(id), number(init));
    }

    public static InputDeclaration input(String id, String init) {
        return new InputDeclaration(Identifier.id(id), string(init));
    }

    public static InputDeclaration input(String id, double init) {
        return new InputDeclaration(Identifier.id(id), number(init));
    }

    public static InputDeclaration input(String id, TypeIdentifier type) {
        return new InputDeclaration(Identifier.id(id), type);
    }

    public static InputDeclaration input(String id) {
        return new InputDeclaration(Identifier.id(id));
    }

    public static InputDeclaration input(Expression id, TypeIdentifier type, Expression init) {
        return new InputDeclaration(id, type, init);
    }

    public static InputDeclaration input(Expression id, TypeIdentifier type) {
        return new InputDeclaration(id, type);
    }

    public static InputDeclaration input(Expression id) {
        if (!(id instanceof Identifier)) {
            throw new IllegalArgumentException("Identifier expected but got: " + id.getClass().getSimpleName());
        }
        return new InputDeclaration(id);
    }

    public static InputDeclaration input(String id, TypeIdentifier type, Expression init) {
        return InputDeclaration.input(Identifier.id(id), type, init);
    }

    public static InputDeclaration input(String id, TypeIdentifier type, int init) {
        return InputDeclaration.input(Identifier.id(id), type, number(init));
    }
    public static InputDeclaration input(String id, TypeIdentifier type, int init, AnnotationDeclaration... annotations) {
        return new InputDeclaration(Identifier.id(id), type, number(init), annotations);
    }

    public static InputDeclaration input(String id, TypeIdentifier type, String init) {
        return InputDeclaration.input(Identifier.id(id), type, string(init));
    }

    public static InputDeclaration input(String id, TypeIdentifier type, boolean init) {
        return InputDeclaration.input(Identifier.id(id), type, bool(init));
    }

    public static InputDeclaration input(Identifier name, TypeIdentifier type, Expression body, Set<AnnotationDeclaration> annotations) {
        return new InputDeclaration(name, type, body, annotations);
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
        return DecoratorType.Target.INPUT;
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
