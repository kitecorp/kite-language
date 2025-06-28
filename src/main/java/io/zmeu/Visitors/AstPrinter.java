package io.zmeu.Visitors;

import io.zmeu.Frontend.Parser.Expressions.*;
import io.zmeu.Frontend.Parser.Literals.*;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.*;
import io.zmeu.TypeChecker.Types.Type;

public final class AstPrinter implements Visitor<String> {

    public String print(Expression expr) {
        return visit(expr);
    }

    @Override
    public String visit(Expression expression) {
        return print(expression);
    }

    @Override
    public String visit(BinaryExpression expression) {
        return parenthesize(expression.getOperator(), expression.getLeft(), expression.getRight());
    }

    @Override
    public String visit(CallExpression expression) {
        return null;
    }

    @Override
    public String visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public String visit(LogicalExpression expression) {
        return parenthesize(expression.getOperator().toString(), expression.getLeft(), expression.getRight());
    }

    @Override
    public String visit(MemberExpression expression) {
        return null;
    }

    @Override
    public String visit(ThisExpression expression) {
        return null;
    }

    @Override
    public String visit(UnaryExpression expression) {
        return parenthesize(expression.getOperator(), expression.getValue());
    }

    @Override
    public String visit(VarDeclaration expression) {
        return null;
    }

    @Override
    public String visit(ValDeclaration expression) {
        return null;
    }

    @Override
    public String visit(AssignmentExpression expression) {
        return parenthesize(expression.getOperator().toString(), expression.getLeft(), expression.getRight());
    }

    @Override
    public String visit(float expression) {
        return String.valueOf(expression);
    }

    @Override
    public String visit(double expression) {
        return String.valueOf(expression);
    }

    @Override
    public String visit(int expression) {
        return String.valueOf(expression);
    }

    @Override
    public String visit(boolean expression) {
        return String.valueOf(expression);
    }

    @Override
    public String visit(String expression) {
        return String.valueOf(expression);
    }

    @Override
    public String visit(Program program) {
        return "";
    }

    @Override
    public String visit(Statement statement) {
        return "";
    }

    @Override
    public String visit(Type statement) {
        return "";
    }

    @Override
    public String visit(InitStatement statement) {
        return "";
    }

    @Override
    public String visit(FunctionDeclaration statement) {
        return "";
    }

    @Override
    public String visit(ExpressionStatement statement) {
        return "";
    }

    @Override
    public String visit(VarStatement statement) {
        return "";
    }

    @Override
    public String visit(ValStatement statement) {
        return "";
    }

    @Override
    public String visit(IfStatement statement) {
        return "";
    }

    @Override
    public String visit(WhileStatement statement) {
        return "";
    }

    @Override
    public String visit(ForStatement statement) {
        return "";
    }

    @Override
    public String visit(SchemaDeclaration statement) {
        return "";
    }

    @Override
    public String visit(ReturnStatement statement) {
        return "";
    }

    @Override
    public String visit(ResourceExpression expression) {
        return "";
    }

    @Override
    public String visit(NumberLiteral expression) {
        if (expression.getVal() == null) {
            return "null";
        }
        return expression.getVal().toString();
    }

    @Override
    public String visit(BooleanLiteral expression) {
        return expression.getVal().toString();
    }

    @Override
    public String visit(Identifier expression) {
        return expression.string();
    }

    @Override
    public String visit(NullLiteral expression) {
        return "null";
    }

    @Override
    public String visit(StringLiteral expression) {
        return expression.getValue();
    }

    @Override
    public String visit(BlockExpression expression) {
        return null;
    }

    @Override
    public String visit(GroupExpression expression) {
        return parenthesize("group", expression.getExpression());
    }

    @Override
    public String visit(LambdaExpression expression) {
        return null;
    }

    private String parenthesize(String name, Expression... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expression expr : exprs) {
            builder.append(" ");
            builder.append(visit(expr));
        }
        builder.append(")");

        return builder.toString();
    }
}