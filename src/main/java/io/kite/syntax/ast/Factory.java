package io.kite.syntax.ast;

import io.kite.syntax.ast.expressions.*;
import io.kite.syntax.ast.statements.EmptyStatement;
import io.kite.syntax.ast.statements.ExpressionStatement;
import io.kite.syntax.ast.statements.Statement;
import io.kite.syntax.ast.statements.VarStatement;
import io.kite.syntax.parser.literals.Identifier;
import io.kite.syntax.parser.literals.NumberLiteral;
import io.kite.syntax.parser.literals.TypeIdentifier;

import java.util.Arrays;

public class Factory {
    public static Program program(Statement... object) {
        return Program.builder().body(Arrays.stream(object).toList()).build();
    }

    public static Program program(Expression... object) {
        return Program.builder().body(Arrays.stream(object).map(ExpressionStatement::expressionStatement).map(it -> (Statement) it).toList()).build();
    }

    public static Statement expressionStatement(Expression object) {
        return ExpressionStatement.expressionStatement(object);
    }

    public static Expression unary(Object operator, Expression left) {
        return UnaryExpression.of(operator, left);
    }

    public static Expression unary(Object operator, String left) {
        return UnaryExpression.of(operator, Identifier.id(left));
    }

    public static Expression binary(String operator, Expression left, Expression right) {
        return BinaryExpression.binary(operator, left, right);
    }

    public static Expression binary(String operator, Expression left, int right) {
        return BinaryExpression.binary(operator, left, NumberLiteral.of(right));
    }

    public static Expression binary(String identifier, int left, String operator) {
        return BinaryExpression.binary(operator, Identifier.id(identifier), NumberLiteral.of(left));
    }

    public static Expression greater(String identifier, int left) {
        return BinaryExpression.binary(">", Identifier.id(identifier), NumberLiteral.of(left));
    }

    public static Expression less(String identifier, int left) {
        return BinaryExpression.binary(">", Identifier.id(identifier), NumberLiteral.of(left));
    }

    public static Expression member(String type, String right) {
        return MemberExpression.member(false, Identifier.id(type), Identifier.id(right));
    }

    public static VarStatement var(Identifier id, TypeIdentifier type) {
        return (VarStatement) VarStatement.varStatement(VarDeclaration.of(id, type));
    }

    public static Statement empty() {
        return EmptyStatement.of();
    }
}
