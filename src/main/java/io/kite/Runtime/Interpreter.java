package io.kite.Runtime;

import io.kite.ContextStack;
import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parse.Literals.ObjectLiteral.ObjectLiteralPair;
import io.kite.Frontend.Parser.Expressions.*;
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
import io.kite.Visitors.SyntaxPrinter;
import io.kite.Visitors.Visitor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static io.kite.Frontend.Parser.Statements.FunctionDeclaration.fun;
import static io.kite.Runtime.interpreter.OperatorComparator.compare;
import static io.kite.Utils.BoolUtils.isTruthy;
import static java.text.MessageFormat.format;

@Log4j2
public final class Interpreter implements Visitor<Object> {
    private final Deque<Callstack> callstack;

    @Getter
    private final SyntaxPrinter printer;
    private final DeferredObservable deferredObservable;
    /**
     * Used to track where are we in the execution of the program. Are we in an for statement? or in a Schema declaration? in a resource declaration?
     */
    private final Deque<ContextStack> contextStacks;
    @Getter
    private final List<OutputDeclaration> outputs;
    private final Map<String, DecoratorInterpreter> decoratorInterpreter;
    @Getter
    private final List<RuntimeException> errors;
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
        this.callstack = new ArrayDeque<>();
        this.contextStacks = new ArrayDeque<>();
        this.errors = new ArrayList<>();
        this.decoratorInterpreter = new HashMap<>();

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

        this.decoratorInterpreter.put("minValue", new MinValueDecorator());
        this.decoratorInterpreter.put("maxValue", new MaxValueDecorator());
        this.decoratorInterpreter.put("maxLength", new MaxLengthDecorator());
        this.decoratorInterpreter.put("minLength", new MinLengthDecorator());
        this.decoratorInterpreter.put("description", new DescriptionDecorator());
        this.decoratorInterpreter.put("sensitive", new SensitiveDecorator());
        this.decoratorInterpreter.put("count", new CountDecorator());
    }

    private static @Nullable Object getProperty(SchemaValue schemaValue, String name) {
        if (schemaValue.getInstances().get(name) == null) {
            // if instance was not installed yet -> it will be installed later so we return a deferred object
            return new Deferred(schemaValue, name);
        } else {
            return schemaValue.getInstances().lookup(name);
        }
    }

    private static void forInit(Environment<Object> forEnv, Identifier index, Object i) {
        if (index != null) {
            forEnv.initOrAssign(index.string(), i);
        }
    }

    private boolean ExecutionContextIn(Class<ForStatement> forStatementClass) {
        for (Callstack next : callstack) {
            if (next.getClass().equals(forStatementClass)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private Callstack ExecutionContext(Class<?> forStatementClass) {
        for (Callstack next : callstack) {
            if (next.getClass().equals(forStatementClass)) {
                return next;
            }
        }
        return null;
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
        if (expression != null) {
            callstack.push(expression);
        }
        var res = executeBlock(expression, env);
        if (expression != null) {
            callstack.pop();
        }
        return res;
    }

    @Override
    public Object visit(@Nullable Statement statement) {
        if (statement != null) {
            callstack.push(statement);
        }
        var res = Visitor.super.visit(statement);
        if (statement != null) {
            callstack.pop();
        }
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
        return env.lookup(expression.string(), expression.getHops());
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
            var hops = contextStacks.peek() == ContextStack.Resource ? 1 : 0;
            var values = new ArrayList<String>(expression.getInterpolationVars().size());
            for (String interpolationVar : expression.getInterpolationVars()) {
                var value = env.lookup(interpolationVar, hops);
                values.add(value.toString());
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

        if (left == null || right == null) {
            throw new IllegalArgumentException("Left expression does not exist: " + printer.visit(expression));
        }
        if (!(left instanceof Boolean) || !(right instanceof Boolean)) {
            throw new IllegalArgumentException("Left expression does not exist: " + printer.visit(expression));
        }

        if (expression.getOperator() == TokenType.Logical_Or) {
            if (isTruthy(left)) {
                return left;
            }
            if (isTruthy(right)) {
                return right;
            }
            return right;
        } else if (expression.getOperator() == TokenType.Logical_And) {
            if (isTruthy(right)) {
                return left;
            }

            if (isTruthy(left)) {
                return right;
            }

            return right;
        }

        throw new IllegalArgumentException("Left expression does not exist: " + printer.visit(expression));
    }

    @Override
    public Object visit(AssignmentExpression expression) {
        switch (expression.getLeft()) {
            case MemberExpression memberExpression -> {
                var instanceEnv = executeBlock(memberExpression.getObject(), env);
                if (instanceEnv instanceof ResourceValue resourceValue) {
                    throw new RuntimeError("Resources can only be updated inside their block: " + resourceValue.getName());
                }
            }
            case SymbolIdentifier identifier -> {
                return assignmentSymbol(expression, identifier);
            }
            case null, default -> {
            }
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
        var propertyName = getSymbolIdentifier(expression);
        var value = executeBlock(expression.getObject(), env);
        switch (value) {
            case SchemaValue schemaValue -> {
                if (ExecutionContextIn(ForStatement.class)) {
                    if (ExecutionContext(ResourceExpression.class) instanceof ResourceExpression resourceExpression) {
                        return getProperty(schemaValue, "%s[%s]".formatted(propertyName, resourceExpression.getIndex()));
                    }
                } else {
                    return getProperty(schemaValue, propertyName);
                }
            }
            case ResourceValue resourceValue -> {
                // when retrieving the type of a resource, we first check the "instances" field for existing resources initialised there
                // Since that environment points to the parent(type env) it will also find the properties
                if (expression.getObject() instanceof MemberExpression memberExpression) {
                    return new Dependency(resourceValue, resourceValue.lookup(propertyName));
                }
                return resourceValue.lookup(propertyName);
            }
            case Map<?, ?> map -> {
                return map.get(propertyName);
            }
            case null, default -> {
            }
        }
        return value;
    }

    private @NotNull String getSymbolIdentifier(MemberExpression expression) {
        return switch (expression.getProperty()) {
            case SymbolIdentifier identifier -> identifier.string();
            case StringLiteral literal -> literal.getValue();
            case null, default ->
                    throw new OperationNotImplementedException("Membership expression not implemented for: " + printer.visit(expression));
        };
    }

    @Override
    public Object visit(ResourceExpression resource) {
        validate(resource);
//        if (callstack.peekLast() instanceof ForStatement) {
//            resource = ResourceExpression.resource(resource);
//        }
        contextStacks.push(ContextStack.Resource);
        // SchemaValue already installed globally when evaluating a SchemaDeclaration. This means the schema must be declared before the resource
        var installedSchema = (SchemaValue) executeBlock(resource.getType(), env);

        var typeEnvironment = installedSchema.getEnvironment();
        setResourceName(resource);
        try {
            if (resource.isEvaluating()) {
                // notifying an existing resource that it's dependencies were satisfied else create a new resource
                return detectCycle(resource, resource.getValue());
            } else {
                ResourceValue instance = initResource(resource, installedSchema, typeEnvironment);
                return detectCycle(resource, instance);
            }
        } finally {
            contextStacks.pop();
        }
    }

    private void validate(ResourceExpression resource) {
        if (resource.getName() == null) {
            throw new InvalidInitException("Resource does not have a name: " + printer.visit(resource));
        }
        if (contextStacks.contains(ContextStack.FUNCTION)) {
            throw new InvalidInitException("Resource cannot be declared inside a function: " + printer.visit(resource));
        }
    }

    private void setResourceName(ResourceExpression resource) {
        if (ExecutionContext(ForStatement.class) instanceof ForStatement forStatement) {
            Object visit = visit(forStatement.getItem());
            switch (visit) {
                case String s -> resource.setIndex("\"%s\"".formatted(s));
                case Number number -> resource.setIndex(number);
                case Map<?, ?> map -> {
                    if (forStatement.getIndex() != null) {
                        resource.setIndex(visit(forStatement.getIndex()));
                    } else {
                        resource.setIndex(visit(forStatement.getItem()));
                    }
                }
                default -> throw new TypeError("Invalid index type: %s".formatted(visit.getClass()));
            }
        }
    }

    private ResourceValue initResource(ResourceExpression resource, SchemaValue installedSchema, Environment<Object> typeEnvironment) {
        // clone all properties from schema properties to the new resource
        var resourceEnv = new Environment<>(env, typeEnvironment.getVariables());
        resourceEnv.remove(SchemaValue.INSTANCES); // instances should not be available to a resource only to it's schema
        var res = new ResourceValue(resource.name(), resourceEnv, installedSchema, resource.isExisting());
        try {
            // init any kind of new resource
            installedSchema.initInstance(resource.name(), res);
            resource.setValue(res);
        } catch (DeclarationExistsException e) {
            throw new DeclarationExistsException("Resource %s already exists: \n%s".formatted(resource.name(), printer.visit(resource)));
        }
        return res;
    }

    private ResourceValue detectCycle(ResourceExpression resource, ResourceValue instance) {
        resource.setEvaluated(true);
        resource.setEvaluating(false);
        for (Statement it : resource.getArguments()) {
            var result = executeBlock(it, instance.getProperties());
            if (result instanceof Deferred deferred) {
                instance.addDependency(deferred.resource());

                CycleDetection.detect(instance);

                resource.setEvaluated(false);

                deferredObservable.addObserver(resource, deferred);
            } else if (result instanceof Dependency dependency) {
                instance.addDependency(dependency.resource().getName());

                CycleDetection.detect(instance);
            }
        }
        if (resource.isEvaluated()) {
            // if not fully evaluated, doesn't make sense to notify observers(resources that depend on this resource)
            deferredObservable.notifyObservers(this, resource.name());
        }
        return instance;
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
    @Override
    public Object visit(ForStatement statement) {
        if (statement.hasRange()) {
            return ForWithRange(statement);
        } else if (statement.hasArray()) {
            var array = visit(statement.getArray());// get array items
            if (array instanceof List<?> list) {
                var forEnv = new Environment<>(env);
                Object result = null;
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);

                    forInit(forEnv, statement.getIndex(), i);
                    forInit(forEnv, statement.getItem(), item);
                    if (item instanceof Map<?, ?> map) {
                        map.forEach((key, value) -> forInit(forEnv, Identifier.symbol(statement.getItem().string() + "." + key), value));
                    }
                    result = executeBlock(statement.getBody(), forEnv);
                }
                return result;
            }
        }
        throw new OperationNotImplementedException("For statement not implemented");
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
        contextStacks.push(ContextStack.Schema);

        for (var property : expression.getProperties()) {
            switch (property.init()) {
                case BlockExpression blockExpression -> executeBlock(blockExpression.getExpression(), environment);
                case null, default -> environment.init(property.name(), visit(property.init()));
            }
        }
        contextStacks.pop();
        var name = expression.getName();
        return env.init(name.string(), SchemaValue.of(name, environment)); // install the type into the global env
    }

    @Override
    public Object visit(UnaryExpression expression) {
        Object operator = expression.getOperator();
        if (operator instanceof String op) {
            return switch (op) {
                case "++" -> {
                    Object res = executeBlock(expression.getValue(), env);
                    switch (res) {
                        case Integer integer -> {
                            yield 1 + integer;
                        }
                        case Double aDouble -> {
                            yield 1 + aDouble;
                        }
                        case null, default -> throw new RuntimeException("Invalid unary operator: " + res);
                    }
                }
                case "--" -> {
                    Object res = executeBlock(expression.getValue(), env);
                    switch (res) {
                        case Integer integer -> {
                            yield integer - 1;
                        }
                        case Double aDouble -> {
                            yield BigDecimal.valueOf(aDouble).subtract(BigDecimal.ONE).doubleValue();
                        }
                        case null, default -> throw new RuntimeException("Invalid unary operator: " + res);
                    }
                }
                case "-" -> {
                    Object res = executeBlock(expression.getValue(), env);
                    switch (res) {
                        case Integer r -> {
                            yield -r;
                        }
                        case Double r -> {
                            yield BigDecimal.valueOf(r).negate().doubleValue();
                        }
                        case null, default -> throw new RuntimeException("Invalid unary operator: " + res);
                    }
                }
                case "!" -> {
                    Object res = executeBlock(expression.getValue(), env);
                    if (res instanceof Boolean aBoolean) {
                        yield !aBoolean;
                    }
                    throw new RuntimeException("Invalid not operator: " + res);
                }
                default -> throw new RuntimeException("Operator could not be evaluated: " + expression.getOperator());
            };
        }
        throw new RuntimeException("Operator could not be evaluated");
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
        if (expression.getInit() instanceof ArrayExpression
            && !(expression.getType() instanceof ArrayTypeIdentifier)) {
            throw new IllegalArgumentException("Invalid type for array: %s".formatted(printer.visit(expression.getId())));
        }
        var type = visit(expression.getType());
        if (type instanceof Set<?> set) {
            if (value instanceof Collection<?> collection) {
                if (!set.containsAll(collection))
                    throw new IllegalArgumentException(format("Invalid value `{0}` for type `{1}`. Valid values `{2}`", value, expression.getType().string(), type));
            } else if (!set.contains(value)) {
                throw new IllegalArgumentException(format("Invalid value `{0}` for type `{1}`. Valid values `{2}`", value, expression.getType().string(), type));
            }
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
                var propertyName = getSymbolIdentifier(memberExpression);
                return resources.get(resource.getName()).get(propertyName);
            }
        }
        return visit(output.getInit());
    }

    @Override
    public Object visit(ValDeclaration expression) {
        String symbol = expression.getId().string();
        Object value = null;
        if (contextStacks.peek() == ContextStack.Resource) {
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
//        try {
        var decorator = decoratorInterpreter.get(expression.name());
        if (decorator != null) {
            decorator.execute(this, expression);
        } else {
            log.warn("Unknown decorator: {}", expression.name());
        }
//        } catch (Exception e) {
//            System.out.println("Decorator error! " + e.getMessage());
//            throw e;
//        }
        return expression.getValue();
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

            return lastEval;
        } catch (RuntimeException e) {
            errors.add(e);
            throw e;
        }
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
        contextStacks.push(ContextStack.FUNCTION);
        var name = declaration.getName();
        var params = declaration.getParams();
        var body = declaration.getBody();
        Object init = env.init(name.string(), FunValue.of(name, params, body, env));
        contextStacks.pop();
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
        Environment<Object> previous = this.env;
        try {
            this.env = environment;
            return Visitor.super.visit(expression);
        } finally {
            this.env = previous;
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

}
