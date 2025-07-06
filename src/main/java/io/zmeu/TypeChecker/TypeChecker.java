package io.zmeu.TypeChecker;

import io.zmeu.Frontend.Parser.Expressions.*;
import io.zmeu.Frontend.Parser.Literals.*;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.*;
import io.zmeu.Runtime.exceptions.InvalidInitException;
import io.zmeu.Runtime.exceptions.NotFoundException;
import io.zmeu.Runtime.exceptions.OperationNotImplementedException;
import io.zmeu.TypeChecker.Types.*;
import io.zmeu.Visitors.LanguageAstPrinter;
import io.zmeu.Visitors.Visitor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Log4j2
public final class TypeChecker implements Visitor<Type> {
    private final LanguageAstPrinter printer = new LanguageAstPrinter();
    @Getter
    private TypeEnvironment env;

    public TypeChecker() {
        env = new TypeEnvironment();
        env.init(ValueType.String.getValue(), ValueType.String);
        env.init(ValueType.Number.getValue(), ValueType.Number);
        env.init(ValueType.Boolean.getValue(), ValueType.Boolean);
        env.init(ValueType.Void.getValue(), ValueType.Void);
        env.init(ValueType.Null.getValue(), ValueType.Null);
        env.init("pow", TypeFactory.fromString("(%s,%s)->%s".formatted(ValueType.Number.getValue(), ValueType.Number.getValue(), ValueType.Number.getValue())));
        env.init("toString", TypeFactory.fromString("(%s)->%s".formatted(ValueType.Number.getValue(), ValueType.String.getValue())));
    }

    public TypeChecker(TypeEnvironment environment) {
        this.env = environment;
    }

    @Override
    public Type visit(Program program) {
        Type type = ValueType.Null;
        for (Statement statement : program.getBody()) {
            type = executeBlock(statement, env);
        }
        return type;
    }

    @Override
    public Type visit(Expression expression) {
        try {
            return Visitor.super.visit(expression);
        } catch (NotFoundException | TypeError exception) {
            log.error(exception.getMessage());
            throw exception;
        }
    }


    @Override
    public Type visit(Identifier expression) {
        try {
            if (expression instanceof TypeIdentifier identifier) {
                Type type = env.lookup(identifier.getType().getValue());
                return Objects.requireNonNullElseGet(type, () -> TypeFactory.fromString(identifier.getType().getValue()));
            } else if (expression instanceof SymbolIdentifier identifier) {
                Type type = env.lookup(identifier.getSymbol());
                return Objects.requireNonNullElseGet(type, () -> TypeFactory.fromString(identifier.getSymbol()));
            }
            throw new TypeError(expression.string());
        } catch (NotFoundException exception) {
            log.error(exception.getMessage());
            throw exception;
        }
    }

    @Override
    public Type visit(NullLiteral expression) {
        return ValueType.Null;
    }

    @Override
    public Type visit(ObjectLiteral expression) {
        throw new OperationNotImplementedException("Object literals not implemented yet");
    }

    @Override
    public Type visit(StringLiteral expression) {
        return ValueType.String;
    }

    @Override
    public Type visit(BlockExpression expression) {
        var env = new TypeEnvironment(this.env);
        return executeBlock(expression.getExpression(), env);
    }

    @NotNull
    private Type executeBlock(List<Statement> statements, TypeEnvironment environment) {
        TypeEnvironment previous = this.env;
        try {
            this.env = environment;
            Type res = ValueType.Null;
            for (Statement statement : statements) {
                res = visit(statement);
            }
            return res;
        } finally {
            this.env = previous;
        }
    }

    private Type executeBlock(Expression statement, TypeEnvironment environment) {
        TypeEnvironment previous = this.env;
        try {
            this.env = environment;
            return visit(statement);
        } finally {
            this.env = previous;
        }
    }

    private Type executeBlock(Statement statement, TypeEnvironment environment) {
        TypeEnvironment previous = this.env;
        try {
            this.env = environment;
            return visit(statement);
        } finally {
            this.env = previous;
        }
    }

    @Override
    public Type visit(GroupExpression expression) {
        return null;
    }

    @Override
    public Type visit(BinaryExpression expression) {
        var op = expression.getOperator();
        Expression right = expression.getRight();
        Expression left = expression.getLeft();
        if (left == null || right == null) {
            throw new TypeError("Operator " + op + " expects 2 arguments");
        }
        var t1 = visit(left);
        var t2 = visit(right);

        // allow operations only on the same types of values
        // 1+1, "hello "+"world", 1/2, 1<2, "hi" == "hi"
        List<Type> allowedTypes = allowTypes(op);
        this.expectOperatorType(t1, allowedTypes, expression);
        this.expectOperatorType(t2, allowedTypes, expression);

        if (isBooleanOp(op)) { // when is a boolean operation in an if statement, we return a boolean type(the result) else we return the type of the result for a + or *
            expect(t1, t2, left);
            return ValueType.Boolean;
        }
        return expect(t1, t2, left);
    }

    private void expectOperatorType(Type type, List<Type> allowedTypes, BinaryExpression expression) {
        if (!allowedTypes.contains(type)) {
            throw new TypeError("Unexpected type: " + type + " in expression " + printer.visit(expression) + ". Allowed types: " + allowedTypes);
        }
    }

    private List<Type> allowTypes(String op) {
        return switch (op) {
            case "+" -> List.of(ValueType.Number, ValueType.String); // allow addition for numbers and string
            case "-", "/", "*", "%" -> List.of(ValueType.Number);
            case "==", "!=" -> List.of(ValueType.String, ValueType.Number, ValueType.Boolean);
            case "<=", "<", ">", ">=" -> List.of(ValueType.Number, ValueType.Boolean);
            default -> throw new TypeError("Unknown operator " + op);
        };
    }

    private boolean isBooleanOp(String op) {
        return switch (op) {
            case "==", "<=", ">=", "!=", "<", ">" -> true;
            case null, default -> false;
        };
    }

    private Type expect(Type actualType, Type expectedType, Expression expectedVal) {
//        if (actualType == ValueType.Null) {
//            return expectedType;
//        }
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (!Objects.equals(actualType, expectedType)) {
            // only evaluate printing if we need to
            String string = "Expected type " + expectedType + " but got " + actualType + " in expression: " + printer.visit(expectedVal);
            throw new TypeError(string);
        }
        return actualType;
    }

    private Type expect(Type actualType, Type expectedType, Statement actualVal, Statement expectedVal) {
        if (actualType == null || actualType == ValueType.Null) {
            return expectedType;
        }
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (!Objects.equals(actualType, expectedType)) {
            // only evaluate printing if we need to
            String string = "Expected type " + expectedType + " for value " + printer.visit(expectedVal) + " but got " + actualType + " in expression: " + printer.visit(actualVal);
            throw new TypeError(string);
        }
        return actualType;
    }

    private Type expect(Type actualType, Type expectedType, Statement actualVal, Expression expectedVal) {
        if (actualType == ValueType.Null) {
            return expectedType;
        }
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (!Objects.equals(actualType, expectedType)) {
            // only evaluate printing if we need to
            String string = "Expected type " + expectedType + " for value " + printer.visit(expectedVal) + " but got " + actualType + " in expression: " + printer.visit(actualVal);
            throw new TypeError(string);
        }
        return actualType;
    }

    @Override
    public Type visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public Type visit(LogicalExpression expression) {
        Type left = visit(expression.getLeft());
        Type right = visit(expression.getRight());
        expect(left, ValueType.Boolean, expression);
        expect(right, ValueType.Boolean, expression);
        return expect(left, right, expression);
    }

    @Override
    public Type visit(MemberExpression expression) {
        if (expression.getProperty() instanceof SymbolIdentifier resourceName) {
            var value = executeBlock(expression.getObject(), env);
            // when retrieving the type of a resource, we first check the "instances" field for existing resources initialised there
            // Since that environment points to the parent(type env) it will also find the properties
            if (value instanceof SchemaType schemaValue) { // vm.main -> if user references the schema we search for the instances of those schemas
                return schemaValue.getInstances().lookup(resourceName.string());
            } else if (value instanceof ResourceType iEnvironment) {
                return iEnvironment.lookup(resourceName.string());
            } // else it could be a resource or any other type like a NumericLiteral or something else
        }
        throw new OperationNotImplementedException("Membership expression not implemented for: " + expression.getObject());
    }

    @Override
    public Type visit(ThisExpression expression) {
        return null;
    }

    @Override
    public Type visit(UnaryExpression expression) {
        var operator = expression.getOperator();
        return switch (operator) {
            case "++", "--", "-" -> executeBlock(expression.getValue(), env);
            case "!" -> {
                var res = executeBlock(expression.getValue(), env);
                if (res == ValueType.Boolean) {
                    yield res;
                }
                throw new RuntimeException("Invalid not operator: " + res);
            }
            default -> throw new RuntimeException("Operator could not be evaluated: " + expression.getOperator());
        };
    }

    @Override
    public Type visit(Type type) {
        return type;
    }

    @Override
    public Type visit(InitStatement statement) {
        return null;
    }

    @Override
    public Type visit(FunctionDeclaration expression) {
        var params = convertParams(expression.getParams());
        var funType = new FunType(params.values(), expression.getReturnType().getType());
        env.init(expression.getName(), funType); // save function signature in env so that we're able to call it later to validate types

        var actualReturn = validateBody(expression.getReturnType().getType(), expression.getBody(), params);
        funType.setReturnType(actualReturn);
        return funType;
    }

    @Override
    public Type visit(LambdaExpression expression) {
        var params = convertParams(expression.getParams());
        // if return type missing from parsing because Void was not specified then convert it to the actual return type from the body
        TypeIdentifier returnType = Optional.ofNullable(expression.getReturnType())
                .orElse(TypeIdentifier.type(ValueType.Null));

        var funType = new FunType(params.values(), visit(returnType));
        var actualReturn = validateBody(returnType.getType(), expression.getBody(), params);
        funType.setReturnType(actualReturn);
        return funType;
    }

    private @NotNull Map<String, Type> convertParams(List<ParameterIdentifier> params) {
        var collect = new HashMap<String, Type>(params.size());
        for (ParameterIdentifier identifier : params) {
            if (identifier.getType() == null) {
                throw new IllegalArgumentException("Missing type for parameter " + identifier.getName().string() + "(" + printer.visit(identifier) + ")");
            }
            Type type = identifier.getType().getType();
            if (collect.put(identifier.getName().getSymbol(), type) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return collect;
    }

    private @NotNull Type validateBody(@Nullable Type returnType, Statement body, Map<String, Type> collect) {
        var funEnv = new TypeEnvironment(env, collect);
        var actualReturnType = executeBlock(body, funEnv);

        return expect(actualReturnType, returnType, body, returnType);
    }

    @Override
    public Type visit(CallExpression<Expression> expression) {
        FunType fun = (FunType) visit(expression.getCallee()); // extract the function type itself

        var passedArgumentsTypes = expression.getArguments()
                .stream()
                .map(this::visit)
                .toList();
        if (fun.getParams().size() != passedArgumentsTypes.size()) {
            String string = "Function '" + printer.visit(expression.getCallee()) + "' expects " + fun.getParams().size() + " arguments but got " + passedArgumentsTypes.size() + " in " + printer.visit(expression);
            throw new TypeError(string);
        }
        checkArgs(fun.getParams(), passedArgumentsTypes, expression);
        return fun.getReturnType();
    }

    private void checkArgs(List<Type> params, List<Type> args, CallExpression<Expression> expression) {
        for (int i = 0; i < args.size(); i++) {
            try {
                var param = params.get(i);
                Type actual = args.get(i);
                expect(actual, param, expression);
            } catch (IndexOutOfBoundsException exception) {
            }
        }
    }

    @Override
    public Type visit(ReturnStatement statement) {
        return visit(statement.getArgument());
    }

    @Override
    public Type visit(ExpressionStatement statement) {
        return executeBlock(statement.getStatement(), env);
    }

    @Override
    public Type visit(VarStatement statement) {
        Type type = ValueType.Null;
        for (VarDeclaration declaration : statement.getDeclarations()) {
            type = executeBlock(declaration, this.env);
        }
        return type;
    }

    @Override
    public Type visit(ValStatement statement) {
        Type type = ValueType.Null;
        for (var declaration : statement.getDeclarations()) {
            type = executeBlock(declaration, this.env);
        }
        return type;
    }

    @Override
    public Type visit(IfStatement statement) {
        Type t1 = visit(statement.getTest());
        expect(t1, ValueType.Boolean, statement.getTest());
        Type t2 = visit(statement.getConsequent());
        Type t3 = null;
        if (statement.getAlternate() != null) {
            t3 = visit(statement.getAlternate());
        }

        return expect(t3, t2, statement, statement);
    }

    @Override
    public Type visit(WhileStatement statement) {
        var condition = visit(statement.getTest());
        expect(condition, ValueType.Boolean, statement, statement.getTest()); // condition should always be boolean
        return visit(statement.getBody());
    }

    @Override
    public Type visit(ForStatement statement) {
        List<Statement> statements = statement.discardBlock();
        statements.add(ExpressionStatement.expressionStatement(statement.getUpdate()));
        var whileStatement = WhileStatement.of(statement.getTest(), BlockExpression.block(statements));
        if (statement.getInit() == null) {
            return executeBlock(whileStatement, env);
        }
        return executeBlock(BlockExpression.block(statement.getInit(), whileStatement), env);
    }

    @Override
    public Type visit(SchemaDeclaration schema) {
        var name = schema.getName();
        var body = schema.getBody();

        var schemaType = new SchemaType(name.string(), env);
        env.init(name, schemaType);

        if (schema.getBody() instanceof ExpressionStatement statement && statement.getStatement() instanceof BlockExpression blockExpression) {
            executeBlock(blockExpression.getExpression(), schemaType.getEnvironment());
            return schemaType;
        }
        throw new RuntimeException("Invalid schema declaration: " + schema.getName());
    }

    @Override
    public Type visit(ResourceExpression resource) {
        if (resource.getName() == null) {
            throw new InvalidInitException("Resource does not have a name: " + resource.name());
        }
        // SchemaValue already installed globally when evaluating a SchemaDeclaration.
        // This means the schema must be declared before the resource
        var installedSchema = (SchemaType) env.lookup(resource.getType().string());
        if (installedSchema == null) {
            throw new InvalidInitException("Schema not found during " + resource.name() + " initialization");
        }

        var schemaEnv = installedSchema.getEnvironment();
        // clone/inherit all default properties from schema properties to the new resource
        var resourceEnv = new TypeEnvironment(schemaEnv, schemaEnv.getVariables());
        // init resource environment with values defined by the user
        executeBlock(resource.getArguments(), resourceEnv);
        // validate each property in the resource that matches the type defined in the schema
        for (var argument : resourceEnv.getVariables().entrySet()) {
            if (installedSchema.getProperty(argument.getKey()) != argument.getValue()) {
                throw new InvalidInitException("Property type mismatch for: " + argument.getKey() + " in " + printer.visit(resource));
            }
        }


        var resourceType = new ResourceType(resource.name(), installedSchema, resourceEnv);
        installedSchema.addInstance(resource.name(), resourceType);

        return resourceType;
//        try {
//            Type init = installedSchema.getProperty("init");
//            if (init != null) {
//                var args = new ArrayList<>();
//                for (Statement it : resource.getArguments()) {
//                    var objectRuntimeValue = executeBlock(it, resourceEnv);
//                    args.add(objectRuntimeValue);
//                }
//                FunType initType = (FunType) init;
//            } else {
//            }
//            var res = installedSchema.initInstance(resource.name(), ResourceValue.of(resource.name(), resourceEnv, installedSchema));
//            engine.process(installedSchema.typeString(), resourceEnv.getVariables());
//        } catch (NotFoundException e) {
//            throw new NotFoundException("Field '%s' not found on resource '%s'".formatted(e.getObjectNotFound(), expression.name()),e);
//            throw e;
//        }
    }

    @Override
    public Type visit(VarDeclaration expression) {
        String var = expression.getId().string();
        if (expression.getInit() != null) {
            var implicitType = visit(expression.getInit());
            if (expression.hasType()) {
                var explicitType = visit(expression.getType());
                expect(implicitType, explicitType, expression);
                return env.init(var, explicitType);
            }
            return env.init(var, implicitType);
        } else if (expression.hasType()) {
            var explicitType = visit(expression.getType());
            return env.init(var, explicitType);
        } else {
            throw new IllegalArgumentException("Missing explicit and implicit type for variable " + var);
        }
    }

    @Override
    public Type visit(ValDeclaration expression) {
        String var = expression.getId().string();
        if (expression.getInit() != null) {
            var implicitType = visit(expression.getInit());
            if (expression.hasType()) {
                var explicitType = visit(expression.getType());
                expect(implicitType, explicitType, expression);
                return env.init(var, explicitType);
            }
            return env.init(var, implicitType);
        } else if (expression.hasType()) {
            var explicitType = visit(expression.getType());
            return env.init(var, explicitType);
        } else {
            throw new IllegalArgumentException("Missing explicit and implicit type for variable " + var);
        }
    }

    @Override
    public Type visit(ObjectExpression expression) {
        throw new OperationNotImplementedException("Object expression not implemented");
    }

    /**
     * Validates value assigned to x is of the same type as init type
     * var x = 1
     * x=2 // should allow number but not string
     */
    @Override
    public Type visit(AssignmentExpression expression) {
        var varType = visit(expression.getLeft());
        var valueType = visit(expression.getRight());
        var expected = expect(valueType, varType, expression.getLeft());
        if (expression.getLeft() instanceof SymbolIdentifier symbolIdentifier) {
            env.assign(symbolIdentifier.string(), expected);
        }
        return expected;
    }

    @Override
    public Type visit(NumberLiteral expression) {
        return ValueType.Number;
    }

    @Override
    public Type visit(BooleanLiteral expression) {
        return ValueType.Boolean;
    }

    @Override
    public Type visit(float expression) {
        return ValueType.Number;
    }

    @Override
    public Type visit(double expression) {
        return ValueType.Number;
    }

    @Override
    public Type visit(int expression) {
        return ValueType.Number;
    }

    @Override
    public Type visit(boolean expression) {
        return ValueType.Boolean;
    }

    @Override
    public Type visit(String expression) {
        return ValueType.String;
    }
}
