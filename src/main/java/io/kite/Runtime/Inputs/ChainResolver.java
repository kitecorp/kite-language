package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.exceptions.InvalidInitException;
import io.kite.TypeChecker.Types.Type;
import io.kite.Utils.FileHelpers;
import io.kite.Visitors.Visitor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public non-sealed class ChainResolver extends InputResolver implements Visitor<Void> {
    private List<InputResolver> resolvers;

    public ChainResolver(Environment<Object> environment) {
        super(environment);
        this.resolvers = List.of(
                new FileResolver(environment, FileHelpers.loadInputDefaultsFiles()),
                new EnvResolver(environment),
                new CliResolver(environment)
        );
    }

    @Override
    public @Nullable Object resolve(InputDeclaration key) {
        Object value = null;
        for (InputResolver resolver : resolvers) {
            value = resolver.resolve(key);
        }
        return value;
    }

    @Override
    public Void visit(InputDeclaration inputDeclaration) {
        var input = resolve(inputDeclaration);
        if (input == null) {
            throw new InvalidInitException("Missing input %s".formatted(inputDeclaration.getId().string()));
        }
        if (input instanceof String string) {
            if (string.trim().isEmpty()) {
                throw new InvalidInitException("Missing input %s".formatted(inputDeclaration.getId().string()));
            }
            Object res = switch (inputDeclaration.getType().getType().getKind()) {
                case STRING -> string;
                case NUMBER -> {
                    if (string.contains(".")) {
                        yield Double.parseDouble(string);
                    } else {
                        yield Integer.parseInt(string);
                    }
                }
                case BOOLEAN -> Boolean.parseBoolean(string);
                case OBJECT -> InputParser.parseObject(string);
//                case ARRAY -> InputParser.parseArray(string);
                case UNION_TYPE -> string;
                default -> throw new IllegalStateException("Unexpected value: " + inputDeclaration.getType().getType());
            };
            getInputs().initOrAssign(inputDeclaration.name(), res);
        }
        return null;
//        throw new InvalidInitException("Missing input %s".formatted(inputDeclaration.getId().string()));
    }

    @Override
    public Void visit(NumberLiteral expression) {
        return null;
    }

    @Override
    public Void visit(BooleanLiteral expression) {
        return null;
    }

    @Override
    public Void visit(Identifier expression) {
        return null;
    }

    @Override
    public Void visit(NullLiteral expression) {
        return null;
    }

    @Override
    public Void visit(ObjectLiteral expression) {
        return null;
    }

    @Override
    public Void visit(StringLiteral expression) {
        return null;
    }

    @Override
    public Void visit(LambdaExpression expression) {
        return null;
    }

    @Override
    public Void visit(BlockExpression expression) {
        return null;
    }

    @Override
    public Void visit(GroupExpression expression) {
        return null;
    }

    @Override
    public Void visit(BinaryExpression expression) {
        return null;
    }

    @Override
    public Void visit(UnionTypeStatement expression) {
        return null;
    }

    @Override
    public Void visit(CallExpression<Expression> expression) {
        return null;
    }

    @Override
    public Void visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public Void visit(ComponentStatement expression) {
        return null;
    }

    @Override
    public Void visit(LogicalExpression expression) {
        return null;
    }

    @Override
    public Void visit(MemberExpression expression) {
        return null;
    }

    @Override
    public Void visit(ThisExpression expression) {
        return null;
    }

    @Override
    public Void visit(UnaryExpression expression) {
        return null;
    }

    @Override
    public Void visit(VarDeclaration expression) {
        return null;
    }

    @Override
    public Void visit(ValDeclaration expression) {
        return null;
    }

    @Override
    public Void visit(ObjectExpression expression) {
        return null;
    }

    @Override
    public Void visit(ArrayExpression expression) {
        return null;
    }

    @Override
    public Void visit(AnnotationDeclaration expression) {
        return null;
    }

    @Override
    public Void visit(AssignmentExpression expression) {
        return null;
    }

    @Override
    public Void visit(float expression) {
        return null;
    }

    @Override
    public Void visit(double expression) {
        return null;
    }

    @Override
    public Void visit(int expression) {
        return null;
    }

    @Override
    public Void visit(boolean expression) {
        return null;
    }

    @Override
    public Void visit(String expression) {
        return null;
    }

    @Override
    public Void visit(Program program) {
        for (Statement statement : program.getBody()) {
            visit(statement);
        }
        return null;
    }


    @Override
    public Void visit(Type type) {
        return null;
    }

    @Override
    public Void visit(InitStatement statement) {
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration statement) {
        return null;
    }

    @Override
    public Void visit(ExpressionStatement statement) {
        return null;
    }

    @Override
    public Void visit(VarStatement statement) {
        return null;
    }

    @Override
    public Void visit(ValStatement statement) {
        return null;
    }

    @Override
    public Void visit(IfStatement statement) {
        return null;
    }

    @Override
    public Void visit(WhileStatement statement) {
        return null;
    }

    @Override
    public Void visit(ForStatement statement) {
        return null;
    }

    @Override
    public Void visit(SchemaDeclaration statement) {
        return null;
    }

    @Override
    public Void visit(ReturnStatement statement) {
        return null;
    }

    @Override
    public Void visit(ResourceExpression expression) {
        return null;
    }
}
