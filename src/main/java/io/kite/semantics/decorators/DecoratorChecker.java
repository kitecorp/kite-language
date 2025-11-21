package io.kite.semantics.decorators;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.semantics.TypeChecker;
import io.kite.semantics.TypeError;
import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.SystemType;
import io.kite.semantics.types.Type;
import io.kite.syntax.annotations.Annotatable;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.literals.*;
import lombok.Data;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

@Data
public abstract class DecoratorChecker {
    protected final SyntaxPrinter printer;
    protected final TypeChecker checker;
    private final String name;
    private final DecoratorType type;
    /**
     * the types that we apply the annotation on. ex: input <type> name
     */
    private final Set<SystemType> allowedOn;

    public DecoratorChecker(TypeChecker checker, String name, DecoratorType type, Set<SystemType> allowedOn) {
        this.printer = checker.getPrinter();
        this.checker = checker;
        this.name = name;
        this.type = type;
        this.allowedOn = allowedOn;
    }

    protected abstract Object validate(AnnotationDeclaration declaration, List<Object> args);

    public Object validate(AnnotationDeclaration declaration, Object... args) {
//        if (doesNotHaveArguments(declaration)) {
//            var message = Ansi.ansi()
//                    .a(printer.visit(declaration))
//                    .a(" is missing arguments")
//                    .toString();
//            throw new TypeError(message);
//        }

//        throwIfNotAllowedOnType(declaration.getTarget().targetType());


        var res = validate(declaration, List.of(args));

        if (!isOnValidTarget(declaration.getTarget())) {
            var ansi = Ansi.ansi().fgYellow().a("@").a(declaration.name()).reset()
                    .a(" can only be used on: ")
                    .fgMagenta()
                    .a(targetString()).reset();
            throw new TypeError(ansi.toString());
        }

        return res;
    }

    public boolean onTargetEvaluated(AnnotationDeclaration declaration) {
        return false;
    }

    protected boolean isAllowedOn(TypeIdentifier type) {
        return type instanceof ArrayTypeIdentifier || isAllowedOn(type.getType());
    }

    protected boolean isAllowedOn(Type type) {
        return type.getKind() == SystemType.ARRAY || isAllowedOn(type.getKind());
    }

    protected boolean isAllowedOn(SystemType type) {
        return allowedOn.contains(type);
    }

    public Set<DecoratorType.Target> targets() {
        return type.getTargets();
    }

    public String targetString() {
        return type.targetString();
    }

    protected Number validateNumber(AnnotationDeclaration declaration, int min, int max) {
        if (declaration.getArgs() != null && !declaration.getArgs().isEmpty()) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(name).a("([..])")
                    .reset()
                    .a(" does not accept arrays as arguments")
                    .toString();
            throw new TypeError(message);
        } else if (declaration.getObject() != null) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(name).a("({..})")
                    .reset()
                    .a(" does not accept objects as arguments")
                    .toString();
            throw new TypeError(message);
        } else if (declaration.getValue() == null) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(name)
                    .reset()
                    .a(" requires a number as argument")
                    .toString();
            throw new TypeError(message);
        }

        var value = declaration.getValue();
        var number = getValue(value);
        var longValue = number.longValue();
        if (longValue < min) {
            String decorator = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(name).a("(").a(number).a(")")
                    .reset()
                    .a(" must have a value greater than ").a(min).a(", got: ").fgYellow().a(number)
                    .reset()
                    .toString();
            throw new TypeError(decorator);
        } else if (longValue >= max) {
            String decorator = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(name).a("(").a(number).a(")")
                    .reset()
                    .a(" must have a value less than ").a(min).a(", got: ").fgYellow().a(number)
                    .reset()
                    .toString();
            throw new TypeError(decorator);
        }
        return longValue;
    }

    private Number getValue(Object value) {
        return switch (value) {
            case NumberLiteral literal -> literal.getValue();
            case StringLiteral literal ->
                    throw new TypeError(Ansi.ansi().fgYellow().a("@").a(name).a("(\"").a(literal.getValue()).a("\")").reset().a(" is invalid. Only numbers are valid arguments").toString());
            case BooleanLiteral literal ->
                    throw new TypeError(Ansi.ansi().fgYellow().a("@").a(name).a("(").a(literal.isValue()).a(")").reset().a(" is invalid. Only numbers are valid arguments").toString());
            default ->
                    throw new TypeError(Ansi.ansi().fgYellow().a(name).a("(").a(value).a(")").reset().a(" is invalid").toString());
        };
    }

    public boolean isOnValidTarget(Annotatable target) {
        return targets().contains(target.getTarget());
    }

    protected boolean doesNotHaveArguments(AnnotationDeclaration declaration) {
        return type.getParams().isEmpty() &&
               (declaration.getValue() != null
                || declaration.getObject() != null
                || declaration.getArgs() != null && !declaration.getArgs().isEmpty()
                || declaration.getNamedArgs() != null && declaration.getNamedArgs().isEmpty());
    }
}
