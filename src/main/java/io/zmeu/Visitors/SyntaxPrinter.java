package io.zmeu.Visitors;

import io.zmeu.Frontend.Parse.Literals.*;
import io.zmeu.Frontend.Parser.Expressions.*;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.*;
import io.zmeu.TypeChecker.Types.Type;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;


public non-sealed class SyntaxPrinter implements Visitor<String> {

    private static @NotNull String formatParameter(ParameterIdentifier it) {
        if (it.getType() == null || it.getType().getType() == null) {
            return it.getName().string();
        }
        return it.getName().string() + " :" + it.getType().string();
    }

    public String print(Expression expr) {
        return visit(expr);
    }

    @Override
    public String visit(BinaryExpression expression) {
        return visit(expression.getLeft()) + " " + expression.getOperator() + " " + visit(expression.getRight());
    }

    @Override
    public String visit(CallExpression<Expression> expression) {
        var callName = visit(expression.getCallee());
        var args = expression.getArguments()
                .stream()
                .map(this::visit)
                .collect(Collectors.joining(","));
        return callName + "(" + args + ")";
    }

    @Override
    public String visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public String visit(LogicalExpression expression) {
        return "(" + visit(expression.getLeft()) + " " + expression.getOperator().toString() + " " + visit(expression.getRight()) + ")";
    }

    @Override
    public String visit(MemberExpression expression) {
        return visit(expression.getObject()) + "." + visit(expression.getProperty());
    }

    @Override
    public String visit(ThisExpression expression) {
        return "this." + visit(expression.getInstance());
    }

    @Override
    public String visit(UnaryExpression expression) {
        return parenthesize(expression.getOperator(), expression.getValue());
    }

    @Override
    public String visit(VarDeclaration expression) {
        var var = new StringBuilder("var ");
        if (expression.hasType()) {
            var.append(expression.getType().getType().getValue()).append(" ");
        }
        var.append(expression.getId().string());
        if (expression.hasInit()) {
            var.append(" = ").append(visit(expression.getInit()));
        }
        return var.toString();
    }

    @Override
    public String visit(ValDeclaration expression) {
        StringBuilder val = new StringBuilder("val");
        if (expression.hasType()) {
            val.append(" ")
                    .append(expression.getType().getType().getValue())
                    .append(" ");
        }
        val.append(expression.getId().string());
        if (expression.hasInit()) {
            val.append(" = ").append(visit(expression.getInit()));
        }
        return val.toString();
    }

    @Override
    public String visit(ObjectExpression expression) {
        var builder = new StringBuilder("{ ");
        for (var literal : expression.getProperties()) {
            builder.append(visit(literal));
            builder.append("\n");
        }
        builder.append(" }");
        return builder.toString();
    }

    @Override
    public String visit(ArrayExpression expression) {
        var builder = new StringBuilder("[");
        List<Expression> items = expression.getItems();
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            var item = items.get(i);
            builder.append(visit(item));
            if (i < itemsSize - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
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
        return string + "\n";
    }

    @Override
    public String visit(AssignmentExpression expression) {
        return visit(expression.getLeft()) + " " + expression.getOperator().toString() + " " + visit(expression.getRight());
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
        return program.getBody()
                .stream()
                .map(this::visit)
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String visit(Type type) {
        return type.getValue();
    }

    @Override
    public String visit(InitStatement statement) {
        return "";
    }

    @Override
    public String visit(FunctionDeclaration statement) {
        return "fun " +
               statement.getName().string() +
               "("
               + statement.getParams().stream().map(SyntaxPrinter::formatParameter).collect(Collectors.joining(","))
               + ") "
               + "{ \n"
               + visit(statement.getBody())
               + "\n} \n";
    }

    @Override
    public String visit(ExpressionStatement statement) {
        return visit(statement.getStatement());
    }

    @Override
    public String visit(VarStatement statement) {
        return "var " + statement.getDeclarations()
                .stream()
                .map(this::visit)
                .collect(Collectors.joining(","));
    }

    @Override
    public String visit(ValStatement statement) {
        return "val " + statement.getDeclarations()
                .stream()
                .map(this::visit)
                .collect(Collectors.joining(","));
    }

    @Override
    public String visit(IfStatement statement) {
        var string = new StringBuilder().append("if ").append(visit(statement.getTest())).append("{\n").append(visit(statement.getConsequent())).append("\n}\n");
        if (statement.hasElse()) {
            string.append(" else {\n")
                    .append(visit(statement.getAlternate()))
                    .append("\n}\n");
        }
        return string.toString();
    }

    @Override
    public String visit(WhileStatement statement) {
        return "while (" + visit(statement.getTest()) + ") {\n"
               + visit(statement.getBody())
               + "\n}\n";
    }

    @Override
    public String visit(ForStatement statement) {
        return "";
    }

    @Override
    public String visit(SchemaDeclaration statement) {
        var builder = new StringBuilder("schema ");
        builder.append(visit(statement.getName()));
        builder.append(" {\n");
        for (SchemaDeclaration.SchemaProperty property : statement.getProperties()) {
            builder.append("\t");
            var declaration = property.declaration();
            builder.append(visit(declaration.getType()));
            builder.append(" ");
            builder.append(declaration.name());
            builder.append("\n");
        }
        builder.append("}\n");
        return builder.toString();
    }

    @Override
    public String visit(ReturnStatement statement) {
        return "return " + visit(statement.getArgument());
    }

    @Override
    public String visit(ResourceExpression expression) {
        return "resource " + visit(expression.getType()) + " " + visit(expression.getName()) + " {\n"
               + visit(expression.getBlock())
               + "}\n";
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
        return switch (expression) {
            case ParameterIdentifier parameterIdentifier -> formatParameter(parameterIdentifier);
            default -> expression.string();
        };
    }

    @Override
    public String visit(NullLiteral expression) {
        return "null";
    }

    @Override
    public String visit(ObjectLiteral expression) {
        return "%s: %s".formatted(visit(expression.getKey()), visit(expression.getValue()));
    }

    @Override
    public String visit(StringLiteral expression) {
        return "\"" + expression.getValue() + "\"";
    }

    @Override
    public String visit(BlockExpression expression) {
        StringBuilder result = new StringBuilder();
        for (Statement statement : expression.getExpression()) {
            result.append("\t");
            result.append(visit(statement));
            result.append("\n");
        }
        return result.toString();
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