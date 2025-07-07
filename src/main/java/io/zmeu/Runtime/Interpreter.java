package io.zmeu.Runtime;

import io.zmeu.ErrorSystem;
import io.zmeu.Frontend.Lexer.Token;
import io.zmeu.Frontend.Lexer.TokenType;
import io.zmeu.Frontend.Parser.Expressions.*;
import io.zmeu.Frontend.Parser.Literals.*;
import io.zmeu.Frontend.Parser.Literals.ObjectLiteral.ObjectLiteralPair;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.*;
import io.zmeu.Runtime.Environment.ActivationEnvironment;
import io.zmeu.Runtime.Environment.Environment;
import io.zmeu.Runtime.Functions.Cast.BooleanCastFunction;
import io.zmeu.Runtime.Functions.Cast.DecimalCastFunction;
import io.zmeu.Runtime.Functions.Cast.IntCastFunction;
import io.zmeu.Runtime.Functions.Cast.StringCastFunction;
import io.zmeu.Runtime.Functions.DateFunction;
import io.zmeu.Runtime.Functions.Numeric.*;
import io.zmeu.Runtime.Functions.PrintFunction;
import io.zmeu.Runtime.Functions.PrintlnFunction;
import io.zmeu.Runtime.Values.*;
import io.zmeu.Runtime.exceptions.*;
import io.zmeu.SchemaContext;
import io.zmeu.TypeChecker.Types.Type;
import io.zmeu.Visitors.LanguageAstPrinter;
import io.zmeu.Visitors.Visitor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.zmeu.Frontend.Parser.Statements.FunctionDeclaration.fun;
import static io.zmeu.Utils.BoolUtils.isTruthy;

@Log4j2
public final class Interpreter implements Visitor<Object> {
    private static boolean hadRuntimeError;
    @Getter
    private final LanguageAstPrinter printer = new LanguageAstPrinter();
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
        return lookupVar(expression);
    }

    private Object lookupVar(Identifier expression) {
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
        Object lhs = executeBlock(expression.getLeft(), env);
        Object rhs = executeBlock(expression.getRight(), env);
        if (expression.getOperator() instanceof String op
            && lhs instanceof Number ln
            && rhs instanceof Number rn) {
            // if both were ints, do int math → preserve integer result
            if (ln instanceof Integer a && rn instanceof Integer b) {
                return switch (op) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    case "/" -> a / b;
                    case "%" -> a % b;
                    case "==" -> a.equals(b);
                    case "<" -> a < b;
                    case "<=" -> a <= b;
                    case ">" -> a > b;
                    case ">=" -> a >= b;
                    default -> throw new RuntimeException("Operator could not be evaluated: " + op);
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
                case "<" -> a < b;
                case "<=" -> a <= b;
                case ">" -> a > b;
                case ">=" -> a >= b;
                default -> throw new RuntimeException("Operator could not be evaluated: " + op);
            };
        }
        throw new RuntimeException("Invalid number: %s %s".formatted(lhs, rhs));
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
                //            Integer distance = locals.get(identifier);
                if (right instanceof Dependency dependency) {
                    env.assign(identifier.string(), dependency.value());
                    return dependency;
                }
                return env.assign(identifier.string(), right);
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
        // when retrieving the type of a resource, we first check the "instances" field for existing resources initialised there
        // Since that environment points to the parent(type env) it will also find the properties
        if (value instanceof SchemaValue schemaValue) { // vm.main -> if user references the schema we search for the instances of those schemas
            String name = resourceName.string();
            if (schemaValue.getInstances().get(name) == null) {
                // if instance was not installed yet -> it will be installed later so we return a deferred object
                return new Deferred(schemaValue, name);
            } else {
                return schemaValue.getInstances().lookup(name);
            }
        } else if (value instanceof ResourceValue resourceValue) {
            if (expression.getObject() instanceof MemberExpression) {
                return new Dependency(resourceValue, resourceValue.lookup(resourceName.string()));
            }
            return resourceValue.lookup(resourceName.string());
        } // else it could be a resource or any other type like a NumericLiteral or something else
        return value;
    }

    @Override
    public Object visit(ResourceExpression resource) {
        if (resource.getName() == null) {
            throw new InvalidInitException("Resource does not have a name: " + resource.name());
        }
        context = SchemaContext.RESOURCE;
        // SchemaValue already installed globally when evaluating a SchemaDeclaration. This means the schema must be declared before the resource
        var installedSchema = (SchemaValue) executeBlock(resource.getType(), env);

        Environment typeEnvironment = installedSchema.getEnvironment();

        var instance = installedSchema.getInstance(resource.name());
        if (instance == null) {
            // clone all properties from schema properties to the new resource
            var resourceEnv = new Environment(typeEnvironment, typeEnvironment.getVariables());
            resourceEnv.remove(SchemaValue.INSTANCES); // instances should not be available to a resource only to it's schema
            instance = new ResourceValue(resource.name(), resourceEnv, installedSchema, resource.isExisting());
            // init any kind of new resource
            installedSchema.initInstance(resource.name(), instance);
        }
        try {
//            var init = installedSchema.getMethodOrNull("init");
//            if (init != null) {
//                var args = new ArrayList<>();
//                for (Statement it : resource.getArguments()) {
//                    Object objectRuntimeValue = executeBlock(it, resourceEnv);
//                    args.add(objectRuntimeValue);
//                }
//                functionCall(FunValue.of(init.name(), init.getParams(), init.getBody(), resourceEnv/* this env */), args);
//            } else {

            resource.setEvaluated(true);
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
                deferredObservable.notifyObservers(this, resource.name());
                return instance;
            } else {
                // if not fully evaluated, doesn't make sense to notify observers(resources that depend on this resource)
                // because they will not be able to be reevaluated
                return instance;
            }
        } catch (NotFoundException e) {
//            throw new NotFoundException("Field '%s' not found on resource '%s'".formatted(e.getObjectNotFound(), expression.name()),e);
            throw e;
        } finally {
            context = null;
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

    @Override
    public Object visit(ForStatement statement) {
        List<Statement> statements = statement.discardBlock();
        statements.add(ExpressionStatement.expressionStatement(statement.getUpdate()));
        var whileStatement = WhileStatement.of(statement.getTest(), BlockExpression.block(statements));
        if (statement.getInit() == null) {
            return executeBlock(whileStatement, env);
        }
        return executeBlock(BlockExpression.block(statement.getInit(), whileStatement), env);
    }

    @Override
    public Object visit(SchemaDeclaration expression) {
        switch (expression.getBody()) {
            case ExpressionStatement statement when statement.getStatement() instanceof BlockExpression blockExpression -> {
                var typeEnv = new Environment<>(env);
                context = SchemaContext.SCHEMA;
                executeBlock(blockExpression.getExpression(), typeEnv); // install properties/methods of a type into the environment
                context = null;
                var name = expression.getName();
                return env.init(name.string(), SchemaValue.of(name, typeEnv)); // install the type into the global env
            }
            case null, default -> {
            }
        }
        throw new RuntimeException("Invalid declaration:" + expression.getName());
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
            var object = (ObjectLiteralPair)visit(property);
            map.put(object.key(), object.value());
        }
        return map;
    }

    @Override
    public Object visit(Program program) {
        Object lastEval = new NullValue();

        if (ErrorSystem.hadErrors()) {
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
                res = execute(statement);
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
                res = execute(statement);
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
            return execute(statement);
        } finally {
            this.env = previous;
        }
    }

    private Object execute(Statement stmt) {
        return Visitor.super.visit(stmt);
    }

    private Object execute(Expression stmt) {
        return visit(stmt);
    }

}
