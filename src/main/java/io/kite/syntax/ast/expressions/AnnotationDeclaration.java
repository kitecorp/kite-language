package io.kite.syntax.ast.expressions;

import io.kite.semantics.types.DecoratorType;
import io.kite.syntax.annotations.Annotatable;
import io.kite.syntax.parser.literals.Identifier;
import io.kite.syntax.parser.literals.StringLiteral;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true, of = "name")
@AllArgsConstructor(staticName = "annotation")
public final class AnnotationDeclaration extends Expression {
    private Identifier name;
    private Object value;
    private ArrayExpression args; // for positional args [1,2,3]; ['dev','env']
    private ObjectExpression object; // for object args
    private Map<String, Expression> namedArgs;
    @ToString.Exclude
    private Annotatable target;

    public AnnotationDeclaration() {
    }

    private AnnotationDeclaration(Identifier name, ArrayExpression args) {
        this();
        this.name = name;
        this.args = args;
    }

    private AnnotationDeclaration(String name, Expression value) {
        this();
        this.name = Identifier.id(name);
        this.value = value;
    }

    private AnnotationDeclaration(Identifier name, Expression value) {
        this();
        this.name = name;
        this.value = value;
    }

    private AnnotationDeclaration(String name, ObjectExpression objectExpression) {
        this();
        this.name = Identifier.id(name);
        this.object = objectExpression;
    }

    private AnnotationDeclaration(String name, ArrayExpression value) {
        this();
        this.name = Identifier.id(name);
        this.args = value;
    }


    private AnnotationDeclaration(Identifier name, Map<String, Expression> namedArgs) {
        this();
        this.name = name;
        this.namedArgs = namedArgs;
    }

    private AnnotationDeclaration(Identifier name) {
        this();
        this.name = name;
    }

    private AnnotationDeclaration(Identifier name, ObjectExpression args) {
        this();
        this.name = name;
        this.object = args;
    }

    private AnnotationDeclaration(Identifier name, Identifier args) {
        this();
        this.name = name;
        this.value = args;
    }

    private AnnotationDeclaration(String name, Identifier args) {
        this(Identifier.id(name), args);
    }

    public static AnnotationDeclaration annotation(Identifier name, ArrayExpression args) {
        return new AnnotationDeclaration(name, args);
    }

    public static AnnotationDeclaration annotation(Identifier name) {
        return new AnnotationDeclaration(name);
    }

    public static AnnotationDeclaration annotation(String name) {
        return new AnnotationDeclaration(Identifier.id(name));
    }

    public static AnnotationDeclaration annotation(Identifier name, ObjectExpression args) {
        return new AnnotationDeclaration(name, args);
    }

    public static AnnotationDeclaration annotation(Identifier name, Identifier args) {
        return new AnnotationDeclaration(name, args);
    }

    public static AnnotationDeclaration annotation(String name, String args) {
        return new AnnotationDeclaration(name, Identifier.id(args));
    }

    public static AnnotationDeclaration annotation(String name, Expression value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(Identifier name, Expression value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(String name, ObjectExpression value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(String name, ArrayExpression value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(Identifier name, Map<String, Expression> value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(String name, Map<String, Expression> value) {
        return new AnnotationDeclaration(Identifier.id(name), value);
    }

    public static AnnotationDeclaration annotation(String name, Identifier args) {
        return new AnnotationDeclaration(name, args);
    }


    public String name() {
        return name.string();
    }

    public DecoratorType.Target target() {
        return target.getTarget();
    }

    public String getStringArg(String key) {
        var obj = namedArgs.get(key);
        return switch (obj) {
            case StringLiteral lit -> lit.getValue();
            case null, default -> null;
        };
    }

    public boolean hasArgs() {
        return getArgs() != null && !getArgs().isEmpty();
    }
}
