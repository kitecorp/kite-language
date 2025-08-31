package io.kite.Runtime.Inputs;

import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Parser;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Interpreter;
import io.kite.Runtime.exceptions.MissingInputException;
import io.kite.TypeChecker.Types.Type;
import io.kite.Utils.FileHelpers;
import io.kite.Visitors.Visitor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public non-sealed class ChainResolver extends InputResolver implements Visitor<Object> {
    private final Tokenizer tokenizer;
    private final Parser parser;
    private final Interpreter interpreter;
    private List<InputResolver> resolvers;

    public ChainResolver(Environment<Object> environment) {
        super(environment);
        this.resolvers = List.of(
                new FileResolver(environment, FileHelpers.loadInputDefaultsFiles()),
                new EnvResolver(environment),
                new CliResolver(environment)
        );
        this.tokenizer = new Tokenizer();
        this.parser = new Parser();
        this.interpreter = new Interpreter();
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
    public Object visit(InputDeclaration inputDeclaration) {
        if (inputDeclaration.hasInit()) {
            var defaultValue = visit(inputDeclaration.getInit());
//            inputDeclaration.setInit(defaultValue);
//            return getInputs().initOrAssign((String) visit(inputDeclaration.getId()), defaultValue);
            return null;
        }
        try {

            var input = resolve(inputDeclaration);
            if (!(input instanceof String string) || StringUtils.isBlank(string.trim())) {
                throw new MissingInputException("Invalid input %s".formatted(inputDeclaration.getId().string()));
            }

            boolean keepOriginal = (NumberUtils.isCreatable(string) ||
                                    BooleanUtils.toBoolean(string)) ||
                                   StringUtils.startsWith(string, "{") ||
                                   StringUtils.startsWith(string, "[");
            if (!keepOriginal) {
                string = "\"%s\"".formatted(string);
            }
            var ast = parser.produceAST(tokenizer.tokenize(string));
            ExpressionStatement expressionStatement = (ExpressionStatement) ast.getBody().get(0);
            inputDeclaration.setInit(expressionStatement.getStatement());

            return null;
        } catch (NoSuchElementException exception) {
            throw new MissingInputException("Missing input %s".formatted(inputDeclaration.getId().string()));
        }
//        throw new InvalidInitException("Missing input %s".formatted(inputDeclaration.getId().string()));
    }

    @Override
    public Object visit(NumberLiteral expression) {
        return expression.getValue();
    }

    @Override
    public Object visit(BooleanLiteral expression) {
        return expression.getVal();
    }

    @Override
    public Object visit(Identifier expression) {
        return expression.string();
    }

    @Override
    public Object visit(NullLiteral expression) {
        return null;
    }

    @Override
    public Object visit(ObjectLiteral expression) {
        return Map.of(visit(expression.getKey()), visit(expression.getValue()));
    }

    @Override
    public Object visit(StringLiteral expression) {
        return expression.getValue();
    }

    @Override
    public Object visit(LambdaExpression expression) {
        return null;
    }

    @Override
    public Object visit(BlockExpression expression) {
        return null;
    }

    @Override
    public Object visit(GroupExpression expression) {
        return null;
    }

    @Override
    public Object visit(BinaryExpression expression) {
        return null;
    }

    @Override
    public Object visit(UnionTypeStatement expression) {
        return null;
    }

    @Override
    public Object visit(CallExpression<Expression> expression) {
        return null;
    }

    @Override
    public Object visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public Object visit(ComponentStatement expression) {
        return null;
    }

    @Override
    public Object visit(LogicalExpression expression) {
        return null;
    }

    @Override
    public Object visit(MemberExpression expression) {
        return null;
    }

    @Override
    public Object visit(ThisExpression expression) {
        return null;
    }

    @Override
    public Object visit(UnaryExpression expression) {
        return null;
    }

    @Override
    public Object visit(VarDeclaration expression) {
        return null;
    }

    @Override
    public Object visit(ValDeclaration expression) {
        return null;
    }

    @Override
    public Object visit(ObjectExpression expression) {
        return null;
    }

    @Override
    public Object visit(ArrayExpression expression) {
        return null;
    }

    @Override
    public Object visit(AnnotationDeclaration expression) {
        return null;
    }

    @Override
    public Object visit(AssignmentExpression expression) {
        return null;
    }

    @Override
    public Object visit(float expression) {
        return null;
    }

    @Override
    public Object visit(double expression) {
        return null;
    }

    @Override
    public Object visit(int expression) {
        return null;
    }

    @Override
    public Object visit(boolean expression) {
        return null;
    }

    @Override
    public Object visit(String expression) {
        return null;
    }

    @Override
    public Object visit(Program program) {
        for (Statement statement : program.getBody()) {
            visit(statement);
        }
        return null;
    }


    @Override
    public Object visit(Type type) {
        return null;
    }

    @Override
    public Object visit(InitStatement statement) {
        return null;
    }

    @Override
    public Object visit(FunctionDeclaration statement) {
        return null;
    }

    @Override
    public Object visit(ExpressionStatement statement) {
        return null;
    }

    @Override
    public Object visit(VarStatement statement) {
        return null;
    }

    @Override
    public Object visit(ValStatement statement) {
        return null;
    }

    @Override
    public Object visit(IfStatement statement) {
        return null;
    }

    @Override
    public Object visit(WhileStatement statement) {
        return null;
    }

    @Override
    public Object visit(ForStatement statement) {
        return null;
    }

    @Override
    public Object visit(SchemaDeclaration statement) {
        return null;
    }

    @Override
    public Object visit(ReturnStatement statement) {
        return null;
    }

    @Override
    public Object visit(ResourceExpression expression) {
        return null;
    }
}
