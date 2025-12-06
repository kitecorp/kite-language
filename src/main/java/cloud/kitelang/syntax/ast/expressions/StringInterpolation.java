package cloud.kitelang.syntax.ast.expressions;

import cloud.kitelang.syntax.literals.SymbolIdentifier;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an interpolated string like "Hello ${name}, you have ${count} messages".
 * <p>
 * Contains a list of parts that alternate between text segments and expressions.
 * Unlike StringLiteral which stores variable names as strings, this node stores
 * the actual parsed Expression nodes, enabling proper type checking.
 * <p>
 * Example: "Hello ${name}" becomes:
 * - Text("Hello ")
 * - Expr(Identifier("name"))
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class StringInterpolation extends Expression {

    private final List<Part> parts;

    public StringInterpolation() {
        this.parts = new ArrayList<>();
    }

    public StringInterpolation(List<Part> parts) {
        this.parts = parts;
    }

    /**
     * Factory method to create a StringInterpolation from parts.
     */
    public static StringInterpolation interpolation(List<Part> parts) {
        return new StringInterpolation(parts);
    }

    /**
     * Factory method to create a StringInterpolation from parts.
     */
    public static StringInterpolation interpolation(String string, Expression... expressions) {
        var text = new StringInterpolation();
        text.addText(string);
        for (var it : expressions) {
            text.addExpression(it);
        }
        return text;
    }

    public static StringInterpolation interpolation(String string, String... expressions) {
        var text = new StringInterpolation();
        text.addText(string);
        for (var it : expressions) {
            text.addExpression(SymbolIdentifier.id(it));
        }
        return text;
    }

    public void addText(String text) {
        parts.add(new Text(text));
    }

    public void addExpression(Expression expression) {
        parts.add(new Expr(expression));
    }

    /**
     * Check if this interpolation has any expression parts.
     * If it only has text parts, it could be simplified to a StringLiteral.
     */
    public boolean hasExpressions() {
        return parts.stream().anyMatch(p -> p instanceof Expr);
    }

    /**
     * A part of an interpolated string - either literal text or an expression.
     */
    public sealed interface Part permits Text, Expr {
    }

    /**
     * A literal text segment within the interpolated string.
     */
    public record Text(String value) implements Part {
    }

    /**
     * An interpolated expression (the content inside ${...}).
     */
    public record Expr(Expression expression) implements Part {
    }
}
