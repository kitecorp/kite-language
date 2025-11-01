package io.kite.Runtime;

import io.kite.ContextStack;
import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.Frontend.Lexer.Tokenizer;
import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parse.Literals.ObjectLiteral.ObjectLiteralPair;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Parser;
import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Runtime.Decorators.*;
import io.kite.Runtime.Environment.ActivationEnvironment;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Functions.Cast.*;
import io.kite.Runtime.Functions.DateFunction;
import io.kite.Runtime.Functions.Numeric.*;
import io.kite.Runtime.Functions.PrintFunction;
import io.kite.Runtime.Functions.PrintlnFunction;
import io.kite.Runtime.Values.*;
import io.kite.Runtime.exceptions.*;
import io.kite.Runtime.interpreter.OperatorComparator;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.Type;
import io.kite.Visitors.StackVisitor;
import io.kite.Visitors.SyntaxPrinter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static io.kite.Frontend.Parser.Statements.FunctionDeclaration.fun;
import static io.kite.Runtime.CycleDetection.topologySort;
import static io.kite.Runtime.CycleDetectionSupport.propertyOrDeferred;
import static io.kite.Runtime.interpreter.OperatorComparator.compare;
import static io.kite.Utils.BoolUtils.isTruthy;
import static java.text.MessageFormat.format;

@Log4j2
public final class Interpreter extends StackVisitor<Object> {
    @Getter
    private final SyntaxPrinter printer;
    private final DeferredObservable deferredObservable;
    @Getter
    private final List<OutputDeclaration> outputs;
    @Getter
    private final Map<String, DecoratorInterpreter> decorators;
    @Getter
    private final List<RuntimeException> errors;
    Parser parser = new Parser();
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Setter
    private Map<String, ResourceValue> instances;
    @Getter
    private Environment<Object> env;

    public Interpreter() {
        this(new Environment<>());
    }

    public Interpreter(Environment<Object> environment) {
        this.env = environment;
        this.outputs = new ArrayList<>();
        this.printer = new SyntaxPrinter();
        this.deferredObservable = new DeferredObservable();
        this.instances = new LinkedHashMap<>();

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

        // number
        this.env.init("pow", new PowFunction());
        this.env.init("min", new MinFunction());
        this.env.init("max", new MaxFunction());
        this.env.init("ceil", new CeilFunction());
        this.env.init("floor", new FloorFunction());
        this.env.init("abs", new AbsFunction());
        this.env.init("date", new DateFunction());
//        this.globals.init("Vm", SchemaValue.of("Vm", new Environment(env, new Vm())));

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

    public ResourceValue initInstance(ResourceValue instance) {
        var contains = this.instances.containsKey(instance.name()); // todo performance tip: replace contains with put only
        if (contains) {
            throw new DeclarationExistsException(">" + instance.name() + "< already exists in schema");
        }
        this.env.init(instance.name(), instance);
        return this.instances.put(instance.name(), instance);
    }

    @Nullable
    public ResourceValue getInstance(String name) {
        return instances.get(name);
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
        } catch (
                NotFoundException e) { // when not finding the variable in the correct scope, try the most nested scope again
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
                    var list = parser.produceStatements(new Tokenizer().tokenize(interpolationVar));
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
        var res = getEnv().init(expression.getName(), values);
        return res;
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
        throw new RuntimeError(new Token(expression.getCallee(), TokenType.Fun), "Can only call functions and classes.");
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
    public Object visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public Object visit(ComponentStatement expression) {
        throw new RuntimeException("Invalid component statement");
    }

    @Override
    public Object visit(InputDeclaration input) {
        if (input.hasInit() && env.get(input.name()) == null) {
            return env.initOrAssign(input.getId().string(), visit(input.getInit()));
        }
        return env.lookup(input.name());
    }

    @Override
    public Object visit(LogicalExpression expression) {
        var left = visit(expression.getLeft());
        var right = visit(expression.getRight());

        validateLogicalOperands(expression, left, right);

        return switch (expression.getOperator()) {
            case Logical_Or -> isTruthy(left) ? left : right;
            case Logical_And -> isTruthy(left) ? right : left;
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
            }
        } else if (expression.getLeft() instanceof SymbolIdentifier identifier) {
            return assignmentSymbol(expression, identifier);
        }
        throw new RuntimeException("Invalid assignment");
    }

    private @Nullable Object assignmentSymbol(AssignmentExpression expression, SymbolIdentifier identifier) {
        Object right = executeBlock(expression.getRight(), env);
        if (Objects.equals(expression.getOperator(), TokenType.Equal_Complex.getField())) {
            return equalComplexAssignment(identifier, right);
        } else if (right instanceof Dependency dependency) {
            var res = env.assign(identifier.string(), dependency.value());
            return dependency;
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
                case ResourceValue resourceVal -> visitResourceMember(expression, resourceVal);
                case Dependency dependency -> {
                    if (dependency.value() instanceof List<?> list) {
                        yield visitListMember(index, list);
                    } else {
                        yield dependency.value();
                    }
                }
                case null, default ->
                        throw new RuntimeError("Cannot index into type: " + (object != null ? object.getClass().getSimpleName() : "null"));
            };
        } else {
            // Dot notation: obj.property
            return switch (object) {
                case SchemaValue schemaValue -> visitSchemaMember(expression);
                case ResourceValue resourceValue -> visitResourceMember(expression, resourceValue);
                case Map<?, ?> map -> visitMapMember(expression, map);
                case Deferred deferred -> visitDeferredMember(expression, deferred);
                case Dependency dependency -> {
                    if (dependency.value() instanceof Map<?, ?> list) {
                        yield visitMapMember(expression, list);
                    } else {
                        yield dependency.value();
                    }
                }
                case null, default -> object;
            };
        }
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

        if (!ExecutionContextIn(ForStatement.class)) {
            return propertyOrDeferred(instances, propertyName);
        }

        // Access computed property in for loop expression without the syntax
        // See: ForResourceTest#dependsOnEarlyResource()
        if (!(ExecutionContext(ResourceStatement.class) instanceof ResourceStatement resourceStatement)) {
            return propertyOrDeferred(instances, propertyName);
        }

        // In a loop (or @count) we try to access an equivalent resource without using the index
        // If nothing is found we might access the resource that is not an array
        // See: CountTests#countResourceDependencyIndex()
        var indexedProperty = "%s[%s]".formatted(propertyName, resourceStatement.getIndex());
        var indexedResource = propertyOrDeferred(instances, indexedProperty);

        if (indexedResource == null) {
            return propertyOrDeferred(instances, propertyName);
        }

        if (indexedResource instanceof Deferred && propertyOrDeferred(instances, propertyName) instanceof ResourceValue resourceValue) {
            return resourceValue;
        }

        return indexedResource;
    }

    private Object visitResourceMember(MemberExpression expression, ResourceValue resourceValue) {
        var propertyName = getPropertyName(expression.getProperty());

        // If doing complex string interpolation and trying to access resource property
        // See: ResourceStringInterpolation#stringInterpolationMemberAccess()
        if (ExecutionContextIn(StringLiteral.class)) {
            return resourceValue.lookup(propertyName);
        }

        var propertyValue = resourceValue.lookup(propertyName);

        // When retrieving the type of a resource through member expression, return a Dependency
        return new Dependency(resourceValue, propertyValue);
    }

    private Object visitMapMember(MemberExpression expression, Map<?, ?> map) {
        var propertyName = getPropertyName(expression.getProperty());
        return map.get(propertyName);
    }

    private Object visitDeferredMember(MemberExpression expression, Deferred deferred) {
        if (expression.getObject() instanceof MemberExpression memberExpression) {
            var key = executeBlock(expression.getProperty(), env);
            var computedProperty = deferred.resource() + "[" + key + "]";
            memberExpression.setProperty(SymbolIdentifier.id(computedProperty));
            return visit(memberExpression);
        }
        return deferred;
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
    public Object visit(ResourceStatement resource) {
        if (resource.isCounted()) {
            return resource;
        }

        validate(resource);
        push(ContextStack.Resource);

        try {
            // SchemaValue already installed globally when evaluating a SchemaDeclaration
            // This means the schema must be declared before the resource
            var installedSchema = (SchemaValue) executeBlock(resource.getType(), env);
            setResourceName(resource);

            var value = resource.isEvaluating()
                    ? resource.getValue() // Notifying existing resource that its dependencies were satisfied
                    : initResource(resource, installedSchema, installedSchema.getEnvironment());

            value.setProviders(resource.getProviders());
            value.setTag(resource.getTags());

            return resolveDependencies(resource, value);
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

    private ResourceValue initResource(ResourceStatement resource, SchemaValue installedSchema, Environment<Object> typeEnvironment) {
        // clone all properties from schema properties to the new resource
        var resourceEnv = new Environment<>(env, typeEnvironment.getVariables());
        String name = resourceName(resource);
        var res = ResourceValue.resourceValue(name, resourceEnv, installedSchema, resource.getExisting());
        try {
            // init any kind of new resource
            initInstance(res);
            resource.setValue(res);
        } catch (DeclarationExistsException e) {
            throw new DeclarationExistsException("Resource already exists: \n%s".formatted(printer.visit(resource)));
        }
        return res;
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
     * Returns a list of deferred (unresolved) dependencies.
     */
    private List<Deferred> collectResourceDependencies(ResourceStatement resource, ResourceValue instance) {
        var deferredList = new ArrayList<Deferred>();

        // Collect dependencies from property evaluations
        for (Statement it : resource.getArguments()) {
            var result = executeBlock(it, instance.getProperties());
            addDependency(resource, instance, result, deferredList);
        }

        // Collect dependencies from @dependsOn decorators
        for (Expression it : resource.getDependencies()) {
            Object result;
            if (it instanceof Identifier identifier) {
                result = env.containsKey(identifier.string())
                        ? executeBlock(it, env)
                        : new Deferred(identifier.string());
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
     * Registers this resource as an observer for all deferred dependencies.
     * Increments the unresolved dependency counter for each deferred dependency.
     */
    private void registerDeferredObservers(ResourceStatement resource, List<Deferred> deferredDependencies) {
        for (Deferred deferred : deferredDependencies) {
            deferredObservable.addObserver(resource, deferred);
            resource.incrementUnresolvedDependencyCount();
        }
    }

    /**
     * Notifies resources that depend on this one if this resource is fully evaluated.
     */
    private void notifyDependentResources(ResourceStatement resource) {
        if (resource.isEvaluated()) {
            deferredObservable.notifyObservers(this, resourceName(resource));
        }
    }

    /**
     * Adds a dependency to the resource based on the evaluation result.
     * Handles three types: Deferred (unresolved), Dependency (resolved), and ResourceValue (from @dependsOn).
     */
    private void addDependency(ResourceStatement resource, ResourceValue instance, Object result, List<Deferred> deferredList) {
        switch (result) {
            case Deferred deferred -> {
                instance.addDependency(deferred.resource());
                resource.setEvaluated(false);
                deferredList.add(deferred);
            }
            case Dependency dependency -> instance.addDependency(dependency.resource().getName());
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

        var forEnv = new Environment<>(env);
        Object result = null;

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);

            forInit(forEnv, statement.getIndex(), i);
            forInit(forEnv, statement.getItem(), item);

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
                forInit(forEnv, Identifier.symbol(itemIdentifier.string() + "." + key), value)
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
            return ExecuteForBody(statement, max, min, forEnv);
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
        var environment = new Environment<>(env);
        push(ContextStack.Schema);

        for (var property : expression.getProperties()) {
            switch (property.init()) {
                case BlockExpression blockExpression -> executeBlock(blockExpression.getExpression(), environment);
                case null, default -> environment.init(property.name(), visit(property.init()));
            }
        }
        pop(ContextStack.Schema);
        var name = expression.getName();
        return env.init(name.string(), SchemaValue.of(name, environment)); // install the type into the global env
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
        String symbol = expression.getId().string();
        Object value = null;
        if (expression.hasInit()) {
            value = executeBlock(expression.getInit(), env);
        }
        expect(expression, value);
        if (value instanceof Dependency dependency) { // a dependency access on another resource
            return env.init(symbol, dependency.value());
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
    public Object visit(OutputDeclaration input) {
        outputs.add(input); // just collect the outputs since they need to be evaluated after the program is run
        if (!input.hasInit()) {
            throw new MissingOutputException("Output type without an init value: " + printer.visit(input));
        }
        var res = visit(input.getInit());
        if (res instanceof Dependency value && value.value() == null) {
            return value;
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
            if (visit(output.getInit()) instanceof Dependency dependency) {
                var resource = dependency.resource();
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
        if (value instanceof Dependency dependency) { // a dependency access on another resource
            return env.init(symbol, dependency.value());
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
        var decorator = decorators.get(expression.name());
        if (decorator != null) {
            decorator.execute(expression);
        } else {
            log.warn("Unknown decorator: {}", expression.name());
        }
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

            if (ParserErrors.hadErrors()) {
                return null;
            }
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
        topologySort(instances);
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
        return instances;
    }
}
