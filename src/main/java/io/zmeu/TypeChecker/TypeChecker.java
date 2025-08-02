package io.zmeu.TypeChecker;

import io.zmeu.Frontend.Parse.Literals.*;
import io.zmeu.Frontend.Parser.Expressions.*;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.*;
import io.zmeu.Runtime.exceptions.InvalidInitException;
import io.zmeu.Runtime.exceptions.NotFoundException;
import io.zmeu.Runtime.exceptions.OperationNotImplementedException;
import io.zmeu.TypeChecker.Types.*;
import io.zmeu.Visitors.SyntaxPrinter;
import io.zmeu.Visitors.Visitor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Log4j2
public final class TypeChecker implements Visitor<Type> {
    private final SyntaxPrinter printer = new SyntaxPrinter();
    private final Set<String> vals = new HashSet<>();
    @Getter
    private TypeEnvironment env;

    public TypeChecker() {
        env = new TypeEnvironment();
    }

    public TypeChecker(TypeEnvironment environment) {
        this.env = environment;
    }

    /**
     * Explicit type declaration should allow assigning empty array like
     * type[] name = []
     */
    private static void handleArrayType(Type implicit, Type explicit) {
        if (explicit == null || explicit.getKind() != SystemType.ARRAY || implicit.getKind() != SystemType.ARRAY) {
            return;
        }
        if (implicit instanceof ArrayType implicitArray && implicitArray.getType() == null) {
            if (explicit instanceof ArrayType expectedArrayType) {
                implicitArray.setType(expectedArrayType.getType());
            }
        }
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
        switch (expression) {
            case ArrayTypeIdentifier identifier -> {
                Type type = env.lookup(identifier.getType().getValue());
                var res = Optional.ofNullable(type).orElseGet(() -> TypeFactory.fromString(identifier.getType().getValue()));
                return new ArrayType(env, res);
            }
            case TypeIdentifier identifier -> {
                Type type = env.lookup(identifier.getType().getValue());
                return Optional.ofNullable(type).orElseGet(() -> TypeFactory.fromString(identifier.getType().getValue()));
            }
            case SymbolIdentifier identifier -> {
                Type type = env.lookup(identifier.getSymbol());
                return Optional.ofNullable(type).orElseGet(() -> TypeFactory.fromString(identifier.getSymbol()));
            }
            case null, default -> {
            }
        }
        throw new TypeError(expression.string());
    }

    @Override
    public Type visit(NullLiteral expression) {
        return ValueType.Null;
    }

    @Override
    public Type visit(ObjectLiteral expression) {
        return visit(expression.getValue());
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
        List<SystemType> allowedTypes = allowTypes(op);
        this.expectOperatorType(t1, allowedTypes, expression);
        this.expectOperatorType(t2, allowedTypes, expression);

        if (isBooleanOp(op)) { // when is a boolean operation in an if statement, we return a boolean type(the result) else we return the type of the result for a + or *
            expect(t1, t2, left);
            return ValueType.Boolean;
        }
        return expect(t1, t2, left);
    }

    private void expectOperatorType(Type type, List<SystemType> allowedTypes, BinaryExpression expression) {
        if (!allowedTypes.contains(type.getKind())) {
            throw new TypeError("Unexpected type `" + type.getValue() + "` in expression: " + printer.visit(expression) + ". Allowed types: " + allowedTypes);
        }
    }

    private List<SystemType> allowTypes(String op) {
        return switch (op) {
            case "+" -> List.of(SystemType.NUMBER, SystemType.STRING); // allow addition for numbers and string
            case "-", "/", "*", "%" -> List.of(SystemType.NUMBER);
            case "==", "!=" -> List.of(SystemType.STRING, SystemType.NUMBER, SystemType.BOOLEAN, SystemType.OBJECT);
            case "<=", "<", ">", ">=" -> List.of(SystemType.NUMBER, SystemType.BOOLEAN);
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
        if (expectedType == null || !Objects.equals(actualType.getValue(), expectedType.getValue())) {
            // only evaluate printing if we need to
            String string = "Expected type `" + expectedType + "` but got `" + actualType + "` in expression: " + printer.visit(expectedVal);
            throw new TypeError(string);
        }
        if (actualType instanceof ArrayType arrayType && expectedType instanceof ArrayType expectedArrayType) {
            if (arrayType.getType() == null && expectedArrayType.getType() != null) { // reassign an empty array
                return expectedArrayType;
            } else if (!Objects.equals(arrayType.getType().getKind(), expectedArrayType.getType().getKind())) {
                String string = "Expected type `" + expectedArrayType.getType() + "` but got `" + arrayType.getType() + "` in expression: " + printer.visit(expectedVal);
                throw new TypeError(string);
            }
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
            switch (value) {
                case SchemaType schemaValue -> {
                    return schemaValue.getInstances().lookup(resourceName.string());  // vm.main -> if user references the schema we search for the instances of those schemas
                }
                case ResourceType iEnvironment -> {
                    try {
                        return iEnvironment.lookup(resourceName.string());
                    } catch (NotFoundException e) {
                        throw new TypeError(propertyNotFoundOnObject(expression, resourceName));
                    }
                }
                case ObjectType objectType -> {
                    return accessMemberType(expression, resourceName.string(), objectType);
                }
                case null, default -> {
                }
            }
            // else it could be a resource or any other type like a NumericLiteral or something else
        } else if (expression.getProperty() instanceof StringLiteral stringLiteral) {
            var value = executeBlock(expression.getObject(), env);
            switch (value) {
                case ObjectType objectType -> {
                    return accessMemberType(expression, stringLiteral.getValue(), objectType);
                }
                case null, default -> {
                }
            }
            Type lookup = env.lookup(stringLiteral.getValue());
            if (lookup == null) {
                throw new TypeError("Property '" + stringLiteral.getValue() + "' not found on object: " + printer.visit(expression.getObject()) + " in expression: " + printer.visit(expression));
            }
            return lookup;
        }
        throw new OperationNotImplementedException("Membership expression not implemented for: " + expression.getObject());
    }

    private @NotNull Type accessMemberType(MemberExpression expression, String resourceName, ObjectType objectType) {
        Type lookup = objectType.getProperty(resourceName);
        if (lookup != null) {
            // found the property in the object:
            // var a = "a"
            // var b = "b"
            // var c = 0
            // var x = { a: 1 } ; x.a is found
            return lookup;
        }
        try {
            // if property was not found check if is a variable declared in a higher scope
            var storedType = objectType.lookup(resourceName);
            // if we found the var declaration, we know it's value so we try to access the member of the object using the variable value
            // x[a] -> x["a"] works only with strings
            return switch (storedType) {
                // if we found the var declaration, we know it's value so we try to access the member of the object using the variable value
                // x[a] -> x["a"] works only with strings
                case StringType stringType -> objectType.lookup(stringType.getString());
                case null, default -> throw new TypeError(propertyNotFoundOnObject(expression, resourceName));
            };
        } catch (RuntimeException e) {
            throw new TypeError(propertyNotFoundOnObject(expression, resourceName));
        }
    }

    private @NotNull String propertyNotFoundOnObject(MemberExpression expression, SymbolIdentifier resourceName) {
        return propertyNotFoundOnObject(expression, resourceName.string());
    }

    private @NotNull String propertyNotFoundOnObject(MemberExpression expression, String resourceName) {
        return "Property '%s' not found on object: %s in expression: %s"
                .formatted(resourceName, printer.visit(expression.getObject()), printer.visit(expression));
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
        for (var declaration : statement.getDeclarations()) {
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
        var typeEnv = new TypeEnvironment(env);
        if (statement.hasRange()) {
            typeEnv.init(statement.getItem(), ValueType.Number);
        } else if (statement.getArray() != null) {
            typeEnv.init(statement.getItem(), visit(statement.getArray()));
        }
//        visit(statement.getArray());
        return executeBlock(statement.getBody(), typeEnv);
//        var whileStatement = WhileStatement.of(statement.getTest(), BlockExpression.block(statements));
//        if (statement.getItem() == null) {
//            return executeBlock(whileStatement, env);
//        }
//        return executeBlock(BlockExpression.block(statement.getItem(), whileStatement), env);

    }

    @Override
    public Type visit(SchemaDeclaration schema) {
        var name = schema.getName();
        var body = schema.getProperties();

        var schemaType = new SchemaType(name.string(), env);
        env.init(name, schemaType);
        for (var property : body) {
            executeBlock(property.declaration(), schemaType.getEnvironment());
//            var t1 = visit(property.type());
//            if (property.defaultValue() instanceof BlockExpression objectExpression) {
//                var t2 = executeBlock(objectExpression.getExpression(), schemaType.getEnvironment());
//                expect(t2, t1, property.defaultValue());
//                schemaType.setProperty(property.name().string(), t2);
//            } else {
//                var t2 = visit(property.defaultValue());
//                if (t2 != null) { // when property is not initialised
//                    expect(t2, t1, property.defaultValue());
//                }
//                schemaType.setProperty(property.name().string(), t1);
//            }
        }

        return schemaType;
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
            if (!Objects.equals(installedSchema.getProperty(argument.getKey()), argument.getValue())) {
                throw new InvalidInitException("Property type mismatch for " + argument.getKey() + " in:\n " + printer.visit(resource));
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
        if (expression.hasInit()) {
            return initType(expression, expression.getInit(), expression.getType(), var);
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
        try {
            vals.add(var);
            Type returnType;
            if (expression.hasInit()) {
                returnType = initType(expression, expression.getInit(), expression.getType(), var);
            } else if (expression.hasType()) {
                var explicitType = visit(expression.getType());
                returnType = env.init(var, explicitType);
            } else {
                throw new TypeError("Missing explicit and implicit type for expression: " + printer.visit(expression));
            }
            if (returnType instanceof ObjectType objectType) {
                objectType.setImmutable(true);
            }
            return returnType;
        } catch (TypeError error) {
            vals.remove(var);
            throw error;
        }
    }

    /**
     * Check if implicit type (after the =) is the same as explicit type
     */
    private Type initType(Expression expression, Expression init, TypeIdentifier typeIdentifier, String var) {
        var implicitType = visit(init);
        if (typeIdentifier != null) { // val type name
            var explicitType = visit(typeIdentifier);
            handleArrayType(implicitType, explicitType);
            expect(implicitType, explicitType, expression);
            if (StringUtils.equals(implicitType.getValue(), ReferenceType.Object.getValue())) {
                // when it's an object implicit type is the object + all of it's env variable types { name: string }
                // so we must use the implicit evaluation of the object. Explicit one is just an empty object initialised once
                return env.init(var, implicitType);
            }
            return env.init(var, explicitType);
        } else if (implicitType == ValueType.Null) {
            throw new TypeError("Explicit type declaration required for: " + printer.visit(expression));
        }
        if (Objects.equals(implicitType.getValue(), ValueType.String.getValue())) {
            // member assignment like val x = a.b.c
            if (init instanceof StringLiteral stringLiteral) {
                // only needed when val is string because this val could be used to access a member on an object
                return env.init(var, new StringType(stringLiteral.getValue()));
            } else {
                return env.init(var, visit(init)); // x[key] we need to find value of variable key so we store it here
            }
        } else {
            return env.init(var, implicitType);
        }
    }

    @Override
    public Type visit(ObjectExpression expression) {
        TypeEnvironment previous = this.env;
        try {
            this.env = new TypeEnvironment(this.env);
            var objectType = new ObjectType(env);

            for (ObjectLiteral property : expression.getProperties()) {
                var t = visit(property); // check each property
                objectType.setProperty(property.getKey().string(), t);

                var keyType = validatePropertyIsString(property.getKey()); // also check the key but it's only available after bein set in the env
                expect(keyType, ValueType.String, property.getKey()); // make sure the key is alwasy string
            }
            return objectType;
        } finally {
            this.env = previous;
        }
    }

    @Override
    public Type visit(ArrayExpression expression) {
        if (expression.getForStatement() != null) {
            return new ArrayType(env, visit(expression.getForStatement()));
        }

        if (expression.isEmpty()) {
            return new ArrayType(env);
        }
        var firstType = visit(expression.getItems().get(0));
        for (Expression item : expression.getItems()) {
            var itemType = visit(item);
            if (!Objects.equals(itemType, firstType)) {
                throw new TypeError("Array items must be of the same type: %s != %s".formatted(itemType, firstType));
            }
        }
        return new ArrayType(env, firstType);
    }

    @Override
    public Type visit(AnnotationDeclaration expression) {
        if (expression.getValue() != null) {
            return visit(expression.getValue());
        } else if (expression.getArgs() != null) {
            return visit(expression.getArgs());
        } else if (expression.getObject() != null) {
            return visit(expression.getObject());
        } else {
            return visit(expression.getName());
        }
    }

    private Type validatePropertyIsString(Identifier key) {
        return switch (key) {
            case SymbolIdentifier symbolIdentifier -> visit(symbolIdentifier.getSymbol());
            case Identifier symbolIdentifier -> visit(symbolIdentifier.string());
            case null -> null;
        };
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
        var expected = expect(valueType, varType, expression);
        if (expression.getLeft() instanceof SymbolIdentifier symbolIdentifier) {
            assign(expression, symbolIdentifier.string(), expression.getRight(), expected);
        } else if (expression.getLeft() instanceof MemberExpression memberExpression) {
            SymbolIdentifier symbolIdentifier = getSymbolIdentifier(memberExpression);
            if (symbolIdentifier != null) {
                // if trying to override a val object's property, we should forbid it
                assign(expression, symbolIdentifier.string(), expression.getRight(), expected);
            }
        }
        return expected;
    }

    /**
     * Recurse through the member access of an object to reach the root var/val
     * x.y.z -> returns x
     * Then we check if x is cloud(val) and throw error if it is
     */
    private SymbolIdentifier getSymbolIdentifier(MemberExpression expression) {
        if (expression.getObject() instanceof MemberExpression symbolIdentifier) {
            return getSymbolIdentifier(symbolIdentifier);
        } else if (expression.getObject() instanceof SymbolIdentifier symbolIdentifier) {
            return symbolIdentifier;
        }
        return null;
    }

    private void assign(Expression expression, String identifier, Expression right, Type expected) {
        /**
         * check if right hand side type is cloud. For example a val object once it's assigned we can't change it's properties
         * val x = { env: "test" }; x.env -> error
         */
        Type lookup = env.lookup(identifier);
        boolean isImmutable = lookup instanceof ObjectType objectType && objectType.isImmutable();
        if (isImmutable || vals.contains(identifier)) {
            Type visit = visit(right);
            if (Objects.equals(ObjectType.Object.getValue(), visit.getValue())) {
                if (visit == lookup) {
                    throw new TypeError("Cannot assign `" + printer.visit(right) + "` to val `" + identifier + "` in expression: " + printer.visit(expression));
                } else if (vals.contains(identifier)) {
                    throw new TypeError("Cannot assign `" + printer.visit(right) + "` to val `" + identifier + "` in expression: " + printer.visit(expression));
                } else {
                    env.assign(identifier, expected);
                    return;
                }
            }
            throw new TypeError("Cannot assign `" + printer.visit(right) + "` to val `" + identifier + "` in expression: " + printer.visit(expression));
        }
        env.assign(identifier, expected);
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
