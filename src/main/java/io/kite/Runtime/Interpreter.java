package io.kite.Runtime;

import io.kite.Frontend.Lexer.Token;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parse.Literals.ObjectLiteral.ObjectLiteralPair;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.ParserErrors;
import io.kite.Runtime.Environment.ActivationEnvironment;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Functions.Cast.BooleanCastFunction;
import io.kite.Runtime.Functions.Cast.DecimalCastFunction;
import io.kite.Runtime.Functions.Cast.IntCastFunction;
import io.kite.Runtime.Functions.Cast.StringCastFunction;
import io.kite.Runtime.Functions.DateFunction;
import io.kite.Runtime.Functions.Numeric.*;
import io.kite.Runtime.Functions.PrintFunction;
import io.kite.Runtime.Functions.PrintlnFunction;
import io.kite.Runtime.Values.*;
import io.kite.Runtime.exceptions.*;
import io.kite.SchemaContext;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.Type;
import io.kite.Visitors.SyntaxPrinter;
import io.kite.Visitors.Visitor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static io.kite.Frontend.Parser.Statements.FunctionDeclaration.fun;
import static io.kite.Utils.BoolUtils.isTruthy;

@Log4j2
public final class Interpreter implements Visitor<Object> {
    public static final String INDEX = "index";
    private static boolean hadRuntimeError;
    private final Deque<Callstack> callstack = new ArrayDeque<>();

    @Getter
    private final SyntaxPrinter printer = new SyntaxPrinter();
    private final DeferredObservable deferredObservable = new DeferredObservable();
    @Getter
    private Environment<Object> env;
    private SchemaContext context;


    public Interpreter() {
        this(new Environment());
    }

    public Interpreter(Environment<Object> environment) {
        this.env = environment;
        this.env.init("null", NullValue.of());
        this.env.init("true", true);
        this.env.init("false", false);
        this.env.init("print", new PrintFunction());
        this.env.init("println", new PrintlnFunction());

        // casting
        this.env.init("int", new IntCastFunction());
        this.env.init("decimal", new DecimalCastFunction());
        this.env.init("string", new StringCastFunction());
        this.env.init("boolean", new BooleanCastFunction());

        // number
        this.env.init("pow", new PowFunction());
        this.env.init("min", new MinFunction());
        this.env.init("max", new MaxFunction());
        this.env.init("ceil", new CeilFunction());
        this.env.init("floor", new FloorFunction());
        this.env.init("abs", new AbsFunction());
        this.env.init("date", new DateFunction());

//        this.globals.init("Vm", SchemaValue.of("Vm", new Environment(env, new Vm())));
    }

    static void runtimeError(RuntimeError error) {
        System.err.printf("%s\n[line %d]%n", error.getMessage(), error.getToken().line());
        hadRuntimeError = true;
    }

    private static @Nullable Object getProperty(SchemaValue schemaValue, String name) {
        if (schemaValue.getInstances().get(name) == null) {
            // if instance was not installed yet -> it will be installed later so we return a deferred object
            return new Deferred(schemaValue, name);
        } else {
            return schemaValue.getInstances().lookup(name);
        }
    }

    private boolean ExecutionContextIn(Class<ForStatement> forStatementClass) {
        var iterator = callstack.iterator();
        while (iterator.hasNext()) {
            Callstack next = iterator.next();
            if (next.getClass().equals(forStatementClass)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private Callstack ExecutionContext(Class<?> forStatementClass) {
        var iterator = callstack.iterator();
        while (iterator.hasNext()) {
            Callstack next = iterator.next();
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
        var key = expression.getKey().string();
        var value = visit(expression.getValue());
        return new ObjectLiteralPair(key, value);
    }

    @Override
    public Object visit(StringLiteral expression) {
        if (expression.isInterpolated()) {
            // when doing string interpolation on a property assignment we should look in the parent environment
            // for the interpolated string not in the "properties/environment of the resource"
            var hops = SchemaContext.RESOURCE == context ? 1 : 0;
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
        var env = new Environment(this.env);
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
        Object left = executeBlock(expression.getLeft(), env);
        Object right = executeBlock(expression.getRight(), env);

        var allowedTypes = allowTypes(expression.getOperator());
        expectOperatorType(expression.getLeft(), allowedTypes, expression);
        expectOperatorType(expression.getRight(), allowedTypes, expression);

        if (expression.getOperator() instanceof String op) {
            if (left instanceof Number ln && right instanceof Number rn) {
                // if both were ints, do int math → preserve integer result
                if (ln instanceof Integer a && rn instanceof Integer b) {
                    return switch (op) {
                        case "+" -> a + b;
                        case "-" -> a - b;
                        case "*" -> a * b;
                        case "/" -> a / b;
                        case "%" -> a % b;
                        case "==" -> a.equals(b);
                        case "!=" -> !a.equals(b);
                        case "<" -> a < b;
                        case "<=" -> a <= b;
                        case ">" -> a > b;
                        case ">=" -> a >= b;
                        default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
                    };
                }
                // otherwise treat both as doubles
                double a = ln.doubleValue(), b = rn.doubleValue();
                return switch (op) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    case "/" -> a / b;
                    case "%" -> a % b;
                    case "==" -> a == b;
                    case "!=" -> a != b;
                    case "<" -> a < b;
                    case "<=" -> a <= b;
                    case ">" -> a > b;
                    case ">=" -> a >= b;
                    default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
                };
            } else if (left instanceof String l && right instanceof String r) {
                return switch (op) {
                    case "+" -> l + r;
                    case "==" -> StringUtils.equals(l, r);
                    case "!=" -> !StringUtils.equals(l, r);
                    case "<" -> StringUtils.compare(l, r) < 0;
                    case "<=" -> StringUtils.compare(l, r) <= 0;
                    case ">" -> StringUtils.compare(l, r) > 0;
                    case ">=" -> StringUtils.compare(l, r) >= 0;
                    default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
                };
            } else if (left instanceof Boolean l && right instanceof Boolean r) {
                return switch (op) {
                    case "==" -> l.equals(r);
                    case "!=" -> !l.equals(r);
                    case "<" -> l.compareTo(r) < 0;
                    case "<=" -> l.compareTo(r) <= 0;
                    case ">" -> l.compareTo(r) > 0;
                    case ">=" -> l.compareTo(r) >= 0;
                    default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
                };
            } else if (left instanceof HashMap l && right instanceof HashMap r) {
                return switch (op) {
                    case "==" -> Objects.equals(l, r);
                    case "!=" -> !Objects.equals(l, r);
                    default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
                };

            }

            throw new IllegalArgumentException("%s cannot be compared with %s".formatted(left, right));
        }
        throw new RuntimeException("Invalid number: %s %s".formatted(left, right));
    }

    private void expectOperatorType(Object type, List<Class> allowedTypes, BinaryExpression expression) {
        if (!allowedTypes.contains(type.getClass())) {
            throw new TypeError("Unexpected type `" + type.getClass() + "` in expression: " + printer.visit(expression) + ". Allowed types: " + allowedTypes);
        }
    }

    private List<Class> allowTypes(String op) {
        return switch (op) {
            case "+" -> List.of(
                    NumberLiteral.class,
                    StringLiteral.class,
                    SymbolIdentifier.class,
                    CallExpression.class,
                    BinaryExpression.class,
                    MemberExpression.class
            );
            // allow addition for numbers and string
            case "-", "/", "*", "%" -> List.of(
                    NumberLiteral.class,
                    CallExpression.class,
                    SymbolIdentifier.class,
                    BinaryExpression.class);
            case "==", "!=" -> List.of(
                    StringLiteral.class,
                    CallExpression.class,
                    SymbolIdentifier.class,
                    NumberLiteral.class,
                    BinaryExpression.class,
                    BooleanLiteral.class,
                    ObjectLiteral.class);
            case "<=", "<", ">", ">=" -> List.of(BinaryExpression.class,
                    NumberLiteral.class,
                    CallExpression.class,
                    BooleanLiteral.class,
                    StringLiteral.class,
                    SymbolIdentifier.class);
            default -> throw new IllegalArgumentException("Operator could not be evaluated: " + op);
        };
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

        if (args.size() != declared.arity()) {
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
        Statement statement = declared.getBody();
        if (statement instanceof ExpressionStatement expressionStatement) {
            Expression expression = expressionStatement.getStatement();
            if (expression instanceof BlockExpression blockExpression) {
                return executeBlock(blockExpression.getExpression(), environment);
            } else { // lambdas without a block could simply be an expression: ((x) -> x*x)
                return executeBlock(expression, environment);
            }
        }
        throw new RuntimeException("Invalid function body");
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
                Object right = executeBlock(expression.getRight(), env);
                if (Objects.equals(expression.getOperator(), TokenType.Equal_Complex.getField())) {
                    var existing = env.lookup(identifier.string());
                    if (existing instanceof Integer left && right instanceof Integer numberLiteralRight) {
                        return env.assign(identifier.string(), left + numberLiteralRight);
                    } else if (existing instanceof Float left && right instanceof Float numberLiteralRight) {
                        return env.assign(identifier.string(), left + numberLiteralRight);
                    } else if (existing instanceof Double left && right instanceof Double numberLiteralRight) {
                        return env.assign(identifier.string(), left + numberLiteralRight);
                    } else if (existing instanceof String str && right instanceof Number number) {
                        return env.assign(identifier.string(), str + number);
                    } else if (existing instanceof List list) { // var x = []; x+=1; x==[1]
                        list.add(right);
                        return list;
                    }
                } else if (right instanceof Dependency dependency) {
                    var res = env.assign(identifier.string(), dependency.value());
                    return dependency;
                } else {
                    return env.assign(identifier.string(), right);
                }
            }
            case null, default -> {
            }
        }
        throw new RuntimeException("Invalid assignment");
    }

    @Override
    public Object visit(MemberExpression expression) {
        if (!(expression.getProperty() instanceof SymbolIdentifier resourceName)) {
            throw new OperationNotImplementedException("Membership expression not implemented for: " + expression.getObject());
        }
        var value = executeBlock(expression.getObject(), env);
        switch (value) {
            case SchemaValue schemaValue -> {
                if (ExecutionContextIn(ForStatement.class)) {
                    if (ExecutionContext(ResourceExpression.class) instanceof ResourceExpression resourceExpression) {
                        return getProperty(schemaValue, "%s[%s]".formatted(resourceName.string(), resourceExpression.getIndex()));
                    }
                } else {
                    String name = resourceName.string();
                    return getProperty(schemaValue, name);
                }
            }
            case ResourceValue resourceValue -> {
                // when retrieving the type of a resource, we first check the "instances" field for existing resources initialised there
                // Since that environment points to the parent(type env) it will also find the properties
                if (expression.getObject() instanceof MemberExpression memberExpression) {
                    return new Dependency(resourceValue, resourceValue.lookup(resourceName.string()));
                }
                return resourceValue.lookup(resourceName.string());
            }
            case null, default -> {
            }
        }
        return value;
    }

    @Override
    public Object visit(ResourceExpression resource) {
        if (resource.getName() == null) {
            throw new InvalidInitException("Resource does not have a name: " + printer.visit(resource));
        }
        context = SchemaContext.RESOURCE;
        // SchemaValue already installed globally when evaluating a SchemaDeclaration. This means the schema must be declared before the resource
        var installedSchema = (SchemaValue) executeBlock(resource.getType(), env);

        Environment typeEnvironment = installedSchema.getEnvironment();
        setResourceName(resource);
        try {
            if (resource.isEvaluating()) {
                // notifying an existing resource that it's dependencies were satisfied else create a new resource
                return detectCycle(resource, resource.getValue());
            } else {
                return detectCycle(resource, initResource(resource, installedSchema, typeEnvironment));
            }
        } finally {
            context = null;
        }
    }

    private void setResourceName(ResourceExpression resource) {
        if (ExecutionContext(ForStatement.class) instanceof ForStatement forStatement) {
            Object visit = visit(forStatement.getItem());
            if (visit instanceof String s) {
                resource.setIndex("\"%s\"".formatted(s));
            } else if (visit instanceof Number number) {
                resource.setIndex(number);
            } else {
                throw new TypeError("Invalid index type: %s".formatted(visit.getClass()));
            }
        }
    }

    private ResourceValue initResource(ResourceExpression resource, SchemaValue installedSchema, Environment typeEnvironment) {
        // clone all properties from schema properties to the new resource
        var resourceEnv = new Environment(env, typeEnvironment.getVariables());
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

                    forEnv.initOrAssign(statement.getItem().string(), item);
                    result = executeBlock(statement.getBody(), forEnv);
                }
                return result;
            }
        }
        //        List<Statement> statements = statement.discardBlock();
//        statements.add(ExpressionStatement.expressionStatement(statement.getUpdate()));
//        var whileStatement = WhileStatement.of(statement.getTest(), BlockExpression.block(statements));
//        if (statement.getItem() == null) {
//            return executeBlock(whileStatement, env);
//        }
//        return executeBlock(BlockExpression.block(statement.getItem(), whileStatement), env);
        throw new OperationNotImplementedException("For statement not implemented");
    }

    private Object ForWithRange(ForStatement statement) {
        if (statement.isBodyBlock()) {
            var range = statement.getRange();
            int minimum = range.getMinimum();
            int maximum = range.getMaximum();

            // env to hold init variable
            var forEnv = new Environment<>(env, Map.of(statement.getItem().string(), minimum));

            Object result = null;
            var body = statement.discardBlock();
            for (int i = minimum; i < maximum; i++) {
                forEnv.initOrAssign(statement.getItem().string(), i); // during range, i, index and value are all the same
                // env to be created on each iteration since the same variables can be declared multiple times
                // during a loop so we need a new env each time @testForReturnsResourceNestedVar
                var environment = new Environment<>(forEnv);
                result = executeBlock(body, environment);
            }
            return result;
        } else if (statement.getBody() != null) {
            var range = statement.getRange();
            int minimum = range.getMinimum();
            int maximum = range.getMaximum();

            String index = statement.getItem().string();
            var forEnv = new Environment<>(env, Map.of(index, minimum));

            List<Object> result = new ArrayList<>(maximum);
            for (int i = minimum; i < maximum; i++) {
                forEnv.initOrAssign(statement.getItem().string(), i);

                if (statement.getBody() instanceof IfStatement ifStatement) {
                    var test = (Boolean) executeBlock(ifStatement.getTest(), forEnv);
                    if (test) {
                        result.add(executeBlock(ifStatement.getConsequent(), forEnv));
                    } else {
                        var alternate = ifStatement.getAlternate();
                        if (alternate != null) {
                            result.add(executeBlock(alternate, forEnv));
                        }
                    }
                } else {
                    Object e = executeBlock(statement.getBody(), forEnv);
                    result.add(e);
                }
            }
            return result;
        }
        throw new OperationNotImplementedException("For statement operation not implemented");
    }

    @Override
    public Object visit(SchemaDeclaration expression) {
        var environment = new Environment<>(env);
        context = SchemaContext.SCHEMA;

        for (var property : expression.getProperties()) {
            var declaration = property.declaration();
            if (declaration.hasInit() && declaration.getInit() instanceof BlockExpression blockExpression) {
                executeBlock(blockExpression.getExpression(), environment);
            } else {
                environment.init(declaration.getId().string(), visit(declaration.getInit()));
            }
//            if (property.defaultValue() instanceof BlockExpression blockExpression) {
//                executeBlock(blockExpression.getExpression(), environment); // install properties/methods of a type into the environment
//            } else {
//                environment.init(property.name().string(), visit(property.defaultValue()));
//            }
        }
        context = null;
        var name = expression.getName();
        return env.init(name.string(), SchemaValue.of(name, environment)); // install the type into the global env
//        switch (expression.getProperties()) {
//            case ExpressionStatement statement when statement.getStatement() instanceof BlockExpression blockExpression -> {
//                var environment = new Environment<>(env);
//                context = SchemaContext.SCHEMA;
//                executeBlock(blockExpression.getExpression(), environment); // install properties/methods of a type into the environment
//                context = null;
//                var name = expression.getName();
//                return env.init(name.string(), SchemaValue.of(name, environment)); // install the type into the global env
//            }
//            case null, default -> {
//            }
//        }
//        throw new RuntimeException("Invalid declaration: " + printer.visit(expression));
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
        if (value instanceof Dependency dependency) { // a dependency access on another resource
            return env.init(symbol, dependency.value());
        }
        return env.init(symbol, value);
    }

    @Override
    public Object visit(ValDeclaration expression) {
        String symbol = expression.getId().string();
        Object value = null;
        if (context == SchemaContext.RESOURCE) {
            if (!expression.hasInit()) {
                throw new InvalidInitException("Val declaration must be initialised: " + expression.getId().string() + " is null");
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
        throw new RuntimeException("Annotations are not yet supported");
    }

    @Override
    public Object visit(Program program) {
        Object lastEval = new NullValue();

        if (ParserErrors.hadErrors()) {
            return null;
        }
        for (Statement i : program.getBody()) {
            lastEval = executeBlock(i, env);
        }

        return lastEval;
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
        var name = declaration.getName();
        var params = declaration.getParams();
        var body = declaration.getBody();
        return env.init(name.string(), FunValue.of(name, params, body, env));
    }

    @Override
    public Object visit(ExpressionStatement statement) {
        return executeBlock(statement.getStatement(), env);
    }

    Object interpret(List<Statement> statements) {
        try {
            Object res = null;
            for (Statement statement : statements) {
                res = visit(statement);
            }
            return res;
        } catch (RuntimeError error) {
            runtimeError(error);
            return null;
        }
    }

    Object executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.env;
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

    Object executeBlock(Expression expression, Environment environment) {
        Environment previous = this.env;
        try {
            this.env = environment;
            return Visitor.super.visit(expression);
        } finally {
            this.env = previous;
        }
    }

    Object executeBlock(Statement statement, Environment environment) {
        Environment previous = this.env;
        try {
            this.env = environment;
            return visit(statement);
        } finally {
            this.env = previous;
        }
    }

}
