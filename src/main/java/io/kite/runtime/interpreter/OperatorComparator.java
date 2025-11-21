package io.kite.runtime.interpreter;

import io.kite.frontend.parse.literals.*;
import io.kite.frontend.parser.expressions.BinaryExpression;
import io.kite.frontend.parser.expressions.CallExpression;
import io.kite.frontend.parser.expressions.MemberExpression;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OperatorComparator {

    public static @NotNull Object compare(String op, Number ln, Number rn) {
        // if both were ints, do int math → preserve integer result
        if (ln instanceof Integer a && rn instanceof Integer b) {
            return compare(op, a, b);
        }
        // otherwise treat both as doubles
        double a = ln.doubleValue(), b = rn.doubleValue();
        return compare(op, a, b);
    }

    public static @NotNull Object compare(String op, Integer a, Integer b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "%" -> a % b;
            case "==" -> a.equals(b);
            case "!=" -> !a.equals(b);
            case "<" -> a < b;
            case "<=" -> a <= b;
            case ">" -> a > b;
            case ">=" -> a >= b;
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
    }

    public static @NotNull Object compare(String op, Boolean l, Boolean r) {
        return switch (op) {
            case "==" -> l.equals(r);
            case "!=" -> !l.equals(r);
            case "<" -> l.compareTo(r) < 0;
            case "<=" -> l.compareTo(r) <= 0;
            case ">" -> l.compareTo(r) > 0;
            case ">=" -> l.compareTo(r) >= 0;
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
    }

    public static @NotNull Object compare(String op, double a, double b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            case "%" -> a % b;
            case "==" -> a == b;
            case "!=" -> a != b;
            case "<" -> a < b;
            case "<=" -> a <= b;
            case ">" -> a > b;
            case ">=" -> a >= b;
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
    }

    public static @NotNull Object compare(String op, String l, String r) {
        return switch (op) {
            case "+" -> l + r;
            case "==" -> StringUtils.equals(l, r);
            case "!=" -> !StringUtils.equals(l, r);
            case "<" -> StringUtils.compare(l, r) < 0;
            case "<=" -> StringUtils.compare(l, r) <= 0;
            case ">" -> StringUtils.compare(l, r) > 0;
            case ">=" -> StringUtils.compare(l, r) >= 0;
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
    }

    public static @NotNull Object compare(String op, String l, Number r) {
        return switch (op) {
            case "+" -> l + r;
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
    }

    public static @NotNull Object compare(String op, Number l, String r) {
        return switch (op) {
            case "+" -> l + r;
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
    }

    public static List<Class> allowTypes(String op) {
        return switch (op) {
            case "+" -> List.of(
                    NumberLiteral.class,
                    StringLiteral.class,
                    SymbolIdentifier.class,
                    CallExpression.class,
                    BinaryExpression.class,
                    MemberExpression.class
            );
            // allow addition for numbers and string
            case "-", "/", "*", "%" -> List.of(
                    NumberLiteral.class,
                    CallExpression.class,
                    SymbolIdentifier.class,
                    BinaryExpression.class);
            case "==", "!=" -> List.of(
                    StringLiteral.class,
                    CallExpression.class,
                    SymbolIdentifier.class,
                    NumberLiteral.class,
                    BinaryExpression.class,
                    BooleanLiteral.class,
                    ObjectLiteral.class);
            case "<=", "<", ">", ">=" -> List.of(BinaryExpression.class,
                    NumberLiteral.class,
                    CallExpression.class,
                    BooleanLiteral.class,
                    StringLiteral.class,
                    SymbolIdentifier.class);
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
    }
}
