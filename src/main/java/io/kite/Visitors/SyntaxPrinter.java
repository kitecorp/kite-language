package io.kite.Visitors;

import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.TypeChecker.Types.ArrayType;
import io.kite.TypeChecker.Types.Type;
import io.kite.TypeChecker.Types.UnionType;
import lombok.Getter;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;


public non-sealed class SyntaxPrinter implements Visitor<String> {
    @Getter
    private final Ansi ansi = Ansi.ansi(50)
            .reset()
            .eraseScreen();

    private static @NotNull String formatParameter(ParameterIdentifier it) {
        if (it.getType() == null || it.getType().getType() == null) {
            return it.getName().string();
        }
        return it.getName().string() + " :" + it.getType().string();
    }

    private static String colorizeType(String t) {
        return Ansi.ansi()
                .fgBlue()
                .a(t)
                .fgDefault()
                .toString();
    }

    public String print(Expression expr) {
        return visit(expr);
    }

    @Override
    public String visit(BinaryExpression expression) {
        return visit(expression.getLeft()) + " " + expression.getOperator() + " " + visit(expression.getRight());
    }

    @Override
    public String visit(UnionTypeStatement expression) {
        return "type " + visit(requireNonNull(expression.getName())) + " = " +
               expression.getExpressions()
                       .stream()
                       .map(this::visit)
                       .reduce((a, b) -> a + " | " + b).orElse("");
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
    public String visit(ComponentStatement expression) {
        throw new UnsupportedOperationException("ComponentStatement is not supported in SyntaxPrinter");
    }

    @Override
    public String visit(InputDeclaration expression) {
        if (expression.hasInit()) {
            return ansi.fgMagenta().a("input ")
                    .a(visit(expression.getType()))
                    .a(" ")
                    .fgDefault()
                    .a(visit(expression.getId()))
                    .a(" = ")
                    .a(visit(expression.getInit()))
                    .toString();
        } else {
            return ansi.fgMagenta().a("input ")
                    .a(visit(expression.getType()))
                    .a(" ")
                    .fgDefault()
                    .a(visit(expression.getId()))
                    .toString();
        }
    }

    @Override
    public String visit(OutputDeclaration expression) {
        // 2) Build once, applying styles based on flags
        var ansi = Ansi.ansi();

        ansi.fgMagenta().a("output ").reset()
                .a(visit(expression.getType())).a(" ")
                .a(visit(expression.getId())).a(" = ");

        if (expression.isSensitive()) {
            // mask, don’t print the actual value
            ansi.fgBrightBlack().a(Ansi.Attribute.ITALIC)
                    .a("<sensitive value>")
                    .reset();
        } else {
            ansi.a(visit(expression.resolvedValue()));
        }

        ansi.a("\n").reset();
        return ansi.toString();
    }

    public Object visit(Object value) {
        return switch (value) {
            case NumberLiteral numberLiteral -> Ansi.ansi().fgCyan().a(visit(numberLiteral)).fgDefault().toString();
            case StringLiteral stringLiteral -> Ansi.ansi().fgCyan().a(visit(stringLiteral)).fgDefault().toString();
            case BooleanLiteral booleanLiteral -> Ansi.ansi().fgCyan().a(visit(booleanLiteral)).fgDefault().toString();
            case Integer integer -> Ansi.ansi().fgCyan().a(visit(integer.intValue())).fgDefault().toString();
            case Double doubleValue -> Ansi.ansi().fgCyan().a(visit(doubleValue.doubleValue())).fgDefault().toString();
            case Float floatValue -> Ansi.ansi().fgCyan().a(visit(floatValue.floatValue())).fgDefault().toString();
            case Boolean booleanValue ->
                    Ansi.ansi().fgCyan().a(visit(booleanValue.booleanValue())).fgDefault().toString();
            case String stringValue -> Ansi.ansi().fgGreen().a('"').a(visit(stringValue)).a('"').fgDefault().toString();
            case ArrayExpression arrayExpression -> visit(arrayExpression);
            case List list -> list.stream().map(this::visit).collect(Collectors.joining(", ", "[", "]"));
            case Map<?, ?> map ->
                    map.entrySet().stream().map(e -> visit(e.getKey()) + ": " + visit(e.getValue())).collect(Collectors.joining(", ", "{", "}"));
            case null, default -> value;
        };
    }

    @Override
    public String visit(LogicalExpression expression) {
        return "(" + visit(expression.getLeft()) + " " + expression.getOperator().toString() + " " + visit(expression.getRight()) + ")";
    }

    @Override
    public String visit(MemberExpression expression) {
        if (expression.isComputed()) {
            return visit(expression.getObject()) + "[" + visit(expression.getProperty()) + "]";
        }
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
            var.append(visit(expression.getType())).append(" ");
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
        if (expression.isEmpty()) {
            return "{ }";
        }
        return expression.getProperties().stream()
                .map(this::visit)
                .collect(Collectors.joining(",\n", "{\n ", " \n}"));
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
        var ansi = Ansi.ansi()
                .fgYellow()
                .a("@")
                .a(visit(expression.getName()));
        if (expression.getArgs() != null) {
            ansi.a("(")
                    .a(visit(expression.getArgs()))
                    .fgYellow()
                    .a(")");
        } else if (expression.getValue() != null) {
            ansi.a("(")
                    .a(visit(expression.getValue()).toString())
                    .fgYellow()
                    .a(")");
        } else if (expression.getObject() != null) {
            ansi.a("(")
                    .a(visit(expression.getObject()))
                    .fgYellow()
                    .a(")");
        }
        return ansi.reset().toString() + "\n";
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
        return switch (type) {
            case ArrayType arrayType -> visit(arrayType.getType()) + "[]";
            case UnionType unionType ->
                    unionType.getTypes().stream().map(this::visit).collect(Collectors.joining(" | "));
            default -> type.getValue();
        };
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
        for (SchemaProperty property : statement.getProperties()) {
            builder.append("\t");
            builder.append(visit(property.type()));
            builder.append(" ");
            builder.append(property.name());
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
        return Ansi.ansi()
                .fgMagenta().a("resource ")
                .fgBlue().a(visit(expression.getType())).a(" ")
                .fgDefault().a(visit(expression.getName())).a(" {\n")
                .a(visit(expression.getBlock()))
                .a("}\n")
                .toString();
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
            case ParameterIdentifier parameterIdentifier -> colorizeType(formatParameter(parameterIdentifier));
            case ArrayTypeIdentifier arrayTypeIdentifier -> colorizeType(visit(arrayTypeIdentifier.getType()));
            case TypeIdentifier identifier -> colorizeType(identifier.string());
            case null -> null;
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