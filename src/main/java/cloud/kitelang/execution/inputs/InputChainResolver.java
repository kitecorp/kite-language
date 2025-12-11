package cloud.kitelang.execution.inputs;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.analysis.visitors.Visitor;
import cloud.kitelang.execution.exceptions.InvalidInitException;
import cloud.kitelang.execution.exceptions.MissingInputException;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.ast.KiteCompiler;
import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.syntax.ast.expressions.*;
import cloud.kitelang.syntax.ast.statements.*;
import cloud.kitelang.syntax.literals.*;
import cloud.kitelang.tool.theme.PlainTheme;
import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public non-sealed class InputChainResolver extends InputResolver implements Visitor<Object> {
    private final KiteCompiler parser;
    private final List<InputResolver> resolvers;
    private final SyntaxPrinter printer = new SyntaxPrinter(new PlainTheme());
    // Track component declarations for resolving inputs in instances
    private final Map<String, ComponentStatement> componentDeclarations = new HashMap<>();
    // Store resolved component inputs by qualified name (e.g., "api.hostname" -> resolved Expression)
    @Getter
    private final Map<String, Expression> resolvedComponentInputs = new HashMap<>();

    public InputChainResolver() {
        this.resolvers = List.of(
                new InputsFilesResolver(),
                new EnvResolver(),
                new CliResolver()
        );
        this.parser = new KiteCompiler();
    }

    public InputChainResolver(List<InputResolver> resolvers) {
        this.resolvers = resolvers;
        this.parser = new KiteCompiler();
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

    /**
     * Resolve an input using a qualified name (e.g., "componentName.inputName").
     * Used for component inputs where the name needs to include the component prefix.
     */
    @Nullable String resolve(String qualifiedName, InputDeclaration key, Object previousValue) {
        for (InputResolver resolver : resolvers) {
            var temp = normalizeArrays(resolver.resolve(qualifiedName, key, previousValue));
            if (temp != null) {
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
        return parseInput(inputDeclaration, init, null);
    }

    /**
     * Parse and resolve an input, optionally with a component prefix for qualified name lookup.
     */
    private Object parseInput(InputDeclaration inputDeclaration, Object init, @Nullable String componentName) {
        try {
            String qualifiedName = componentName != null
                    ? componentName + "." + inputDeclaration.name()
                    : inputDeclaration.name();

            var input = componentName != null
                    ? resolve(qualifiedName, inputDeclaration, init)
                    : resolve(inputDeclaration, init);

            var srcCode = normalizeStringInputs(input, inputDeclaration, qualifiedName);

            var ast = parser.parse(srcCode);
            var statement = (ExpressionStatement) ast.getBody().get(0);

            if (componentName != null) {
                // Store resolved value for component inputs - don't modify the shared declaration
                resolvedComponentInputs.put(qualifiedName, statement.getStatement());
            } else {
                // For top-level inputs, set the init directly on the InputDeclaration
                inputDeclaration.setInit(statement.getStatement());
            }

            return visit(statement.getStatement());
        } catch (NoSuchElementException exception) {
            throw new MissingInputException("Missing `%s`".formatted(printer.visit(inputDeclaration)));
        }
    }

    private @Nullable String normalizeStringInputs(String input, InputDeclaration inputDeclaration, String displayName) {
        if (!(input instanceof String string) || StringUtils.isBlank(string.trim())) {
            throw new MissingInputException("Missing input `%s`".formatted(displayName));
        }
        string = string.trim();
        boolean keepOriginal = NumberUtils.isCreatable(string) ||
                               BooleanUtils.toBoolean(string) ||
                               looksLikeExpression(string);
        if (!keepOriginal && !string.equals("false")) {
            string = "\"%s\"".formatted(string);
        }
        return string;
    }

    private boolean looksLikeExpression(String input) {
        // Starts with expression-like patterns
        return input.startsWith("{") ||
               input.startsWith("[") ||
               input.startsWith("object(") ||
               input.startsWith("object()");
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
    public Object visit(StringInterpolation expression) {
        var result = new StringBuilder();
        for (var part : expression.getParts()) {
            switch (part) {
                case StringInterpolation.Text text -> result.append(text.value());
                case StringInterpolation.Expr expr -> {
                    var value = visit(expr.expression());
                    result.append(value != null ? value.toString() : "null");
                }
            }
        }
        return result.toString();
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
    public Object visit(ComponentStatement component) {
        var typeName = component.getType().string();

        if (component.isDefinition()) {
            // Store component definition for later instance resolution
            componentDeclarations.put(typeName, component);
        } else if (component.name() != null) {
            // Component instance - resolve inputs from the declaration
            var declaration = componentDeclarations.get(typeName);
            if (declaration != null) {
                // Collect input names that are explicitly overridden in the instance
                var overriddenInputs = new HashSet<String>();
                for (Statement stmt : component.getArguments()) {
                    if (stmt instanceof ExpressionStatement exprStmt &&
                        exprStmt.getStatement() instanceof AssignmentExpression assignment &&
                        assignment.getLeft() instanceof Identifier id) {
                        overriddenInputs.add(id.string());
                    }
                }

                // Only resolve inputs that are NOT explicitly overridden in the instance
                for (Statement stmt : declaration.getArguments()) {
                    if (stmt instanceof InputDeclaration input && !overriddenInputs.contains(input.name())) {
                        Object init = input.hasInit() ? visit(input.getInit()) : null;
                        parseInput(input, init, component.name());
                    }
                }
            }
        }
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
        // when an input is initialised with an object this will be called.
        // we just convert it to object({...}) string because later needs to be parsed
        return "object(" + printer.visit(expression).trim() + ")";
    }

    @Override
    public Object visit(ArrayExpression expression) {
        return printer.visit(expression).trim();
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
    public Object visit(ImportStatement statement) {
        // Import statements are handled by the interpreter
        return null;
    }

    @Override
    public Object visit(ResourceStatement expression) {
        return null;
    }
}
