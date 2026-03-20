package cloud.kitelang.execution.decorators;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.values.CloudPropertyObserver;
import cloud.kitelang.execution.values.DeferredFunctionCall;
import cloud.kitelang.execution.values.DeferredValue;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.literals.Literal;
import cloud.kitelang.syntax.literals.NumberLiteral;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Base class for decorators that work with numeric values.
 * Provides transparent handling of deferred cloud values - subclasses
 * don't need to know about DeferredValue.
 */
@Slf4j
public abstract class NumberDecorator extends DecoratorInterpreter {
    protected Interpreter interpreter;
    protected SyntaxPrinter printer;

    public NumberDecorator(String name, Interpreter interpreter) {
        super(name);
        this.interpreter = interpreter;
        this.printer = interpreter.getPrinter();
    }

    /**
     * Evaluate the decorator's value expression, handling deferred values transparently.
     * If the value is deferred (depends on @cloud property), registers an observer
     * and marks the resource as cloud-pending.
     *
     * @param declaration the annotation declaration
     * @return the evaluated value, or null if deferred
     */
    protected Object evaluateValueWithDeferredHandling(AnnotationDeclaration declaration) {
        var value = declaration.getValue();
        if (!(value instanceof Expression expr)) {
            return value;
        }

        var evaluated = interpreter.visit(expr);

        // Handle deferred cloud property
        if (evaluated instanceof DeferredValue deferred) {
            return handleDeferred(declaration, deferred);
        }

        // Handle deferred function call (e.g., length(resource.cloudProp))
        if (evaluated instanceof DeferredFunctionCall deferredCall) {
            return handleDeferred(declaration, deferredCall.deferredValue());
        }

        return evaluated;
    }

    /**
     * Handle deferred value by registering a CloudPropertyObserver.
     * The observer will re-evaluate this decorator after apply when cloud values are available.
     *
     * @param declaration the annotation declaration
     * @param deferred    the deferred value
     * @return null (evaluation is deferred to apply phase)
     */
    protected Object handleDeferred(AnnotationDeclaration declaration, DeferredValue deferred) {
        var observer = new CloudPropertyObserver(declaration, deferred, this);
        interpreter.getCloudObservable().addObserver(deferred.dependencyName(), observer);

        // Mark target resource as cloud-pending
        if (declaration.getTarget() instanceof ResourceStatement rs) {
            rs.setCloudPending(true);
            rs.setCounted(true); // Prevent normal evaluation
        }

        log.info("Deferred @{} for '{}' - depends on {}.{}",
                getName(), observer.getTemplateName(),
                deferred.dependencyName(), deferred.propertyPath());

        return null; // Will be evaluated during apply
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
