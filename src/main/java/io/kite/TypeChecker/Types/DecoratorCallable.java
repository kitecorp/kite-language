package io.kite.TypeChecker.Types;

import io.kite.Frontend.Parse.Literals.BooleanLiteral;
import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.TypeError;
import lombok.Data;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

@Data
public abstract class DecoratorCallable {
    private final String name;
    private final DecoratorType type;

    public DecoratorCallable(String name, DecoratorType type) {
        this.name = name;
        this.type = type;
    }

    public abstract Object validate(AnnotationDeclaration declaration, List<Object> args);


    public Object validate(AnnotationDeclaration declaration, Object... args) {
        return validate(declaration, List.of(args));
    }

    public int arity() {
        return 0;
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
        var number = switch (value) {
            case NumberLiteral literal -> literal.getValue();
            case StringLiteral literal -> throw new TypeError(Ansi.ansi().fgYellow().a("@").a(name).a("(\"").a(literal.getValue()).a("\")").reset().a(" is invalid. Only numbers are valid arguments").toString());
            case BooleanLiteral literal -> throw new TypeError(Ansi.ansi().fgYellow().a("@").a(name).a("(").a(literal.isValue()).a(")").reset().a(" is invalid. Only numbers are valid arguments").toString());
            default -> throw new TypeError(Ansi.ansi().fgYellow().a(name).a("(").a(value).a(")").reset().a(" is invalid").toString());
        };
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

}
