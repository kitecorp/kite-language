package io.kite.execution.decorators;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.execution.Interpreter;
import io.kite.semantics.TypeError;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.parser.literals.Literal;
import io.kite.syntax.parser.literals.NumberLiteral;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class NumberDecorator extends DecoratorInterpreter {
    protected Interpreter interpreter;
    protected SyntaxPrinter printer;

    public NumberDecorator(String name, Interpreter interpreter) {
        super(name);
        this.interpreter = interpreter;
        this.printer = interpreter.getPrinter();
    }

    protected static int compareNumbers(Number a, Number b) {
        return toBigDecimal(a).compareTo(toBigDecimal(b));
    }

    protected static BigDecimal toBigDecimal(Number n) {
        if (n == null) throw new TypeError("null number");
        if (n instanceof BigDecimal bd) return bd;
        if (n instanceof BigInteger bi) return new BigDecimal(bi);
        if (n instanceof Byte || n instanceof Short || n instanceof Integer || n instanceof Long) {
            return BigDecimal.valueOf(n.longValue());
        }
        if (n instanceof Float f) {
            if (f.isNaN() || f.isInfinite()) throw new TypeError("Non-finite float: " + f);
            // Use String ctor to avoid binary FP rounding surprises
            return new BigDecimal(f.toString());
        }
        if (n instanceof Double d) {
            if (d.isNaN() || d.isInfinite()) throw new TypeError("Non-finite double: " + d);
            return new BigDecimal(d.toString());
        }
        // Fallback — best effort
        return new BigDecimal(n.toString());
    }

    protected static void ensureFinite(Number n) {
        if (n instanceof Float f && (f.isNaN() || f.isInfinite())) {
            throw new TypeError("Non-finite float not allowed: " + f);
        }
        if (n instanceof Double d && (d.isNaN() || d.isInfinite())) {
            throw new TypeError("Non-finite double not allowed: " + d);
        }
    }

    /**
     * Extracts the single numeric argument from the annotation.
     * Supports:
     *
     * @min(5)
     * @min(value=5)
     * @min { value: 5 }   // if you also support object-style args
     */
    protected static Number extractSingleNumericArg(AnnotationDeclaration decl) {
        // Adjust to your AST:
        // - decl.getValue() for a lone literal
        // - decl.getArgs() for positional args
        // - decl.getObject() for named args / object literal

        if (decl.getValue() instanceof NumberLiteral n) return n.getValue();

        if (decl.getArgs() != null && !decl.getArgs().isEmpty()) {
            Object first = decl.getArgs().getFirst();
            if (first instanceof Number n) return n;
            if (first instanceof Literal lit && lit.getVal() instanceof Number n2) return n2;
        }

//        if (decl.getObject() instanceof ObjectExpression obj) {
//            var maybe = obj.get("value"); // or whatever your key accessor is
//            if (maybe instanceof Number n) return n;
//            if (maybe instanceof Literal lit && lit.value() instanceof Number n2) return n2;
//        }

        throw new TypeError("@minV requires a numeric argument, e.g. @min(5)");
    }
}
