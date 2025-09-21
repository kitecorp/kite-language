package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.TypeChecker.Types.DecoratorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(staticName = "annotation")
public final class AnnotationDeclaration extends Expression {
    private Identifier name;
    private Object value;
    private ArrayExpression args; // for positional args [1,2,3]; ['dev','env']
    private ObjectExpression object; // for named args
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Annotatable target;

    public AnnotationDeclaration() {
    }

    private AnnotationDeclaration(Identifier name, ArrayExpression args) {
        this();
        this.name = name;
        this.args = args;
    }

    private AnnotationDeclaration(String name, Object value) {
        this();
        this.name = Identifier.id(name);
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

    private AnnotationDeclaration(Identifier name, Object value) {
        this();
        this.name = name;
        this.value = value;
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

    public static AnnotationDeclaration annotation(String name, Object value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(String name, ObjectExpression value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(String name, ArrayExpression value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(Identifier name, Object value) {
        return new AnnotationDeclaration(name, value);
    }

    public static AnnotationDeclaration annotation(String name, Identifier args) {
        return new AnnotationDeclaration(name, args);
    }


    public String name() {
        return name.string();
    }

    public DecoratorType.Target targetType() {
        return target.getTarget();
    }

}
