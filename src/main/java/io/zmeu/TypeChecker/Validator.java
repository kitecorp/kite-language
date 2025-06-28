package io.zmeu.TypeChecker;

import io.zmeu.Frontend.Parser.Expressions.Expression;
import io.zmeu.Frontend.Parser.Statements.Statement;
import io.zmeu.TypeChecker.Types.Type;
import io.zmeu.TypeChecker.Types.ValueType;
import io.zmeu.Visitors.LanguageAstPrinter;

import java.util.Objects;

public class Validator {
    private final LanguageAstPrinter printer;

    public Validator(LanguageAstPrinter printer) {
        this.printer = printer;
    }

    public Type expect(Type actualType, Type expectedType, Expression expectedVal) {
        if (actualType == ValueType.Null) {
            return expectedType;
        }
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (!Objects.equals(actualType, expectedType)) {
            // only evaluate printing if we need to
            String string = "Expected type " + expectedType + " but got " + actualType + " in expression: " + printer.visit(expectedVal);
            throw new TypeError(string);
        }
        return actualType;
    }

    public Type expect(Type actualType, Type expectedType, Statement actualVal, Statement expectedVal) {
        if (actualType == null || actualType == ValueType.Null) {
            return expectedType;
        }
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (!Objects.equals(actualType, expectedType)) {
            // only evaluate printing if we need to
            String string = "Expected type " + expectedType + " for value " + printer.visit(expectedVal) + " but got " + actualType + " in expression: " + printer.visit(actualVal);
            throw new TypeError(string);
        }
        return actualType;
    }

    public Type expect(Type actualType, Type expectedType, Statement actualVal, Expression expectedVal) {
        if (actualType == ValueType.Null) {
            return expectedType;
        }
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (!Objects.equals(actualType, expectedType)) {
            // only evaluate printing if we need to
            String string = "Expected type " + expectedType + " for value " + printer.visit(expectedVal) + " but got " + actualType + " in expression: " + printer.visit(actualVal);
            throw new TypeError(string);
        }
        return actualType;
    }
}
