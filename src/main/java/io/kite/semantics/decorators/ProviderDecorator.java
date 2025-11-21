package io.kite.semantics.decorators;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.semantics.TypeChecker;
import io.kite.semantics.TypeError;
import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.SystemType;
import io.kite.semantics.types.ValueType;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.ComponentStatement;
import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.literals.Identifier;
import io.kite.syntax.literals.StringLiteral;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.semantics.types.DecoratorType.decorator;

public class ProviderDecorator extends DecoratorChecker {
    public static final String NAME = "provider";
    private final TypeChecker checker;
    private final SyntaxPrinter printer;

    public ProviderDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(ValueType.String),
                        Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)
                ), Set.of()
        );
        this.checker = checker;
        this.printer = checker.getPrinter();
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        if (doesNotHaveArguments(declaration)) {
            var message = Ansi.ansi()
                    .a(printer.visit(declaration))
                    .a(" is missing arguments")
                    .toString();
            throw new TypeError(message);
        }

        var value = declaration.getValue();
        if (value != null) {
            validateValue(declaration, value);
        } else if (declaration.hasArgs()) {
            for (Expression item : declaration.getArgs().getItems()) {
                if (item instanceof StringLiteral literal) {
                    validateValue(declaration, literal);
                } else {
                    throw new TypeError("%s has invalid argument `%s`".formatted(printer.visit(declaration), printer.visit(item)));
                }
            }
        }

        if (declaration.getTarget() instanceof ComponentStatement statement) {
            if (statement.isDefinition()) {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" cannnot be applied to a component definition")
                        .toString();
                throw new TypeError(message);
            }
        }


        return null;
    }

    private void validateValue(AnnotationDeclaration declaration, Object value) {
        if (value instanceof StringLiteral literal) {
            if (StringUtils.isBlank(literal.getValue())) {
                throw new TypeError("@provider must have a non-empty string as argument or an array of strings");
            }
        } else if (value instanceof Identifier identifier) {
            var res = checker.visit(identifier);
            if (res.getKind() != SystemType.STRING) {
                throw new TypeError("@provider must have a string as argument or an array of strings");
            }
        } else {
            throw new TypeError("%s has invalid argument `%s`".formatted(printer.visit(declaration), printer.visit(value)));
        }
    }


    @Override
    public boolean doesNotHaveArguments(AnnotationDeclaration declaration) {
        return declaration.getValue() == null
               && (declaration.getArgs() == null || declaration.getArgs().isEmpty());
    }

}
