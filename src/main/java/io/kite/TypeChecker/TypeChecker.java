package io.kite.TypeChecker;

import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Runtime.exceptions.InvalidInitException;
import io.kite.Runtime.exceptions.NotFoundException;
import io.kite.Runtime.exceptions.OperationNotImplementedException;
import io.kite.TypeChecker.Types.*;
import io.kite.TypeChecker.Types.Decorators.*;
import io.kite.Visitors.SyntaxPrinter;
import io.kite.Visitors.Visitor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Log4j2
public final class TypeChecker implements Visitor<Type> {
    private final SyntaxPrinter printer = new SyntaxPrinter();
    private final Set<String> vals = new HashSet<>();
    @Getter
    private TypeEnvironment env;
    private final Map<String, DecoratorChecker> decoratorInfoMap;

    public TypeChecker() {
        this(new TypeEnvironment());
    }

    public TypeChecker(TypeEnvironment environment) {
        this.env = environment;
        this.decoratorInfoMap = new HashMap<>();
        this.decoratorInfoMap.put(SensitiveDecorator.NAME, new SensitiveDecorator());
        this.decoratorInfoMap.put(CountDecorator.NAME, new CountDecorator());
        this.decoratorInfoMap.put(DescriptionDecorator.NAME, new DescriptionDecorator());
        this.decoratorInfoMap.put(MaxLengthDecorator.NAME, new MaxLengthDecorator());
        this.decoratorInfoMap.put(MinLengthDecorator.NAME, new MinLengthDecorator());
        this.decoratorInfoMap.put(MinValueDecorator.NAME, new MinValueDecorator());
        this.decoratorInfoMap.put(MaxValueDecorator.NAME, new MaxValueDecorator());
        this.decoratorInfoMap.put(AllowedDecorator.NAME, new AllowedDecorator());
        this.decoratorInfoMap.put(DependsOnDecorator.NAME, new DependsOnDecorator());
        this.decoratorInfoMap.put(NonEmptyDecorator.NAME, new NonEmptyDecorator());
        this.decoratorInfoMap.put(UniqueDecorator.NAME, new UniqueDecorator());
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
        throw new TypeError(expression != null ? expression.string() : null);
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

    @Override
    public Type visit(UnionTypeStatement expression) {
        var nameType = visit(expression.name());
        expect(nameType, ValueType.String, expression.getName());

        var unionType = new UnionType(expression.name(), env);
        for (Expression it : expression.getExpressions()) {
            var type = visit(it);
            unionType.getTypes().add(type);
        }

        env.init(expression.getName(), unionType);
        return unionType;
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
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (expectedType != null && Objects.equals(actualType.getValue(), expectedType.getValue())) {
            return expectArray(actualType, expectedType, expectedVal);
        }
        if (expectedType == null) return actualType;

        return switch (expectedType.getKind()) {
            case ARRAY -> expectArray(actualType, expectedType, expectedVal);
            case UNION_TYPE -> expect(actualType, (UnionType) expectedType, expectedVal);
            case ANY ->
                    actualType; // just return the type of the actual value since we don't care what is declared in code
            case null, default -> {
                // only evaluate printing if we need to
                String string = format("Expected type `{0}` but got `{1}` in expression: {2}",
                        expectedType, actualType, printer.visit(expectedVal));
                throw new TypeError(string);
            }
        };
    }

    private Type expectArray(Type actualType, Type expectedType, Expression expectedVal) {
        if (actualType instanceof ArrayType actualArray && expectedType instanceof ArrayType expectedArrayType) {
            if (expectedArrayType.isType(AnyType.INSTANCE)) {
                return expectedArrayType; // skip type checking for any type
            }

            if (actualArray.getType() == null && expectedArrayType.getType() != null) { // reassign an empty array
                return expectedArrayType;
            } else if (actualArray.getType() != null && expectedArrayType.getType() == null) {
                // when we pass expect(visit, ArrayType.ARRAY_TYPE, statement.getArray());
                // ArrayType.ARRAY_TYPE is just array of unkown type so we just want to check that it's an array and don't care about the types of items inside it
                return actualType;
            } else if (expectedArrayType.getType() instanceof UnionType union) {
                return expect(actualArray.getType(), union, expectedVal);
            } else if (!Objects.equals(actualArray.getType().getKind(), expectedArrayType.getType().getKind())) {
                String string = format("Expected type `{0}` but got `{1}` in expression: {2}",
                        expectedArrayType.getType().getValue(), actualArray.getType(), printer.visit(expectedVal));
                throw new TypeError(string);
            }
            return expectedType;
        } else if (expectedType instanceof ArrayType expectedArrayType) {
            expect(actualType, expectedArrayType.getType(), expectedVal);
            return expectedArrayType;
        }
        return expectedType;
    }

    private Type expect(Type actualType, UnionType declaredType, Expression expectedVal) {
        if (Objects.equals(actualType, declaredType)) {
            return declaredType;
        }
        if (!declaredType.getTypes().contains(actualType)) {
            String string = format("Expected type `{0}` with valid values: `{1}` but got `{2}` in expression: `{3}`",
                    printer.visit(declaredType),
                    printer.visit(declaredType),
                    actualType.getValue(),
                    printer.visit(expectedVal));
            throw new TypeError(string);
        }
        if (!(actualType instanceof ObjectType properties)) {
            return declaredType;
        }
        // iterate over init object properties to make sure all declarations match the allowed properties in the union type type
        for (var entry : properties.getEnvironment().getVariables().entrySet()) {
            for (Expression type : declaredType.getTypes()) {
                if (type instanceof ObjectType objectType) {
                    var declared = objectType.lookup(entry.getKey()); // make sure the property exists in the declared type
                    expect(entry.getValue(), declared, declared);
                }
            }
        }
        return declaredType;
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
    public Type visit(ComponentStatement expression) {
        return ReferenceType.Resource;
    }

    @Override
    public Type visit(InputDeclaration expression) {
        var t1 = visit(expression.getType());
        // update inputDeclaration Type because the old type was set by the parser and could be wrong, especially for reference types
        switch (expression.getInit()) {
            case ArrayExpression arrayExpression -> {
                if (expression.getType() instanceof ArrayTypeIdentifier arrayTypeIdentifier) {
                    arrayExpression.setType(arrayTypeIdentifier);
                }
                var t2 = visit(arrayExpression);
                expect(t2, t1, expression.getInit());
            }
            case null -> {

            }
            default -> {
                var t2 = visit(expression.getInit());
                expect(t2, t1, expression.getInit());
            }
        }
        if (expression.getType().getType().getKind() != t1.getKind()) {
            expression.getType().setType(t1);
        }
        // invoke annotations that implement after init checking
        for (AnnotationDeclaration annotation : expression.getAnnotations()) {
            var res = decoratorInfoMap.get(annotation.getName().string());
            res.validateAfterInit(annotation);
        }
        return t1;
    }

    @Override
    public Type visit(OutputDeclaration expression) {
        var t1 = visit(expression.getType());
        // update inputDeclaration Type because the old type was set by the parser and could be wrong, especially for reference types
        switch (expression.getInit()) {
            case ArrayExpression arrayExpression -> {
                if (expression.getType() instanceof ArrayTypeIdentifier arrayTypeIdentifier) {
                    arrayExpression.setType(arrayTypeIdentifier);
                }
                var t2 = visit(arrayExpression);
                expect(t2, t1, expression.getInit());
            }
            case null -> {

            }
            default -> {
                var t2 = visit(expression.getInit());
                expect(t2, t1, expression.getInit());
            }
        }
        if (expression.getType().getType().getKind() != t1.getKind()) {
            expression.getType().setType(t1);
        }
        return t1;
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
            return switch (value) {
                case SchemaType schemaValue -> // vm.main -> if user references the schema we search for the instances of those schemas
                        schemaValue.getInstances().lookup(resourceName.string());
                case ResourceType iEnvironment -> {
                    try {
                        yield iEnvironment.lookup(resourceName.string());
                    } catch (NotFoundException e) {
                        throw new TypeError(propertyNotFoundOnObject(expression, resourceName));
                    }
                }
                case ObjectType objectType -> accessMemberType(expression, resourceName.string(), objectType);
                case null, default -> null;
            };
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
            // if we found the var type, we know it's value so we try to access the member of the object using the variable value
            // x[a] -> x["a"] works only with strings
            return switch (storedType) {
                // if we found the var type, we know it's value so we try to access the member of the object using the variable value
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
            Type visit = visit(statement.getArray());
            // make sure it's an iterable array (for item in vars); vars must be array of any kind
            expect(visit, ArrayType.ARRAY_TYPE, statement.getArray());
            var arrayType = (ArrayType) visit; // the kind of the item we set 'item' to the type of the array elements type
            typeEnv.init(statement.getItem(), arrayType.getType());
        }
        return executeBlock(statement.getBody(), typeEnv);
    }

    @Override
    public Type visit(SchemaDeclaration schema) {
        var name = schema.getName();
        var body = schema.getProperties();

        var schemaType = new SchemaType(name.string(), env);
        env.init(name, schemaType);
        for (SchemaProperty property : body) {
            var vardeclaration = VarDeclaration.var(property.name(), property.type(), property.init());
            executeBlock(vardeclaration, schemaType.getEnvironment());
        }

        return schemaType;
    }

    @Override
    public Type visit(ResourceExpression resource) {
        if (resource.getName() == null) {
            throw new InvalidInitException("Resource does not have a name: " + resourceName(resource));
        }
        // SchemaValue already installed globally when evaluating a SchemaDeclaration.
        // This means the schema must be declared before the resource
        var installedSchema = (SchemaType) env.lookup(resource.getType().string());
        if (installedSchema == null) {
            throw new InvalidInitException("Schema not found during " + resourceName(resource) + " initialization");
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


        var resourceType = new ResourceType(resourceName(resource), installedSchema, resourceEnv);
        installedSchema.addInstance(resourceName(resource), resourceType);

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
//            var res = installedSchema.initInstance(resourceName(resource), ResourceValue.of(resourceName(resource), resourceEnv, installedSchema));
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
            if (explicitType instanceof UnionType unionType) {
                var unionType1 = expect(implicitType, unionType, expression);
                return env.init(var, unionType1);
            } else {
                expect(implicitType, explicitType, expression);
            }
            if (StringUtils.equals(implicitType.getValue(), ObjectType.INSTANCE.getValue())) {
                // when it's an object implicit type is the object + all of it's env variable types { name: string }
                // so we must use the implicit evaluation of the object. Explicit one is just an empty object initialised once
                return env.init(var, implicitType);
            }
            return env.init(var, explicitType);
        } else if (implicitType == ValueType.Null) {
            throw new TypeError("Explicit type type required for: " + printer.visit(expression));
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
                objectType.setProperty(property.keyString(), t);

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
        // [] (no items, no explicit type)
        if (expression.isEmpty()) {
            return new ArrayType(env);
        }
        // Try explicit type first (e.g., [: string | number])
        var explicit = expression.getType();
        if (explicit != null) {
            var arrFromExplicit = tryArrayTypeFromExplicit(expression, explicit);
            if (arrFromExplicit != null) return arrFromExplicit;
        }
        // Fallback: infer from first item
        var firstItemType = visit(expression.getItems().get(0));
        return inferArrayTypeOrWrap(expression, Set.of(firstItemType), firstItemType);
    }

    private ArrayType tryArrayTypeFromExplicit(ArrayExpression expr, Expression explicitTypeNode) {
        var explicitType = visit(explicitTypeNode);
        if (!(explicitType instanceof ArrayType at)) return null;

        var element = at.getType();
        if (element instanceof UnionType ut) {
            // We "visit" the union's members to normalize aliases/etc.
            var visitedMembers = ut.getTypes().stream()
                    .map(this::visit)
                    .collect(Collectors.toSet());

            var inferred = getArrayType(expr, visitedMembers);
            return inferred != null ? inferred : new ArrayType(env, ut);
        }

        // Non-union explicit arrays don't need special handling here
        return null;
    }

    private ArrayType inferArrayTypeOrWrap(ArrayExpression expr,
                                           Set<Type> candidateElementTypes,
                                           Type fallbackElementType) {
        var inferred = getArrayType(expr, candidateElementTypes);
        return inferred != null ? inferred : new ArrayType(env, fallbackElementType);
    }

    private ArrayType getArrayType(ArrayExpression arrayExpression, Set<Type> expression) {
        for (Expression item : arrayExpression.getItems()) {
            var itemType = visit(item);
            if (!expression.contains(itemType)) {
//                throw new TypeError("Array items must be of the same type: %s != %s".formatted(itemType, firstType));
                // if the first item is not the same as the rest of the items then we return any type
                return new ArrayType(env, AnyType.INSTANCE);
            }
        }
        return null;
    }

    /**
     * This must be manually called from the following statement(output, input, resource, schema, schemaProperty, component)
     */
    @Override
    public Type visit(AnnotationDeclaration declaration) {
        var decoratorInfo = decoratorInfoMap.get(declaration.name());
        if (decoratorInfo == null) {
            var message = Ansi.ansi().fgYellow().a("@").a(declaration.name()).reset().a(" decorator is unknown").toString();
            throw new TypeError(message);
        }
        if (decoratorInfo.hasValidArguments(declaration)) {
            var message = Ansi.ansi().fgYellow().a("@").a(declaration.name()).reset().a(" must not have any arguments").toString();
            throw new TypeError(message);
        }

        decoratorInfo.validate(declaration);

        if (!decoratorInfo.isOnValidTarget(declaration.getTarget())) {
            var ansi = Ansi.ansi().fgYellow().a("@").a(declaration.name()).reset()
                    .a(" can only be used on: ")
                    .fgMagenta()
                    .a(decoratorInfo.targetString()).reset();
            throw new TypeError(ansi.toString());
        }

        return decoratorInfo.getType();
    }

    private Type validatePropertyIsString(Expression key) {
        return switch (key) {
            case SymbolIdentifier symbolIdentifier -> visit(symbolIdentifier.getSymbol());
            case Identifier symbolIdentifier -> visit(symbolIdentifier.string());
            case StringLiteral symbolIdentifier -> visit(symbolIdentifier);
            case null -> null;
            default -> throw new IllegalStateException("Unexpected value: " + key);
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
        switch (expression.getLeft()) {
            case SymbolIdentifier symbolIdentifier ->
                    assign(expression, symbolIdentifier.string(), expression.getRight(), expected);
            case MemberExpression memberExpression -> {
                var identifier = getSymbolIdentifier(memberExpression);
                if (identifier != null) {
                    // if trying to override a val object's property, we should forbid it
                    assign(expression, identifier.string(), expression.getRight(), expected);
                }
            }
            case null, default -> {
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
            if (Objects.equals(ObjectType.INSTANCE.getValue(), visit.getValue())) {
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
