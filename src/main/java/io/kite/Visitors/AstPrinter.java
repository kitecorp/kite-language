package io.kite.Visitors;

import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.TypeChecker.Types.Type;

import java.text.MessageFormat;

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
    public String visit(UnionTypeStatement expression) {
        return expression.name() + " = " + expression.getExpressions().stream().map(this::visit).reduce((a, b) -> a + " | " + b).orElse("");
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
    public String visit(ComponentStatement expression) {
        return "component";
    }

    @Override
    public String visit(InputDeclaration expression) {
        if (expression.hasInit()) {
            return MessageFormat.format("input {0} {1} = {2}", visit(expression.getType()), visit(expression.getId()), visit(expression.getInit()));
        } else {
            return MessageFormat.format("input {0} {1}", visit(expression.getType()), visit(expression.getId()));
        }
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
    public String visit(ObjectExpression expression) {
        return "";
    }

    @Override
    public String visit(ArrayExpression expression) {
        return "[" + expression.getItems()
                .stream()
                .map(this::visit)
                .reduce((a, b) -> a + "," + b)
                .orElse("")
               + "]";
    }

    @Override
    public String visit(AnnotationDeclaration expression) {
        var string = new StringBuilder("@" + visit(expression.getName()));
        if (expression.getArgs() != null) {
            string.append("(")
                    .append(visit(expression.getArgs()))
                    .append(")");
        } else if (expression.getValue() != null) {
            string.append("(")
                    .append(visit(expression.getValue()))
                    .append(")");
        } else if (expression.getObject() != null) {
            string.append("(")
                    .append(visit(expression.getObject()))
                    .append(")");
        }
        return string.toString() + "\n";
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
    public String visit(ObjectLiteral expression) {
        return "";
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