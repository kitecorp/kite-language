package io.kite.Visitors;

import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.TypeChecker.Types.ArrayType;
import io.kite.TypeChecker.Types.Type;
import io.kite.TypeChecker.Types.UnionType;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;


public non-sealed class SyntaxPrinter implements Visitor<String> {
    @Setter
    private Theme theme;

    public SyntaxPrinter() {
        this(new JansiTheme());
    }

    public SyntaxPrinter(Theme colorise) {
        this.theme = colorise;
    }

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
        return visit(expression.getLeft())
               + " "
               + theme.kw(expression.getOperator().toString())
               + " "
               + visit(expression.getRight());
    }

    @Override
    public String visit(UnionTypeStatement expression) {
        return theme.kw("type ")
               + visit(requireNonNull(expression.getName()))
               + " "
               + theme.punctuation("=")
               + " "
               + expression.getExpressions()
                       .stream()
                       .map(this::visit)
                       .reduce((a, b) -> a + " " + theme.punctuation("|") + " " + b)
                       .orElse("");
    }

    @Override
    public String visit(CallExpression<Expression> expression) {
        var callName = visit(expression.getCallee());
        var args = expression.getArguments()
                .stream()
                .map(this::visit)
                .collect(Collectors.joining(theme.punctuation(",")));

        return callName
               + theme.punctuation("(")
               + args
               + theme.punctuation(")");
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
            return theme.kw("input ")
                   + visit(expression.getType())
                   + " "
                   + visit(expression.getId())
                   + " = "
                   + visit(expression.getInit());
        } else {
            return theme.kw("input ")
                   + visit(expression.getType())
                   + " "
                   + visit(expression.getId());
        }
    }

    @Override
    public String visit(OutputDeclaration expression) {
        var head = theme.kw("output ") + visit(expression.getType()) + " " + visit(expression.getId()) + " = ";
        if (expression.isSensitive()) {
            return head + theme.normal("<sensitive value>") + "\n";
        }
        return head + visit(expression.resolvedValue()) + "\n";
    }

    public Object visit(Object value) {
        return switch (value) {
            case NumberLiteral n -> visit(n);
            case StringLiteral s -> visit(s);
            case BooleanLiteral b -> visit(b);
            case Integer i -> theme.num(String.valueOf(i));
            case Double d -> theme.num(String.valueOf(d));
            case Float f -> theme.num(String.valueOf(f));
            case Boolean z -> theme.bool(String.valueOf(z));
            case String s -> theme.string("\"" + s + "\"");
            case ArrayExpression a -> visit(a);
            case List list -> list.stream().map(this::visit)
                    .collect(Collectors.joining(", ", "[", "]"));
            case Map<?, ?> map -> map.entrySet().stream()
                    .map(e -> visit(e.getKey()) + ": " + visit(e.getValue()))
                    .collect(Collectors.joining(", ", "{", "}"));
            case null, default -> value;
        };
    }

    @Override
    public String visit(LogicalExpression expression) {
        return theme.punctuation("(")
               + visit(expression.getLeft())
               + " "
               + theme.kw(expression.getOperator().toString())
               + " "
               + visit(expression.getRight())
               + theme.punctuation(")");
    }

    @Override
    public String visit(MemberExpression expression) {
        if (expression.isComputed()) {
            return visit(expression.getObject())
                   + theme.punctuation("[")
                   + visit(expression.getProperty())
                   + theme.punctuation("]");
        }

        return visit(expression.getObject())
               + theme.punctuation(".")
               + visit(expression.getProperty());
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
        var builder = new StringBuilder();

        builder.append(expression.hasType() ? visit(expression.getType()) + " " : "")
                .append(theme.identifier(expression.getId().string()));

        if (expression.hasInit()) {
            builder.append(" ")
                    .append(theme.punctuation("="))
                    .append(" ")
                    .append(visit(expression.getInit()));
        }

        return builder.toString();
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
                .map(expression1 -> visit(expression1))
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
    public String visit(AnnotationDeclaration e) {
        var name = theme.decorator("@" + e.getName().string());
        String args = null;

        if (e.getArgs() != null) {
            args = "(" + visit(e.getArgs()) + ")";
        } else if (e.getValue() != null) {
            args = "(" + visit(e.getValue()) + ")";
        } else if (e.getObject() != null) {
            args = "(" + visit(e.getObject()) + ")";
        } else if (e.getNamedArgs() != null) {
            args = "(" + e.getNamedArgs().entrySet().stream()
                    .map(x -> visit(x.getKey()) + " = " + visit(x.getValue()))
                    .collect(Collectors.joining(", ")) + ")";
        }
        return args == null ? name : name + args;
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
            case ArrayType at -> theme.type(at.getType().getValue() + "[]");
            case UnionType ut -> theme.type(ut.getTypes()
                    .stream().map(this::visit)
                    .collect(Collectors.joining(" | ")));
            default -> theme.type(type.getValue());
        };
    }

    @Override
    public String visit(InitStatement statement) {
        return "";
    }

    @Override
    public String visit(FunctionDeclaration statement) {
        return theme.kw("fun ")
               + theme.identifier(statement.getName().string())
               + theme.punctuation("(")
               + statement.getParams().stream()
                       .map(SyntaxPrinter::formatParameter)
                       .collect(Collectors.joining(theme.punctuation(",")))
               + theme.punctuation(") {")
               + "\n"
               + visit(statement.getBody())
               + "\n"
               + theme.punctuation("}")
               + "\n";
    }

    @Override
    public String visit(ExpressionStatement statement) {
        return visit(statement.getStatement());
    }

    @Override
    public String visit(VarStatement statement) {
        return theme.kw("var ")
               + statement.getDeclarations()
                       .stream()
                       .map(this::visit)
                       .collect(Collectors.joining(theme.punctuation(",")));
    }

    @Override
    public String visit(ValStatement statement) {
        return "val " + statement.getDeclarations()
                .stream()
                .map(this::visit)
                .collect(Collectors.joining(","));
    }

    @Override
    public String visit(IfStatement s) {
        var sb = new StringBuilder();

        sb.append(theme.kw("if "))
                .append(visit(s.getTest()))
                .append(theme.punctuation("{")).append("\n")
                .append(visit(s.getConsequent())).append("\n")
                .append(theme.punctuation("}")).append("\n");

        if (s.hasElse()) {
            sb.append(" ")
                    .append(theme.kw("else"))
                    .append(" ")
                    .append(theme.punctuation("{")).append("\n")
                    .append(visit(s.getAlternate())).append("\n")
                    .append(theme.punctuation("}")).append("\n");
        }

        return sb.toString();
    }

    @Override
    public String visit(WhileStatement statement) {
        return theme.kw("while")
               + theme.punctuation(" (")
               + visit(statement.getTest())
               + theme.punctuation(") {")
               + "\n"
               + visit(statement.getBody())
               + "\n"
               + theme.punctuation("}")
               + "\n";
    }

    @Override
    public String visit(ForStatement statement) {
        return "";
    }

    @Override
    public String visit(SchemaDeclaration statement) {
        var builder = new StringBuilder();

        builder.append(theme.kw("schema "))
                .append(visit(statement.getName()))
                .append(theme.punctuation(" {"))
                .append("\n");

        for (SchemaProperty property : statement.getProperties()) {
            builder.append("\t")
                    .append(visit(property.type()))
                    .append(" ")
                    .append(theme.identifier(property.name()))
                    .append("\n");
        }

        builder.append(theme.punctuation("}"))
                .append("\n");

        return builder.toString();
    }

    @Override
    public String visit(ReturnStatement statement) {
        return theme.kw("return ") + visit(statement.getArgument());
    }

    @Override
    public String visit(ResourceStatement expression) {
        return theme.kw("resource ") +
               theme.type(visit(expression.getType())) + " " +
               visit(expression.getName()) + " {\n" +
               visit(expression.getBlock()) +
               "}\n";
    }

    @Override
    public String visit(NumberLiteral expression) {
        return expression.getVal() == null ? "null" : theme.num(expression.getVal().toString());
    }

    @Override
    public String visit(BooleanLiteral expression) {
        return theme.bool(expression.getVal().toString());
    }

    @Override
    public String visit(Identifier expression) {
        return switch (expression) {
            case ParameterIdentifier p -> theme.type(formatParameter(p));
            case ArrayTypeIdentifier a -> theme.type(visit(a.getType()));
            case TypeIdentifier t -> theme.type(t.string());
            case null -> null;
            default -> theme.identifier(expression.string());
        };
    }

    @Override
    public String visit(NullLiteral expression) {
        return theme.kw("null");
    }

    @Override
    public String visit(ObjectLiteral expression) {
        return visit(expression.getKey())
               + theme.punctuation(": ")
               + visit(expression.getValue());
    }

    @Override
    public String visit(StringLiteral expression) {
        return theme.string("\"" + expression.getValue() + "\"");
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

    private String parenthesize(String name, Expression... expressions) {
        var builder = new StringBuilder();

        builder.append(theme.punctuation("("))
                .append(theme.kw(name));

        for (Expression expression : expressions) {
            builder.append(" ")
                    .append(visit(expression));
        }

        builder.append(theme.punctuation(")"));

        return builder.toString();
    }
}