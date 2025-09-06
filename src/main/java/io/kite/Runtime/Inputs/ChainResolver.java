package io.kite.Runtime.Inputs;

import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Parser;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Runtime.exceptions.InvalidInitException;
import io.kite.Runtime.exceptions.MissingInputException;
import io.kite.TypeChecker.Types.Type;
import io.kite.Visitors.SyntaxPrinter;
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
    private List<InputResolver> resolvers;
    private SyntaxPrinter printer = new SyntaxPrinter();

    public ChainResolver() {
        this.resolvers = List.of(
                new InputsDefaultsFilesFinder(),
                new EnvResolver(),
                new CliResolver()
        );
        this.tokenizer = new Tokenizer();
        this.parser = new Parser();
    }

    public ChainResolver(List<InputResolver> resolvers) {
        this.resolvers = resolvers;
        this.tokenizer = new Tokenizer();
        this.parser = new Parser();
    }

    protected static @Nullable String normalizeArrays(@Nullable String value) {
        if (value == null) return null;

        if (value.contains(",") &&
            !StringUtils.startsWithAny(value, "[", "{") &&
            !StringUtils.endsWithAny(value, "]", "}")
        ) {
            value = "[" + value + "]";
        }
        return value;
    }

    /**
     * An input must be resolved from the first resolver to the last resolver.
     * If the input is not resolved, the next resolver is tried.
     * If the input is resolved, the value is returned.
     * If the input is not resolved by any resolver, null is returned.
     * Oder of resolvers is important.
     * Lowest → Highest
     * 1.	inputs.defaults.kite (baseline defaults)
     * 2.	inputs.env.kite (project/env file)
     * 3.	Environment variables (KITE_VAR_KEY=...)
     * 4.	CLI args (--var key=value)
     * <p>
     * How to apply it
     * •	If you merge into one map, load lowest → highest so later sources overwrite earlier ones:
     * 1.	defaults → 2. env file → 3. ENV → 4. CLI
     */
    @Override
    @Nullable String resolve(InputDeclaration key, Object previousValue) {
        for (InputResolver resolver : resolvers) {
            var temp = normalizeArrays(resolver.resolve(key, previousValue));
            if (temp != null) {
                // if value is present in env and absent in file, we don't want to override existing value with null
                previousValue = temp;
            }
        }
        if (previousValue == null) return null;
        return previousValue.toString();
    }

    @Override
    public Object visit(InputDeclaration inputDeclaration) {
        if (inputDeclaration.hasInit()) {
            var defaultValue = visit(inputDeclaration.getInit());
            return parseInput(inputDeclaration, defaultValue);
        }
        return parseInput(inputDeclaration, null);
    }

    private Object parseInput(InputDeclaration inputDeclaration, Object init) {
        try {
            var input = resolve(inputDeclaration, init);
            if (!(input instanceof String string) || StringUtils.isBlank(string.trim())) {
                throw new MissingInputException("Missing `%s`".formatted(printer.visit(inputDeclaration)));
            }

            boolean keepOriginal = NumberUtils.isCreatable(string) ||
                                   BooleanUtils.toBoolean(string) ||
                                   StringUtils.startsWith(string, "{") ||
                                   StringUtils.startsWith(string, "[");
            if (!keepOriginal && !string.equals("false")) {
                string = "\"%s\"".formatted(string);
            }
            var ast = parser.produceAST(tokenizer.tokenize(string));
            var statement = (ExpressionStatement) ast.getBody().get(0);
            inputDeclaration.setInit(statement.getStatement());

            return visit(statement.getStatement());
        } catch (NoSuchElementException exception) {
            throw new MissingInputException("Missing `%s`".formatted(printer.visit(inputDeclaration)));
        }
    }

    @Override
    public Object visit(OutputDeclaration inputDeclaration) {
        if (inputDeclaration.hasInit()) {
            var defaultValue = visit(inputDeclaration.getInit());
//            inputDeclaration.setInit(defaultValue);
//            return getInputs().initOrAssign((String) visit(inputDeclaration.getId()), defaultValue);
            return null;
        }

        throw new InvalidInitException("Missing input %s".formatted(inputDeclaration.getId().string()));
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
        return printer.visit(expression).trim();
//        if (expression.isEmpty()) {
//            return Map.of();
//        }
//        var map = new HashMap<String, Object>();
//        for (var pair : expression.getProperties()) {
//            if (pair.getKey() instanceof StringLiteral literal) {
//                var key = (String) visit(literal);
//                var value = visit(pair.getValue());
//                map.put(key, value);
//            }
//        }
//        return map;
    }

    @Override
    public Object visit(ArrayExpression expression) {
        return printer.visit(expression).trim();
        //        var list = new ArrayList<>();
//        for (Expression item : expression.getItems()) {
//            var element = visit(item);
//            list.add(element);
//        }
//        return list;
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
