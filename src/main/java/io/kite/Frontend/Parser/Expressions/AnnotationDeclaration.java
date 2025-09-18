package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringExclude;

@Data
@EqualsAndHashCode(callSuper = true)
public final class AnnotationDeclaration extends Expression {
    private Identifier name;
    private Identifier value;
    private ArrayExpression args; // for positional args
    private ObjectExpression object; // for named args
    @EqualsAndHashCode.Exclude
    @ToStringExclude
    private Object target;

    public AnnotationDeclaration() {
    }

    private AnnotationDeclaration(Identifier name, ArrayExpression args) {
        this();
        this.name = name;
        this.args = args;
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

    public static AnnotationDeclaration annotation(String name, Identifier args) {
        return new AnnotationDeclaration(name, args);
    }


    public String name() {
        return name.string();
    }

}
