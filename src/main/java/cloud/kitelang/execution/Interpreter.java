package cloud.kitelang.execution;

import cloud.kitelang.ContextStack;
import cloud.kitelang.analysis.ImportResolver;
import cloud.kitelang.analysis.visitors.StackVisitor;
import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.execution.decorators.*;
import cloud.kitelang.execution.environment.ActivationEnvironment;
import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.execution.exceptions.*;
import cloud.kitelang.execution.interpreter.OperatorComparator;
import cloud.kitelang.execution.values.*;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.scope.ScopeResolver;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.stdlib.functions.PrintFunction;
import cloud.kitelang.stdlib.functions.PrintlnFunction;
import cloud.kitelang.stdlib.functions.cast.*;
import cloud.kitelang.stdlib.functions.collections.*;
import cloud.kitelang.stdlib.functions.datetime.*;
import cloud.kitelang.stdlib.functions.numeric.*;
import cloud.kitelang.stdlib.functions.objects.EntriesFunction;
import cloud.kitelang.stdlib.functions.objects.HasKeyFunction;
import cloud.kitelang.stdlib.functions.objects.KeysFunction;
import cloud.kitelang.stdlib.functions.objects.MergeFunction;
import cloud.kitelang.stdlib.functions.string.*;
import cloud.kitelang.stdlib.functions.types.*;
import cloud.kitelang.stdlib.functions.utility.*;
import cloud.kitelang.syntax.ast.KiteCompiler;
import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.syntax.ast.expressions.*;
import cloud.kitelang.syntax.ast.statements.*;
import cloud.kitelang.syntax.literals.*;
import cloud.kitelang.syntax.literals.ObjectLiteral.ObjectLiteralPair;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static cloud.kitelang.execution.CycleDetection.topologySort;
import static cloud.kitelang.execution.CycleDetectionSupport.lookupOrPending;
import static cloud.kitelang.execution.CycleDetectionSupport.propertyOrDeferred;
import static cloud.kitelang.execution.interpreter.OperatorComparator.compare;
import static cloud.kitelang.syntax.ast.statements.FunctionDeclaration.fun;
import static java.text.MessageFormat.format;

@Log4j2
public final class Interpreter extends StackVisitor<Object> {
    private final DeferredObservable deferredObservable;
    @Getter
    private final List<OutputDeclaration> outputs;
    @Getter
    private final Map<String, DecoratorInterpreter> decorators;
    @Getter
    private final List<RuntimeException> errors;
    private final KiteCompiler parser = new KiteCompiler();
    // Track currently importing files to detect circular imports
    private final Set<String> importChain;
    // Track component declarations for later instantiation (similar to TypeChecker's ComponentRegistry)
    private final Map<String, ComponentStatement> componentDeclarations;
    @Getter
    @Setter
    private SyntaxPrinter printer;
    @Getter
    private Environment<Object> env;

    public Interpreter() {
        this(new Environment<>());
    }

    public Interpreter(Environment<Object> environment) {
        this(environment, new SyntaxPrinter());
    }

    public Interpreter(SyntaxPrinter printer) {
        this(new Environment<>("global"), printer);
    }

    public Interpreter(Environment<Object> environment, SyntaxPrinter printer) {
        this(environment, printer, new LinkedHashSet<>());
    }

    // Constructor with importChain for sharing across nested imports
    private Interpreter(Environment<Object> environment, SyntaxPrinter printer, Set<String> importChain) {
        this.env = environment;
        this.outputs = new ArrayList<>();
        this.printer = printer;
        this.deferredObservable = new DeferredObservable();
        this.importChain = importChain; // Share the import chain
        this.componentDeclarations = new HashMap<>();

        this.errors = new ArrayList<>();
        this.decorators = new HashMap<>();

        this.env.setName("interpreter");
        this.env.init("null", NullValue.of());
        this.env.init("true", true);
        this.env.init("false", false);
        this.env.init("print", new PrintFunction());
        this.env.init("println", new PrintlnFunction());

        // casting
        this.env.init("int", new IntCastFunction());
        this.env.init("number", new NumberCastFunction());
        this.env.init("decimal", new DecimalCastFunction());
        this.env.init("string", new StringCastFunction());
        this.env.init("boolean", new BooleanCastFunction());
        this.env.init("any", new AnyCastFunction());

        // numeric functions
        this.env.init("abs", new AbsFunction());
        this.env.init("pow", new PowFunction());
        this.env.init("ceil", new CeilFunction());
        this.env.init("floor", new FloorFunction());
        this.env.init("min", new MinFunction());
        this.env.init("max", new MaxFunction());
        this.env.init("round", new RoundFunction());
        this.env.init("sqrt", new SqrtFunction());
        this.env.init("random", new RandomFunction());
        this.env.init("clamp", new ClampFunction());
        this.env.init("sign", new SignFunction());
        this.env.init("mod", new ModFunction());

        // collection functions
        this.env.init("isEmpty", new IsEmptyFunction());
        this.env.init("contains", new ContainsFunction());
        // this.env.init("first", new FirstFunction()); // Removed: conflicts with common resource names
        // this.env.init("last", new LastFunction()); // Removed: conflicts with common resource names
        this.env.init("join", new JoinFunction());
        this.env.init("sort", new SortFunction());
        this.env.init("push", new PushFunction());
        this.env.init("pop", new PopFunction());
        this.env.init("reverse", new ReverseFunction());
        this.env.init("slice", new SliceFunction());
        // this.env.init("find", new FindFunction()); // Removed: conflicts with common resource names
        this.env.init("distinct", new DistinctFunction());
        this.env.init("flatten", new FlattenFunction());
        this.env.init("take", new TakeFunction());
        this.env.init("drop", new DropFunction());
        this.env.init("sum", new SumFunction());
        this.env.init("range", new RangeFunction());
        this.env.init("zip", new ZipFunction());
        this.env.init("average", new AverageFunction());
        this.env.init("findIndex", new FindIndexFunction());

        // string functions
        this.env.init("length", new LengthFunction());
        this.env.init("substring", new SubstringFunction());
        this.env.init("toUpperCase", new ToUpperCaseFunction());
        this.env.init("toLowerCase", new ToLowerCaseFunction());
        this.env.init("trim", new TrimFunction());
        this.env.init("replace", new ReplaceFunction());
        this.env.init("split", new SplitFunction());
        this.env.init("indexOf", new IndexOfFunction());
        this.env.init("startsWith", new StartsWithFunction());
        this.env.init("endsWith", new EndsWithFunction());
        this.env.init("repeat", new RepeatFunction());
        this.env.init("padStart", new PadStartFunction());
        this.env.init("padEnd", new PadEndFunction());
        this.env.init("charAt", new CharAtFunction());
        this.env.init("matches", new MatchesFunction());
        this.env.init("format", new FormatFunction());

        // datetime functions
        this.env.init("now", new NowFunction());
        this.env.init("year", new YearFunction());
        this.env.init("month", new MonthFunction());
        this.env.init("day", new DayFunction());
        this.env.init("hour", new HourFunction());
        this.env.init("minute", new MinuteFunction());
        // this.env.init("second", new SecondFunction()); // Removed: conflicts with common resource names
        this.env.init("formatDate", new FormatDateFunction());
        this.env.init("timestamp", new TimestampFunction());
        // this.env.init("date", new DateFunction()); // Removed: conflicts with common resource names
        this.env.init("addDays", new AddDaysFunction());
        this.env.init("diffDays", new DiffDaysFunction());
        this.env.init("isLeapYear", new IsLeapYearFunction());
        this.env.init("dayOfWeek", new DayOfWeekFunction());
        this.env.init("parseDate", new ParseDateFunction());
        this.env.init("toISOString", new ToISOStringFunction());

        // type checking functions
        this.env.init("isString", new IsStringFunction());
        this.env.init("isNumber", new IsNumberFunction());
        this.env.init("isBoolean", new IsBooleanFunction());
        this.env.init("isArray", new IsArrayFunction());
        this.env.init("isObject", new IsObjectFunction());
        this.env.init("isNull", new IsNullFunction());
        this.env.init("toNumber", new ToNumberFunction());

        // object manipulation functions
        this.env.init("keys", new KeysFunction());
        // this.env.init("values", new ValuesFunction()); // Removed: conflicts with common variable names
        this.env.init("entries", new EntriesFunction());
        this.env.init("merge", new MergeFunction());
        this.env.init("hasKey", new HasKeyFunction());
        // this.env.init("get", new GetFunction()); // Removed: conflicts with common resource names

        // utility functions
        this.env.init("uuid", new UuidFunction());
        this.env.init("base64Encode", new Base64EncodeFunction());
        this.env.init("base64Decode", new Base64DecodeFunction());
        // this.env.init("hash", new HashFunction()); // Removed: conflicts with common resource names
        // this.env.init("env", new EnvFunction()); // Removed: conflicts with common variable names (e.g., for env in environments)
        this.env.init("fileExists", new FileExistsFunction());
        this.env.init("readFile", new ReadFileFunction());
        this.env.init("fromJson", new FromJsonFunction());
        this.env.init("toJson", new ToJsonFunction());

        this.decorators.put("minValue", new MinValueDecorator(this));
        this.decorators.put("maxValue", new MaxValueDecorator(this));
        this.decorators.put("maxLength", new MaxLengthDecorator(this));
        this.decorators.put("minLength", new MinLengthDecorator(this));
        this.decorators.put("description", new DescriptionDecorator(printer));
        this.decorators.put("sensitive", new SensitiveDecorator());
        this.decorators.put("count", new CountDecorator(this));
        this.decorators.put("dependsOn", new DependsOnDecorator(this));
        this.decorators.put("allowed", new AllowedDecorator(this));
        this.decorators.put("nonEmpty", new NonEmptyDecorator(this));
        this.decorators.put("unique", new UniqueDecorator(this));
        this.decorators.put("validate", new ValidateDecorator(this));
        this.decorators.put("provider", new ProviderDecorator(this));
        this.decorators.put("tags", new TagsDecorator(this));
        this.decorators.put("existing", new ExistingDecorator(printer));
    }

    private static void forInit(Environment<Object> forEnv, Identifier index, Object i) {
        if (index != null) {
            forEnv.initOrAssign(index.string(), i);
        }
    }

    public static boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        } else if (object instanceof Boolean b) {
            return b;
        } else {
            return object instanceof BooleanLiteral;
        }
    }

    /**
     * Registers a resource instance at the root environment level.
     * Ensures global uniqueness of resource names across all scopes.
     */
    public ResourceValue initInstance(ResourceValue instance) {
        var segmentName = instance.getPath().toSegmentName();
        // Register at root level for global uniqueness
        env.initResource(segmentName, instance);
        if (ExecutionContextIn(ForStatement.class)) {
            // make resource name {..} accessible through .name instead of .name[count]
            // This is a convenience alias in the current scope, not a separate resource
            env.initOrAssign(instance.getPath().getName(), instance);
        }
        return instance;
    }

    @Nullable
    public ResourceValue getInstance(String name) {
        return env.getResource(name);
    }

    @Override
    public Object visit(int expression) {
        return expression;
    }

    @Override
    public Object visit(boolean expression) {
        return expression;
    }

    @Override
    public Object visit(String expression) {
        return expression;
    }

    @Override
    public Object visit(double expression) {
        return expression;
    }

    @Override
    public Object visit(float expression) {
        return expression;
    }

    @Override
    public Object visit(Expression expression) {
        return executeBlock(expression, env);
    }

    @Override
    public Object visit(@Nullable Statement statement) {
        push(statement);
        var res = super.visit(statement);
        pop(statement);
        return res;
    }

    @Override
    public Object visit(NumberLiteral expression) {
        return switch (expression) {
            case NumberLiteral literal -> literal.getValue();
        };
    }

    @Override
    public Object visit(BooleanLiteral expression) {
        return expression.isValue();
    }

    @Override
    public Object visit(Identifier expression) {
        if (expression instanceof ArrayTypeIdentifier arrayTypeIdentifier) {
            return env.lookup(arrayTypeIdentifier.getType().getValue(), expression.getHops());
        }
        try {
            return env.lookup(expression.string(), expression.getHops());
        } catch (NotFoundException e) {
            // when not finding the variable in the correct scope, try the most nested scope again
            return env.lookup(expression.string(), 0);
        }
    }

    @Override
    public Object visit(NullLiteral expression) {
        return null;
    }

    @Override
    public Object visit(ObjectLiteral expression) {
        if (expression.getKey() == null || expression.getValue() == null) {
            return new ObjectLiteralPair();
        }
        var res = switch (expression.getKey()) {
            case SymbolIdentifier id -> new ObjectLiteralPair(id.string(), visit(expression.getValue()));
            case StringLiteral literal -> new ObjectLiteralPair((String) visit(literal), visit(expression.getValue()));
            case StringInterpolation interpolation ->
                    new ObjectLiteralPair((String) visit(interpolation), visit(expression.getValue()));
            default -> throw new IllegalArgumentException("Invalid object literal key: " + expression.getKey());
        };
        return res;
    }

    @Override
    public Object visit(StringLiteral expression) {
        if (expression.isInterpolated()) {
            // when doing string interpolation on a property assignment we should look in the parent environment
            // for the interpolated string not in the "properties/environment of the resource"
            var hops = peek(ContextStack.Resource) ? 1 : 0;
            var values = new ArrayList<String>(expression.getInterpolationVars().size());
            for (String interpolationVar : expression.getInterpolationVars()) {
                if (StringUtils.containsAny(".", "[")) { // complex interpolation ${vm.resourceName.property}
                    var list = parser.parse(interpolationVar).getBody();
                    for (Statement statement : list) {
                        values.add(visit(statement).toString());
                    }
                } else { // normal variables ${variable}
                    var value = env.lookup(interpolationVar, hops);
                    values.add(value.toString());
                }

            }
            return expression.getInterpolatedString(values);
        }
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
        var params = expression.getParams();
        var body = expression.getBody();
        return FunValue.of((Identifier) null, params, body, env);
    }

    @Override
    public Object visit(BlockExpression block) {
        Object res = NullValue.of();
        var env = new Environment<>(this.env);
        for (var it : block.getExpression()) {
            res = executeBlock(it, env);
        }
        return res;
    }

    @Override
    public Object visit(VarStatement statement) {
        Object res = NullValue.of();
        for (var it : statement.getDeclarations()) {
            res = executeBlock(it, env);
        }
        return res;
    }

    @Override
    public Object visit(ValStatement statement) {
        Object res = NullValue.of();
        for (var it : statement.getDeclarations()) {
            res = executeBlock(it, env);
        }
        return res;
    }

    @Override
    public Object visit(GroupExpression expression) {
        return null;
    }

    @Override
    public Object visit(BinaryExpression expression) {
        Object leftBlock = executeBlock(expression.getLeft(), env);
        Object rightBlock = executeBlock(expression.getRight(), env);

        var allowedTypes = OperatorComparator.allowTypes(expression.getOperator());
        expectOperatorType(expression.getLeft(), allowedTypes, expression);
        expectOperatorType(expression.getRight(), allowedTypes, expression);

        var op = expression.getOperator();
        return switch (leftBlock) {
            case Number left when rightBlock instanceof Number right -> compare(op, left, right);
            case Number left when rightBlock instanceof String right -> compare(op, left, right);
            case String left when rightBlock instanceof String right -> compare(op, left, right);
            case String left when rightBlock instanceof Number right -> compare(op, left, right);
            case Boolean left when rightBlock instanceof Boolean right -> compare(op, left, right);
            case Map<?, ?> left when rightBlock instanceof Map<?, ?> right -> switch (op) {
                case "==" -> Objects.equals(left, right);
                case "!=" -> !Objects.equals(left, right);
                default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
            };
            case null, default ->
                    throw new IllegalArgumentException(format("{0} cannot be compared with {1}", printer.visit(expression.getLeft()), printer.visit(expression.getRight())));
        };
    }

    @Override
    public Object visit(UnionTypeStatement expression) {
        var expressions = expression.getExpressions();
        var values = new HashSet<>(expressions.size());
        for (Expression it : expressions) {
            Object o = executeBlock(it, env);
            switch (o) {
                case Set<?> list -> values.addAll(list);
                case null, default -> values.add(o);
            }
        }
        return getEnv().init(expression.getName(), values);
    }

    private void expectOperatorType(Object type, List<Class> allowedTypes, BinaryExpression expression) {
        if (!allowedTypes.contains(type.getClass())) {
            throw new TypeError("Unexpected type `" + type.getClass() + "` in expression: " + printer.visit(expression) + ". Allowed types: " + allowedTypes);
        }
    }

    @Override
    public Object visit(CallExpression<Expression> expression) {
        var callee = executeBlock(expression.getCallee(), env);
        if (callee instanceof Callable function) {

            // evaluate arguments
            var args = new ArrayList<>(expression.getArguments().size());
            for (Expression it : expression.getArguments()) {
                args.add(executeBlock(it, env));
            }

            try {
                return function.call(this, args);
            } catch (Return aReturn) {
                return aReturn.getValue();
            }
        }
        throw new RuntimeError("Can only call functions and classes: " + printer.visit(expression.getCallee()));
    }

    public Object Call(FunValue function, List<Object> args) {
        if (function.name() == null) { // execute lambda
            return lambdaCall(function, args);
        }

        return functionCall(function, args);
    }

    private Object functionCall(FunValue function, List<Object> args) {
        // for function execution, use the clojured environment from the declared scope
        var declared = (FunValue) function.getClojure().lookup(function.name(), "Function not declared: %s".formatted(function.name()));

        if (declared != null && args.size() != declared.arity()) {
            throw new RuntimeException("Expected %s arguments but got %d: %s".formatted(function.getParams().size(), args.size(), function.getName()));
        }

        var environment = new ActivationEnvironment(declared.getClojure(), declared.getParams(), args);
        return executeDiscardBlock(declared, environment);
    }

    private Object lambdaCall(FunValue function, List<Object> args) {
        var environment = new ActivationEnvironment(function.getClojure(), function.getParams(), args);
        return executeDiscardBlock(function, environment);
    }

    private Object executeDiscardBlock(FunValue declared, ActivationEnvironment environment) {
        if (!(declared.getBody() instanceof ExpressionStatement expressionStatement)) {
            throw new RuntimeException("Invalid function body");
        }
        Expression expression = expressionStatement.getStatement();
        if (expression instanceof BlockExpression blockExpression) {
            return executeBlock(blockExpression.getExpression(), environment);
        } else { // lambdas without a block could simply be an expression: ((x) -> x*x)
            return executeBlock(expression, environment);
        }
    }

    @Override
    public Object visit(ReturnStatement statement) {
        Object value = null;
        if (statement.getArgument() != null) {
            value = visit(statement.getArgument());
        }
        throw new Return(value);
    }

    @Override
    public Object visit(ImportStatement statement) {
        var resolver = new ImportResolver(parser, importChain);

        resolver.resolve(statement, env, program -> {
            // Resolve scopes in the imported program
            var scopeResolver = new ScopeResolver();
            scopeResolver.resolve(program);

            // Create a new interpreter with shared import chain
            var importInterpreter = new Interpreter(new Environment<>("import", env), printer, importChain);
            importInterpreter.visit(program);
            return importInterpreter.getEnv();
        });

        return null;
    }

    @Override
    public Object visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public Object visit(ComponentStatement statement) {
        if (!contextStackContains(ContextStack.Decorator)) {
            // needs to be above isCounted because @count marks the resourceStatement as counted
            visitAnnotations(statement.getAnnotations());
        }

        if (!statement.hasType()) {
            throw new RuntimeException("Invalid component declaration: component must have a type");
        }

        var typeName = statement.getType().string();
        var isDefinition = statement.isDefinition();
        var typeExists = componentDeclarations.containsKey(typeName);

        if (isDefinition) {
            return declareComponent(statement, typeName, typeExists);
        } else {
            return initializeComponent(statement, typeName, typeExists);
        }
    }

    /**
     * Declares a component type (component server { ... }).
     * Registers the declaration for later instantiation.
     */
    private ComponentValue declareComponent(ComponentStatement expression, String typeName, boolean typeExists) {
        if (typeExists) {
            throw new RuntimeException("Duplicate component definition: " + typeName);
        }

        // Register the declaration for later instantiation
        componentDeclarations.put(typeName, expression);

        // Create component value with its own environment
        var componentEnv = new Environment<>(typeName, env);
        var componentValue = ComponentValue.builder()
                .name(null) // No name for type definitions
                .properties(componentEnv)
                .build();

        // Execute the declaration block to initialize inputs/outputs with defaults
        // Skip resources - they should only be created during instantiation
        for (var stmt : expression.getArguments()) {
            if (stmt instanceof ResourceStatement) {
                continue; // Resources are created per-instance, not during type declaration
            }
            // Register inputs and outputs as public properties
            if (stmt instanceof InputDeclaration input) {
                componentValue.addPublicProperty(input.name());
            } else if (stmt instanceof OutputDeclaration output) {
                componentValue.addPublicProperty(output.name());
            }
            executeBlock(stmt, componentEnv);
        }

        // Register in global environment
        env.init(typeName, componentValue);

        return componentValue;
    }

    /**
     * Creates an instance of a declared component type (component server main { ... }).
     * Inherits defaults from declaration and applies instance-specific values.
     * Instance is registered by its name only (not qualified with type).
     * Only outputs can be accessed on the instance via instanceName.outputName.
     */
    private ComponentValue initializeComponent(ComponentStatement expression, String typeName, boolean typeExists) {
        if (!typeExists) {
            throw new RuntimeException("Component type not declared: " + typeName);
        }

        var instanceName = expression.name();
        var declaration = componentDeclarations.get(typeName);

        // Build a map of input declarations for validation lookup
        var inputDeclarations = new HashMap<String, InputDeclaration>();
        for (Statement stmt : declaration.getArguments()) {
            if (stmt instanceof InputDeclaration input) {
                inputDeclarations.put(input.name(), input);
            }
        }

        // Create instance environment as child of global env
        var instanceEnv = new Environment<>(instanceName, env);
        var componentValue = ComponentValue.builder()
                .name(instanceName)
                .properties(instanceEnv)
                .build();

        // Phase 1: Execute inputs and outputs from declaration block (skip resources)
        // This sets up default values for all inputs/outputs
        for (var stmt : declaration.getArguments()) {
            if (stmt instanceof InputDeclaration input) {
                componentValue.addPublicProperty(input.name());
                executeBlock(stmt, instanceEnv);
            } else if (stmt instanceof OutputDeclaration output) {
                componentValue.addPublicProperty(output.name());
                executeBlock(stmt, instanceEnv);
            }
            // Skip resources in this phase - they'll be executed after input overrides
        }

        // Phase 2: Apply instance overrides for inputs
        // This allows resources to use the overridden values
        for (var stmt : expression.getArguments()) {
            if (stmt instanceof ExpressionStatement exprStmt) {
                handleAssignment(typeName, stmt, exprStmt, instanceEnv, inputDeclarations);
            } else {
                executeBlock(stmt, instanceEnv);
            }
        }

        // Phase 3: Execute resources from declaration block with overridden input values
        for (var stmt : declaration.getArguments()) {
            if (stmt instanceof ResourceStatement) {
                executeBlock(stmt, instanceEnv);
            }
        }


        // Register instance by name only (new pattern)
        env.init(instanceName, componentValue);

        return componentValue;
    }

    private void handleAssignment(String typeName, Statement stmt, ExpressionStatement exprStmt,
                                  Environment<Object> instanceEnv, Map<String, InputDeclaration> inputDeclarations) {
        if (exprStmt.getStatement() instanceof AssignmentExpression assignment) {
            // Handle assignment: property = value
            var propertyName = getPropertyName(assignment.getLeft());
            if (!instanceEnv.hasVar(propertyName)) {
                throw new NotFoundException("Property '" + propertyName + "' not defined in component type '" + typeName + "'");
            }
            // Evaluate in instanceEnv so component properties are accessible
            var value = executeBlock(assignment.getRight(), instanceEnv);
            instanceEnv.assign(propertyName, value);

            // Re-apply decorators from the input declaration for validation
            revalidateInputDecorators(inputDeclarations, propertyName, value, instanceEnv);
        } else {
            executeBlock(stmt, instanceEnv);
        }
    }

    /**
     * Re-applies decorators (like @validate) when a component input is overridden.
     * Creates a temporary InputDeclaration with the new value and runs validation.
     */
    private void revalidateInputDecorators(Map<String, InputDeclaration> inputDeclarations,
                                           String propertyName, Object value, Environment<Object> instanceEnv) {
        var inputDecl = inputDeclarations.get(propertyName);
        if (inputDecl == null || !inputDecl.hasAnnotations()) {
            return;
        }

        // Create a value expression from the evaluated value
        var valueExpr = createValueExpression(value);
        var tempInput = inputDecl.withInit(valueExpr);

        // Create new annotations with the temp input as target
        var tempAnnotations = new LinkedHashSet<AnnotationDeclaration>();
        for (var annotation : inputDecl.getAnnotations()) {
            var tempAnnotation = annotation.copy();
            tempAnnotation.setTarget(tempInput);
            tempAnnotations.add(tempAnnotation);
        }

        // Execute annotations in instanceEnv context
        var previousEnv = this.env;
        try {
            this.env = instanceEnv;
            visitAnnotations(tempAnnotations);
        } finally {
            this.env = previousEnv;
        }
    }

    /**
     * Creates an Expression wrapper around an already-evaluated value.
     * Used for passing evaluated values to decorators that expect an Expression.
     */
    private Expression createValueExpression(Object value) {
        return switch (value) {
            case String s -> StringLiteral.of(s);
            case Integer i -> NumberLiteral.of(i);
            case Double d -> NumberLiteral.of(d);
            case Boolean b -> BooleanLiteral.bool(b);
            case List<?> list -> {
                var items = list.stream()
                        .map(item -> (Literal) createValueExpression(item))
                        .toList();
                yield ArrayExpression.array(items);
            }
            case Map<?, ?> map -> {
                var properties = new ArrayList<ObjectLiteral>();
                for (var entry : map.entrySet()) {
                    var key = StringLiteral.of(entry.getKey().toString());
                    var val = createValueExpression(entry.getValue());
                    properties.add(ObjectLiteral.object(key, val));
                }
                yield ObjectExpression.object(properties);
            }
            case null -> NullLiteral.nullLiteral();
            default -> throw new RuntimeException("Cannot create expression for value: " + value);
        };
    }

    @Override
    public Object visit(InputDeclaration input) {
        visitAnnotations(input.getAnnotations());
        var name = input.getId().string();

        if (input.hasInit()) {
            // Has a default value - use it if not already set
            if (env.get(name) == null) {
                return env.initOrAssign(name, visit(input.getInit()));
            }
            return env.lookup(name);
        }

        // No initializer - declare as null if not already set (for component declarations)
        if (env.get(name) == null) {
            return env.init(name, null);
        }
        return env.lookup(name);
    }

    private void visitAnnotations(Set<AnnotationDeclaration> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }
        annotations.forEach(this::visit);
    }

    @Override
    public Object visit(LogicalExpression expression) {
        var left = visit(expression.getLeft());
        var right = visit(expression.getRight());

        validateLogicalOperands(expression, left, right);

        return switch (expression.getOperator()) {
            case "||" -> isTruthy(left) ? left : right;
            case "&&" -> isTruthy(left) ? right : left;
            default -> throw new IllegalArgumentException(
                    "Unknown logical operator in expression: " + printer.visit(expression)
            );
        };
    }

    private void validateLogicalOperands(LogicalExpression expression, Object left, Object right) {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Null operand in logical expression: " + printer.visit(expression));
        }

        if (!(left instanceof Boolean) || !(right instanceof Boolean)) {
            throw new IllegalArgumentException("Logical expression requires boolean operands: " + printer.visit(expression));
        }
    }

    @Override
    public Object visit(AssignmentExpression expression) {
        if (expression.getLeft() instanceof MemberExpression memberExpression) {
            var instanceEnv = executeBlock(memberExpression.getObject(), env);
            if (instanceEnv instanceof ResourceValue resourceValue) {
                throw new RuntimeError("Resources can only be updated inside their block: " + resourceValue.getName());
            } else if (instanceEnv instanceof ComponentValue componentValue) {
                throw new RuntimeError("Components can only be updated inside their block: " + componentValue.getName());
            }
        } else if (expression.getLeft() instanceof SymbolIdentifier identifier) {
            return assignmentSymbol(expression, identifier);
        }
        throw new RuntimeException("Invalid assignment");
    }

    private @Nullable Object assignmentSymbol(AssignmentExpression expression, SymbolIdentifier identifier) {
        Object right = executeBlock(expression.getRight(), env);
        if (Objects.equals(expression.getOperator(), "+=")) {
            return equalComplexAssignment(identifier, right);
        } else if (right instanceof ResourceRef.Resolved resolved) {
            var res = env.assign(identifier.string(), resolved.value());
            return resolved;
        } else {
            return env.assign(identifier.string(), right);
        }
    }

    private @Nullable Object equalComplexAssignment(SymbolIdentifier identifier, Object right) {
        var existing = env.lookup(identifier.string());
        return switch (existing) {
            case Integer left when right instanceof Integer integer -> env.assign(identifier.string(), left + integer);
            case Float left when right instanceof Float aFloat -> env.assign(identifier.string(), left + aFloat);
            case Double left when right instanceof Double aDouble -> env.assign(identifier.string(), left + aDouble);
            case String str when right instanceof Number number -> env.assign(identifier.string(), str + number);
            case List list -> {
                list.add(right);
                yield list;
            }
            case null, default -> null;
        };
    }

    @Override
    public Object visit(MemberExpression expression) {
        Object object;
        try {
            object = executeBlock(expression.getObject(), env);
        } catch (NotFoundException e) {
            object = visitSchemaMember(expression);
        }

        if (expression.isComputed()) {
            // Array/object indexing: obj[index] or obj["key"]
            var index = visit(expression.getProperty());
            return switch (object) {
                case List<?> list -> visitListMember(index, list);
                case Map<?, ?> map -> visitMapMember(expression, map);
//                case ObjectValue objVal -> objVal.getProperties().get(index.toString());
                case ResourceValue resourceVal -> visitResourceArray(expression);
                case ResourceRef.Resolved resolved -> {
                    if (resolved.value() instanceof List<?> list) {
                        yield visitListMember(index, list);
                    } else {
                        yield resolved.value();
                    }
                }
                case ResourceRef.Pending pending -> {
                    // When base name doesn't exist (e.g., 'main' for @count resources),
                    // try direct lookup of the full indexed name 'main[0]'
                    var propertyName = getPropertyName(expression.getObject());
                    var fullName = format("{0}[{1}]", propertyName, index);
                    yield lookupOrPending(getInstances(), fullName);
                }
                case null, default ->
                        throw new RuntimeError("Cannot index into type: " + (object != null ? object.getClass().getSimpleName() : "null"));
            };
        } else {
            // Dot notation: obj.property
            return switch (object) {
                case SchemaValue schemaValue -> visitSchemaMember(expression);
                case ResourceValue resourceValue -> visitResourceMember(expression, resourceValue);
                case ComponentValue componentValue -> visitComponentMember(expression, componentValue);
                case Map<?, ?> map -> visitMapMember(expression, map);
                case ResourceRef.Pending pending -> visitPendingMember(expression, pending);
                case ResourceRef.Resolved resolved -> {
                    if (resolved.value() instanceof Map<?, ?> map2) {
                        yield visitMapMember(expression, map2);
                    } else {
                        yield resolved.value();
                    }
                }
                case null, default -> object;
            };
        }
    }

    private Object visitResourceArray(MemberExpression expression) {
        var identifier = switch (visit(expression.getObject())) {
            case Identifier id -> id.string();
            case ResourceValue resourceValue -> resourceValue.getName();
            default ->
                    throw new RuntimeException(format("Invalid resource array index: {0}", printer.visit(expression)));
        };
        var visit = switch (visit(expression.getProperty())) {
            case String s -> "\"" + s + "\"";
            case Number number -> number;
            case null, default ->
                    throw new TypeError("Invalid index type: %s".formatted(printer.visit(expression.getProperty())));
        };
        var format = format("{0}[{1}]", identifier, visit);
        return env.lookup(format);
    }

    private Object visitListMember(Object index, List<?> list) {
        if (!(index instanceof Number)) {
            throw new RuntimeError("Array index must be a number, got: " + index);
        }
        int i = ((Number) index).intValue();
        if (i < 0 || i >= list.size()) {
            throw new RuntimeError("Array index out of bounds: " + i + " (size: " + list.size() + ")");
        }
        return list.get(i);
    }

    private Object visitSchemaMember(MemberExpression expression) {
        var propertyName = getPropertyName(expression.getObject());
        var resources = getInstances();

        if (!ExecutionContextIn(ForStatement.class)) {
            return lookupOrPending(resources, propertyName);
        }

        // Access computed property in for loop expression without the syntax
        // See: ForResourceTest#dependsOnEarlyResource()
        if (!(ExecutionContext(ResourceStatement.class) instanceof ResourceStatement resourceStatement)) {
            return lookupOrPending(resources, propertyName);
        }

        // In a loop (or @count) we try to access an equivalent resource without using the index
        // If nothing is found we might access the resource that is not an array
        // See: CountTests#countResourceDependencyIndex()
        var indexedProperty = "%s[%s]".formatted(propertyName, resourceStatement.getIndex());
        var indexedResource = propertyOrDeferred(resources, indexedProperty);

        if (indexedResource == null) {
            return propertyOrDeferred(resources, propertyName);
        }

        if (indexedResource instanceof ResourceRef.Pending && propertyOrDeferred(resources, propertyName) instanceof ResourceValue resourceValue) {
            return resourceValue;
        }

        return indexedResource;
    }

    /**
     * Only valid for resource.property or component.property
     */
    private Object visitResourceMember(MemberExpression expression, ResourceValue resourceValue) {
        var propertyName = getPropertyName(expression.getProperty());

        // If doing complex string interpolation and trying to access resource property
        // See: ResourceStringInterpolation#stringInterpolationMemberAccess()
        if (ExecutionContextIn(StringLiteral.class)) {
            return resourceValue.lookup(propertyName);
        } else {
            var propertyValue = resourceValue.lookup(propertyName);
            // When retrieving the type of a resource through member expression, return a Resolved reference
            return ResourceRef.resolved(resourceValue, propertyValue);
        }
    }

    private Object visitMapMember(MemberExpression expression, Map<?, ?> map) {
        var propertyName = getPropertyName(expression.getProperty());
        return map.get(propertyName);
    }

    /**
     * Handles property access on component instances.
     * Only inputs and outputs can be accessed via instanceName.propertyName.
     * Resources are private and cannot be accessed from outside the component.
     * Component types (not instances) do not allow external property access.
     */
    private Object visitComponentMember(MemberExpression expression, ComponentValue componentValue) {
        var propertyName = getPropertyName(expression.getProperty());

        // Component types (name == null) do not allow external property access
        // Only instances (name != null) support public property access
        if (componentValue.getName() == null) {
            throw new RuntimeError("Cannot access property '%s' on component type. Create an instance first."
                    .formatted(propertyName));
        }

        // Check if property is publicly accessible (input or output)
        if (!componentValue.isPublicProperty(propertyName)) {
            throw new RuntimeError("Cannot access private property '%s' on component '%s'. Only inputs and outputs are accessible."
                    .formatted(propertyName, componentValue.getName()));
        }

        return componentValue.lookup(propertyName);
    }

    /**
     * Handles property access on a pending (not yet resolved) resource reference.
     * For example: main[0].name where main[0] hasn't been evaluated yet.
     *
     * @param expression the member expression being accessed
     * @param pending    the pending resource reference
     * @return a new pending reference that includes the property path
     */
    private Object visitPendingMember(MemberExpression expression, ResourceRef.Pending pending) {
        // Get the property name being accessed (e.g., "name" for main[0].name)
        var propertyName = getPropertyName(expression.getProperty());

        // Return a pending reference that includes the property path.
        // When the resource is resolved, the property will be accessed during re-evaluation.
        return ResourceRef.pending(
                pending.resourceName(),
                propertyName,
                ResourceRef.RefSource.PROPERTY_ACCESS
        );
    }

    private @NotNull String getPropertyName(Expression expression) {
        return switch (expression) {
            case SymbolIdentifier identifier -> identifier.string();
            case StringLiteral literal -> literal.getValue();
            case null, default ->
                    throw new OperationNotImplementedException("Membership expression not implemented for: " + printer.visit(expression));
        };
    }

    @Override
    public Object visit(ResourceStatement statement) {
        if (!contextStackContains(ContextStack.Decorator)) {
            // needs to be above isCounted because @count marks the resourceStatement as counted
            visitAnnotations(statement.getAnnotations());
        }
        if (statement.isCounted()) {
            return statement;
        }

        validate(statement);
        push(ContextStack.Resource);

        try {
            // SchemaValue already installed globally when evaluating a SchemaDeclaration
            // This means the schema must be declared before the resource
            var installedSchema = (SchemaValue) executeBlock(statement.getType(), env);
            setResourceName(statement);

            var value = statement.isEvaluating()
                    ? statement.getValue() // Notifying existing resource that its dependencies were satisfied
                    : initResource(statement, installedSchema, installedSchema.getEnvironment());

            value.setProviders(statement.getProviders());
            value.setTags(statement.getTags());

            return resolveDependencies(statement, value);
        } finally {
            pop(ContextStack.Resource);
        }
    }

    private void validate(ResourceStatement resource) {
        if (resource.getName() == null) {
            throw new InvalidInitException("Resource does not have a name: " + printer.visit(resource));
        }
        if (contextStackContains(ContextStack.FUNCTION)) {
            throw new InvalidInitException("Resource cannot be declared inside a function: " + printer.visit(resource));
        }
    }

    private void setResourceName(ResourceStatement resource) {
        if (ExecutionContext(ForStatement.class) instanceof ForStatement forStatement) {
            Object visit = visit(forStatement.getItem());
            switch (visit) {
                case String s -> resource.setIndex("\"%s\"".formatted(s));
                case Number number -> resource.setIndex(number);
                case Map<?, ?> map -> {
                    if (forStatement.getIndex() != null) {
                        resource.setIndex(visit(forStatement.getIndex()));
                    } else {
                        resource.setIndex(map);
                    }
                }
                default -> throw new TypeError("Invalid index type: %s".formatted(visit.getClass()));
            }
        }
    }

    private ResourceValue initResource(ResourceStatement statement, SchemaValue installedSchema, Environment<Object> typeEnvironment) {
        // clone all properties from schema properties to the new resource
        var path = resourceName(statement); // install indexed resource name in environment ex: resName["prod"] or resName[0]
        if (ExecutionContext(ComponentStatement.class) instanceof ComponentStatement componentStatement) {
            path.setParentPath(ResourcePath.builder()
                    .name(componentStatement.name())
                    .segments(new ArrayList<>())
                    .build());
        }
        var resourceEnv = new Environment<>(path.getName(), env, typeEnvironment.getVariables());
        var instance = ResourceValue.resourceValue(path.getName(), resourceEnv, installedSchema, statement.getExisting());
        instance.setPath(path);
        try {
            // init any kind of new resource
            initInstance(instance);
            statement.setValue(instance);
        } catch (DeclarationExistsException e) {
            throw new DeclarationExistsException("Resource already exists: \n%s".formatted(printer.visit(statement)));
        }
        return instance;
    }

    /**
     * Resolves resource dependencies through a 4-phase process:
     * 1. Collect all dependencies from property evaluations and decorators
     * 2. Validate no cyclic dependencies exist
     * 3. Register observers for unresolved (deferred) dependencies
     * 4. Notify dependent resources if this resource is fully evaluated
     *
     * <p>This implements an optimized observer pattern for lazy dependency resolution.
     * See docs/DEPENDENCY_RESOLUTION.md for detailed architecture and sequence diagrams.
     *
     * @param resource The resource statement being evaluated
     * @param instance The resource value instance
     * @return The resource instance with dependencies resolved
     * @see DeferredObservable
     * @see ResourceStatement#notifyDependencyResolved
     */
    private ResourceValue resolveDependencies(ResourceStatement resource, ResourceValue instance) {
        resource.setEvaluated(true);
        resource.setEvaluating(false);

        var deferredDependencies = collectResourceDependencies(resource, instance);
        validateNoCycles(instance);
        registerDeferredObservers(resource, deferredDependencies);
        notifyDependentResources(resource);

        return instance;
    }

    /**
     * Collects all dependencies from resource properties and @dependsOn decorators.
     * Returns a list of pending (unresolved) dependencies.
     */
    private List<ResourceRef.Pending> collectResourceDependencies(ResourceStatement resource, ResourceValue instance) {
        var deferredList = new ArrayList<ResourceRef.Pending>();

        // Collect dependencies from property evaluations
        for (Statement it : resource.getArguments()) {
            validateNotCloudProperty(it, instance, resource);
            var result = executeBlock(it, instance.getProperties());
            addDependency(resource, instance, result, deferredList);
        }

        // Collect dependencies from @dependsOn decorators
        for (Expression it : resource.getDependencies()) {
            Object result;
            if (it instanceof Identifier identifier) {
                result = env.containsKey(identifier.string())
                        ? executeBlock(it, env)
                        : ResourceRef.pending(identifier.string(), null, ResourceRef.RefSource.DEPENDS_ON);
            } else {
                result = executeBlock(it, env);
            }
            addDependency(resource, instance, result, deferredList);
        }

        return deferredList;
    }

    /**
     * Validates that no cyclic dependencies exist in the dependency graph.
     * Throws CycleException if a cycle is detected.
     */
    private void validateNoCycles(ResourceValue instance) {
        if (instance.hasDependencies()) {
            CycleDetection.detect(instance, this);
        }
    }

    /**
     * Registers this resource as an observer for all pending dependencies.
     * Increments the unresolved dependency counter for each pending dependency.
     */
    private void registerDeferredObservers(ResourceStatement resource, List<ResourceRef.Pending> pendingDependencies) {
        for (ResourceRef.Pending pending : pendingDependencies) {
            deferredObservable.addObserver(resource, pending);
            resource.incrementUnresolvedDependencyCount();
        }
    }

    /**
     * Notifies resources that depend on this one if this resource is fully evaluated.
     */
    private void notifyDependentResources(ResourceStatement resource) {
        if (resource.isEvaluated()) {
            deferredObservable.notifyObservers(this, resource.getValue().getPath().toSegmentName());
        }
    }

    /**
     * Validates that the statement is not trying to assign a value to a @cloud property.
     * Cloud properties are set by the cloud provider, not by users.
     */
    private void validateNotCloudProperty(Statement statement, ResourceValue instance, ResourceStatement resource) {
        if (!(statement instanceof ExpressionStatement exprStmt)) {
            return;
        }
        if (!(exprStmt.getStatement() instanceof AssignmentExpression assignment)) {
            return;
        }

        var propertyName = getPropertyName(assignment.getLeft());
        var schema = instance.getSchema();

        if (schema != null && schema.isCloudProperty(propertyName)) {
            var resourceName = getPropertyName(resource.getName());
            var message = Ansi.ansi()
                    .fgRed().a("Error: ").reset()
                    .a("Cannot set ")
                    .fgYellow().a("@cloud").reset()
                    .a(" property '")
                    .fgCyan().a(propertyName).reset()
                    .a("' in resource '")
                    .fgCyan().a(resourceName).reset()
                    .a("'. Cloud properties are generated by the cloud provider after apply.")
                    .toString();
            throw new InvalidInitException(message);
        }
    }

    /**
     * Adds a dependency to the resource based on the evaluation result.
     * Handles three types: Pending (unresolved), Resolved (resolved), and ResourceValue (from @dependsOn).
     */
    private void addDependency(ResourceStatement resource, ResourceValue instance, Object result, List<ResourceRef.Pending> deferredList) {
        switch (result) {
            case ResourceRef.Pending pending -> {
                instance.addDependency(pending.resourceName());
                resource.setEvaluated(false);
                deferredList.add(pending);
            }
            case ResourceRef.Resolved resolved -> instance.addDependency(resolved.resource().getPath().toSegmentName());
            case ResourceValue resourceValue -> instance.addDependency(resourceValue.getName());
            case null, default -> {
            }
        }
    }

    @Override
    public Object visit(ThisExpression expression) {
        return null;
    }

    @Override
    public Object visit(IfStatement statement) {
        var eval = (Boolean) executeBlock(statement.getTest(), env);
        if (eval) {
            return executeBlock(statement.getConsequent(), env);
        } else {
            Statement alternate = statement.getAlternate();
            if (alternate == null) {
                return null;
            }
            return executeBlock(alternate, env);
        }
    }

    @Override
    public Object visit(WhileStatement statement) {
        Object result = NullValue.of();

        while (isTruthy(visit(statement.getTest()))) {
            result = executeBlock(statement.getBody(), env);
        }
        return result;
    }

    /**
     * We don't support the implicit "each" when iterating over a list. The variable is explicitly defined so you can reference it
     * We follow the explicit is better than implicit design. Compared to competitors where they declare it like for_each = toset(["eastus", "westus"])
     * they need an implicit variable to be declared (each). We think this is confusing for the user and adds extra complexity and
     * things to be remembered
     */
    public Object visit(ForStatement statement) {
        push(statement);

        try {
            if (statement.hasRange()) {
                return ForWithRange(statement);
            }

            if (statement.hasArray()) {
                return executeForArray(statement);
            }

            throw new OperationNotImplementedException("For statement not implemented");
        } finally {
            pop(statement);
        }
    }

    private Object executeForArray(ForStatement statement) {
        var array = visit(statement.getArray());

        if (!(array instanceof List<?> list)) {
            throw new OperationNotImplementedException("For statement requires an array");
        }

        var forEnv = new Environment<>("for", env);
        Object result = null;

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);

            forInit(forEnv, statement.getItem(), item);
            forInit(forEnv, statement.getIndex(), i);

            // Flatten map properties into the loop environment
            if (item instanceof Map<?, ?> map) {
                flattenMapProperties(forEnv, statement.getItem(), map);
            }

            result = executeBlock(statement.getBody(), forEnv);
        }

        return result;
    }

    private void flattenMapProperties(Environment<Object> forEnv, Identifier itemIdentifier, Map<?, ?> map) {
        map.forEach((key, value) ->
                forInit(forEnv, Identifier.id(itemIdentifier.string() + "." + key), value)
        );
    }

    private Object ForWithRange(ForStatement statement) {
        var range = statement.getRange();
        var min = range.getMinimum();
        var max = range.getMaximum();

        // No iterations? Fast exits keep intent obvious.
        if (min >= max) {
            if (statement.isBodyBlock()) return null; // same "last" semantics
            if (statement.getBody() != null) return List.of();
            throw new OperationNotImplementedException("For statement operation not implemented");
        }
        // Seed loop env with the item var (as before)
        var forEnv = new Environment<>(env, Map.of(statement.getItem().string(), min));

        if (statement.isBodyBlock()) { // Block-style: execute body each iteration, return last result
            return ExecuteForBodyBlock(statement, min, max, forEnv);
        } else if (statement.getBody() != null) { // Expr-style: build a list from body results (with If support)
            return ExecuteForBody(statement, max, min, env);
        } else {
            throw new OperationNotImplementedException("For statement operation not implemented");
        }
    }

    private @NotNull ArrayList<Object> ExecuteForBody(ForStatement statement, Integer max, Integer min, Environment<Object> forEnv) {
        var body = statement.getBody();
        var out = new ArrayList<>(max - min);
        for (int i = min; i < max; i++) {
            initIteration(forEnv, statement, i);
            var iterEnv = new Environment<>(forEnv);

            if (body instanceof IfStatement iff) {
                var result = executeBlock(iff, iterEnv);
                if (result != null) {
                    out.add(result);
                }
            } else {
                out.add(executeBlock(body, iterEnv));
            }
        }
        return out;
    }

    private @Nullable Object ExecuteForBodyBlock(ForStatement statement, Integer min, Integer max, Environment<Object> forEnv) {
        Object last = null;
        var body = statement.discardBlock();
        for (int i = min; i < max; i++) {
            initIteration(forEnv, statement, i);
            last = executeBlock(body, new Environment<>(forEnv));
        }
        return last;
    }

    private void initIteration(Environment<Object> env, ForStatement st, int i) {
        forInit(env, st.getIndex(), i);
        forInit(env, st.getItem(), i);
    }

    @Override
    public Object visit(SchemaDeclaration expression) {
        visitAnnotations(expression.getAnnotations());
        var environment = new Environment<>(env);
        var schemaValue = SchemaValue.of(expression.getName(), environment);
        push(ContextStack.Schema);

        for (var property : expression.getProperties()) {
            // Track @cloud properties and validate no initialization
            if (property.isCloudGenerated()) {
                schemaValue.addCloudProperty(property.name());
                if (property.hasInit()) {
                    throw new InvalidInitException(
                            "@cloud property '%s' cannot have an initialization value in schema '%s'"
                                    .formatted(property.name(), expression.getName().string())
                    );
                }
                // Initialize cloud properties with null - they'll be set by the cloud provider
                environment.init(property.name(), null);
                continue;
            }

            switch (property.init()) {
                case BlockExpression blockExpression -> executeBlock(blockExpression.getExpression(), environment);
                case null, default -> environment.init(property.name(), visit(property.init()));
            }
        }
        pop(ContextStack.Schema);
        return env.init(expression.getName().string(), schemaValue);
    }

    @Override
    public Object visit(UnaryExpression expression) {
        if (!(expression.getOperator() instanceof String op)) {
            throw new RuntimeException("Operator could not be evaluated");
        }

        return switch (op) {
            case "++" -> incrementValue(expression);
            case "--" -> decrementValue(expression);
            case "-" -> negateValue(expression);
            case "!" -> notValue(expression);
            default -> throw new RuntimeException("Operator could not be evaluated: " + op);
        };
    }

    private Object incrementValue(UnaryExpression expression) {
        Object value = executeBlock(expression.getValue(), env);
        return switch (value) {
            case Integer i -> i + 1;
            case Double d -> d + 1;
            case null, default -> throw new RuntimeException("Invalid unary operator: " + value);
        };
    }

    private Object decrementValue(UnaryExpression expression) {
        Object value = executeBlock(expression.getValue(), env);
        return switch (value) {
            case Integer i -> i - 1;
            case Double d -> BigDecimal.valueOf(d).subtract(BigDecimal.ONE).doubleValue();
            case null, default -> throw new RuntimeException("Invalid unary operator: " + value);
        };
    }

    private Object negateValue(UnaryExpression expression) {
        Object value = executeBlock(expression.getValue(), env);
        return switch (value) {
            case Integer i -> -i;
            case Double d -> BigDecimal.valueOf(d).negate().doubleValue();
            case null, default -> throw new RuntimeException("Invalid unary operator: " + value);
        };
    }

    private Object notValue(UnaryExpression expression) {
        Object value = executeBlock(expression.getValue(), env);
        if (value instanceof Boolean b) {
            return !b;
        }
        throw new RuntimeException("Invalid not operator: " + value);
    }

    @Override
    public Object visit(VarDeclaration expression) {
        visitAnnotations(expression.getAnnotations());
        String symbol = expression.getId().string();
        Object value = null;
        if (expression.hasInit()) {
            value = executeBlock(expression.getInit(), env);
        }
        expect(expression, value);
        if (value instanceof ResourceRef.Resolved resolved) { // a resolved reference to another resource
            return env.init(symbol, resolved.value());
        }
        return env.init(symbol, value);
    }

    private void expect(DependencyHolder expression, Object value) {
        if (!expression.hasType()) {
            return;
        }

        validateArrayType(expression);

        var type = visit(expression.getType());
        if (type instanceof Set<?> set) {
            validateValueInSet(expression, value, set);
        }
    }

    private void validateArrayType(DependencyHolder expression) {
        if (expression.getInit() instanceof ArrayExpression
            && !(expression.getType() instanceof ArrayTypeIdentifier)) {
            throw new IllegalArgumentException(
                    "Invalid type for array: %s".formatted(printer.visit(expression.getId()))
            );
        }
    }

    private void validateValueInSet(DependencyHolder expression, Object value, Set<?> validValues) {
        boolean isValid = value instanceof Collection<?> collection
                ? validValues.containsAll(collection)
                : validValues.contains(value);

        if (!isValid) {
            throw new IllegalArgumentException(format(
                    "Invalid value `{0}` for type `{1}`. Valid values `{2}`",
                    value,
                    expression.getType().string(),
                    validValues
            ));
        }
    }

    @Override
    public Object visit(OutputDeclaration expression) {
        visitAnnotations(expression.getAnnotations());
        outputs.add(expression); // collect outputs for printing after apply
        if (!expression.hasInit()) {
            throw new MissingOutputException("Output type without an init value: " + printer.visit(expression));
        }
        var res = visit(expression.getInit());

        // Store output in current environment for component member access
        var name = expression.name();
        env.initOrAssign(name, res);

        if (res instanceof ResourceRef.Resolved resolved && resolved.value() == null) {
            return resolved;
        } else {
            return res;
        }
    }

    public String printOutputs(Map<String, Map<String, Object>> resources) {
        String value = null;
        System.out.println(Ansi.ansi().a(Ansi.Attribute.INTENSITY_BOLD).a("Final Outputs:").toString());
        for (OutputDeclaration output : outputs) {
            var object = resolveResource(resources, output);
            output.setResolvedValue(object);
            value = printer.visit(output);
            System.out.println(value);
        }
        return value;
    }

    private Object resolveResource(Map<String, Map<String, Object>> resources, OutputDeclaration output) {
        if (output.getInit() instanceof MemberExpression memberExpression) {
            if (visit(output.getInit()) instanceof ResourceRef.Resolved resolved) {
                var resource = resolved.resource();
                var propertyName = getPropertyName(memberExpression.getProperty());
                return resources.get(resource.getName()).get(propertyName);
            }
        }
        return visit(output.getInit());
    }

    @Override
    public Object visit(ValDeclaration expression) {
        String symbol = expression.getId().string();
        Object value = null;
        if (super.peek(ContextStack.Resource)) {
            if (!expression.hasInit()) {
                throw new InvalidInitException("Val type must be initialised: " + expression.getId().string() + " is null");
            }
        }
        if (expression.hasInit()) {// resource/schema can both have init but is only mandatory in the resource
            value = executeBlock(expression.getInit(), env);
        }
        if (value instanceof ResourceRef.Resolved resolved) { // a resolved reference to another resource
            return env.init(symbol, resolved.value());
        }
        return env.init(symbol, value);
    }

    @Override
    public Object visit(ObjectExpression expression) {
        var map = new HashMap<String, Object>(expression.getProperties().size());
        for (ObjectLiteral property : expression.getProperties()) {
            var object = (ObjectLiteralPair) visit(property);
            map.put(object.key(), object.value());
        }
        return map;
    }

    @Override
    public Object visit(ArrayExpression expression) {
        if (expression.hasForStatement()) {
            return executeBlock(expression.getForStatement(), env);
        }
        if (expression.hasItems()) {
            return expression.getItems().stream().map(this::visit).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }

    }

    @Override
    public Object visit(AnnotationDeclaration expression) {
        push(ContextStack.Decorator);
        var decorator = decorators.get(expression.name());
        if (decorator != null) {
            decorator.execute(expression);
        } else {
            log.warn("Unknown decorator: {}", expression.name());
        }
        pop(ContextStack.Decorator);
        if (expression.getValue() != null) {
            return expression.getValue();
        } else if (expression.getArgs() != null) {
            return expression.getArgs();
        } else if (expression.getNamedArgs() != null) {
            return expression.getNamedArgs();
        } else if (expression.getObject() != null) {
            return expression.getObject();
        } else {
            return NullValue.of();
        }
    }

    @Override
    public Object visit(Program program) {
        try {
            Object lastEval = new NullValue();

            for (Statement i : program.getBody()) {
                lastEval = executeBlock(i, env);
            }
            topologySortResources();

            return lastEval;
        } catch (RuntimeException e) {
            errors.add(e);
            throw e;
        }
    }

    private void topologySortResources() {
        topologySort(getInstances());
    }

    @Override
    public Object visit(Type type) {
        return type;
    }

    @Override
    public Object visit(InitStatement statement) {
        return visit(fun(statement.getName(), statement.getParams(), statement.getBody()));
    }

    @Override
    public Object visit(FunctionDeclaration declaration) {
        push(ContextStack.FUNCTION);
        var name = declaration.getName();
        var params = declaration.getParams();
        var body = declaration.getBody();
        Object init = env.init(name.string(), FunValue.of(name, params, body, env));
        pop(ContextStack.FUNCTION);
        return init;
    }

    @Override
    public Object visit(ExpressionStatement statement) {
        return executeBlock(statement.getStatement(), env);
    }

    Object executeBlock(List<Statement> statements, Environment<Object> environment) {
        Environment<Object> previous = this.env;
        try {
            this.env = environment;
            Object res = null;
            for (Statement statement : statements) {
                res = visit(statement);
            }
            return res;
        } finally {
            this.env = previous;
        }
    }

    Object executeBlock(Expression expression, Environment<Object> environment) {
        super.push(expression);
        Environment<Object> previous = this.env;
        try {
            this.env = environment;
            return super.visit(expression);
        } finally {
            this.env = previous;
            super.pop(expression);
        }
    }

    Object executeBlock(Statement statement, Environment<Object> environment) {
        Environment<Object> previous = this.env;
        try {
            this.env = environment;
            return visit(statement);
        } catch (RuntimeException e) {
            throw e;
        } finally {
            this.env = previous;
        }
    }

    public SchemaValue getSchema(String vm) {
        return (SchemaValue) env.lookup(vm);
    }

    public Object getVar(String y) {
        return env.lookup(y);
    }

    public boolean hasVar(String x) {
        return env.hasVar(x);
    }

    public Object getFun(String myFun) {
        return env.lookup(myFun);
    }

    public ComponentValue getComponent(String s) {
        return (ComponentValue) env.lookup(s);
    }

    public Map<String, ResourceValue> getInstances() {
        return env.getRoot().getResources();
    }
}
