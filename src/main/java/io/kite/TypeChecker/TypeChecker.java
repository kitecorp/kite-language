package io.kite.TypeChecker;

import io.kite.Frontend.Parse.Literals.*;
import io.kite.Frontend.Parser.Expressions.*;
import io.kite.Frontend.Parser.Program;
import io.kite.Frontend.Parser.Statements.*;
import io.kite.Frontend.annotations.CountAnnotatable;
import io.kite.Runtime.CycleDetectionSupport;
import io.kite.Runtime.Values.Deferred;
import io.kite.Runtime.exceptions.DeclarationExistsException;
import io.kite.Runtime.exceptions.InvalidInitException;
import io.kite.Runtime.exceptions.NotFoundException;
import io.kite.Runtime.exceptions.OperationNotImplementedException;
import io.kite.TypeChecker.Types.*;
import io.kite.TypeChecker.Types.Decorators.*;
import io.kite.Visitors.StackVisitor;
import io.kite.Visitors.SyntaxPrinter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Log4j2
public final class TypeChecker extends StackVisitor<Type> {
    private static final List<SystemType> NUMERIC_OPS = List.of(SystemType.NUMBER);
    private static final List<SystemType> ADDITIVE_OPS = List.of(SystemType.NUMBER, SystemType.STRING);
    private static final List<SystemType> EQUALITY_OPS = List.of(SystemType.STRING, SystemType.NUMBER, SystemType.BOOLEAN, SystemType.OBJECT);
    private static final List<SystemType> COMPARISON_OPS = List.of(SystemType.NUMBER, SystemType.BOOLEAN);
    @Getter
    private final SyntaxPrinter printer = new SyntaxPrinter();
    private final Set<String> vals = new HashSet<>();
    private final Map<String, DecoratorChecker> decoratorInfoMap;
    private final ComponentRegistry componentRegistry;

    @Getter
    private TypeEnvironment env;

    public TypeChecker() {
        this(new TypeEnvironment("global"));
    }

    public TypeChecker(TypeEnvironment environment) {
        this.env = environment;
        for (var value : ValueType.values()) {
            env.init(value.getValue(), value);
        }
        for (var value : ReferenceType.values()) {
            env.init(value.getValue(), value);
        }
        env.init("pow", TypeFactory.fromString("(%s,%s)->%s".formatted(ValueType.Number.getValue(), ValueType.Number.getValue(), ValueType.Number.getValue())));
        env.init("toString", TypeFactory.fromString("(%s)->%s".formatted(ValueType.Number.getValue(), ValueType.String.getValue())));
        env.init("print", TypeFactory.add(FunType.fun(ValueType.Void, AnyType.INSTANCE)));
        env.init("println", TypeFactory.add(FunType.fun(ValueType.Void, AnyType.INSTANCE)));

        this.componentRegistry = new ComponentRegistry();

        this.decoratorInfoMap = new HashMap<>();
        this.decoratorInfoMap.put(SensitiveDecorator.NAME, new SensitiveDecorator());
        this.decoratorInfoMap.put(CountDecorator.NAME, new CountDecorator(this));
        this.decoratorInfoMap.put(DescriptionDecorator.NAME, new DescriptionDecorator());
        this.decoratorInfoMap.put(MaxLengthDecorator.NAME, new MaxLengthDecorator());
        this.decoratorInfoMap.put(MinLengthDecorator.NAME, new MinLengthDecorator());
        this.decoratorInfoMap.put(MinValueDecorator.NAME, new MinValueDecorator());
        this.decoratorInfoMap.put(MaxValueDecorator.NAME, new MaxValueDecorator());
        this.decoratorInfoMap.put(AllowedDecorator.NAME, new AllowedDecorator());
        this.decoratorInfoMap.put(DependsOnDecorator.NAME, new DependsOnDecorator());
        this.decoratorInfoMap.put(NonEmptyDecorator.NAME, new NonEmptyDecorator());
        this.decoratorInfoMap.put(UniqueDecorator.NAME, new UniqueDecorator());
        this.decoratorInfoMap.put(ValidateDecorator.NAME, new ValidateDecorator());
        this.decoratorInfoMap.put(ProviderDecorator.NAME, new ProviderDecorator(printer));
        this.decoratorInfoMap.put(TagsDecorator.NAME, new TagsDecorator(printer));
        this.decoratorInfoMap.put(ExistingDecorator.NAME, new ExistingDecorator(printer));
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
            return super.visit(expression);
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

    @NotNull
    private Type executeBlock(List<Statement> statements, TypeEnvironment environment, Class<?>... filterBy) {
        TypeEnvironment previous = this.env;
        try {
            this.env = environment;
            Type res = ValueType.Null;
            for (Statement statement : statements) {
                for (Class<?> aClass : filterBy) {
                    if (statement.getClass().equals(aClass)) {
                        res = visit(statement);
                    }
                }
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
        push(statement);
        TypeEnvironment previous = this.env;
        try {
            this.env = environment;
            return visit(statement);
        } finally {
            this.env = previous;
            pop(statement);
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
            case "+" -> ADDITIVE_OPS;
            case "-", "/", "*", "%" -> NUMERIC_OPS;
            case "==", "!=" -> EQUALITY_OPS;
            case "<=", "<", ">", ">=" -> COMPARISON_OPS;
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
        // Null expected type means accept any actual type
        if (expectedType == null || expectedType == ValueType.Null) {
            return actualType;
        }

        // Types match exactly
        if (Objects.equals(actualType, expectedType)) {
            return expectArray(actualType, expectedType, expectedVal);
        }

        return switch (expectedType.getKind()) {
            case ARRAY -> expectArray(actualType, expectedType, expectedVal);
            case UNION_TYPE -> expect(actualType, (UnionType) expectedType, expectedVal);
            case ANY -> actualType; // Accept any actual type without validation
            case null, default -> throw new TypeError(format(
                    "Expected type `{0}` but got `{1}` in expression: {2}",
                    expectedType, actualType, printer.visit(expectedVal)
            ));
        };
    }

    private Type expectArray(Type actualType, Type expectedType, Expression expectedVal) {
        if (!(actualType instanceof ArrayType actualArray)) {
            return handleNonArrayActual(actualType, expectedType, expectedVal);
        }

        if (!(expectedType instanceof ArrayType expectedArrayType)) {
            return expectedType;
        }

        // Skip type checking for any type
        if (expectedArrayType.isType(AnyType.INSTANCE)) {
            return expectedArrayType;
        }

        // Reassign an empty array
        if (actualArray.getType() == null && expectedArrayType.getType() != null) {
            return expectedArrayType;
        }

        // Check if it's an array without caring about element types (e.g., ArrayType.ARRAY_TYPE)
        if (actualArray.getType() != null && expectedArrayType.getType() == null) {
            return actualType;
        }

        // Handle union types
        if (expectedArrayType.getType() instanceof UnionType union) {
            return expect(actualArray.getType(), union, expectedVal);
        }

        // Validate element type compatibility
        if (!Objects.equals(actualArray.getType().getKind(), expectedArrayType.getType().getKind())) {
            throw new TypeError(format(
                    "Expected type `{0}` but got `{1}` in expression: {2}",
                    expectedArrayType.getType().getValue(),
                    actualArray.getType(),
                    printer.visit(expectedVal)
            ));
        }

        return expectedType;
    }

    private Type handleNonArrayActual(Type actualType, Type expectedType, Expression expectedVal) {
        if (expectedType instanceof ArrayType expectedArrayType) {
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
            throw new TypeError(format(
                    "Expected type `{0}` with valid values: `{1}` but got `{2}` in expression: `{3}`",
                    printer.visit(declaredType),
                    printer.visit(declaredType),
                    actualType.getValue(),
                    printer.visit(expectedVal)
            ));
        }

        if (actualType instanceof ObjectType objectType) {
            validateObjectProperties(objectType, declaredType);
        }

        return declaredType;
    }

    private void validateObjectProperties(ObjectType actualObject, UnionType declaredType) {
        // Iterate over init object properties to ensure all match the allowed properties in the union type
        for (var entry : actualObject.getEnvironment().getVariables().entrySet()) {
            for (Expression type : declaredType.getTypes()) {
                if (type instanceof ObjectType declaredObject) {
                    var declaredProperty = declaredObject.lookup(entry.getKey());
                    expect(entry.getValue(), declaredProperty, declaredProperty);
                }
            }
        }
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

    private Type expect(Type actualType, Type expectedType, Statement actualVal) {
        if (actualType == ValueType.Null) {
            return expectedType;
        }
        if (expectedType == ValueType.Null) {
            return actualType;
        }
        if (!Objects.equals(actualType, expectedType)) {
            // only evaluate printing if we need to
            String string = "Expected type " + printer.visit(expectedType) + " but got " + printer.visit(actualType) + " in expression: " + printer.visit(actualVal);
            throw new TypeError(string);
        }
        return actualType;
    }

    @Override
    public Type visit(ErrorExpression expression) {
        return null;
    }

    @Override
    public Type visit(InputDeclaration expression) {
        var declaredType = visit(expression.getType());

        if (expression.getInit() != null) {
            validateInitializer(expression, declaredType);
        }

        // Update InputDeclaration type if it differs (important for reference types set by parser)
        if (expression.getType().getType().getKind() != declaredType.getKind()) {
            expression.getType().setType(declaredType);
        }

        onFinalAnnotations(expression.getAnnotations());
        return env.init(expression.getId(), declaredType);
    }

    private void validateInitializer(InputDeclaration expression, Type declaredType) {
        switch (expression.getInit()) {
            case ArrayExpression arrayExpression -> {
                if (expression.getType() instanceof ArrayTypeIdentifier arrayTypeIdentifier) {
                    arrayExpression.setType(arrayTypeIdentifier);
                }
                var initType = visit(arrayExpression);
                expect(initType, declaredType, expression.getInit());
            }
            default -> {
                var initType = visit(expression.getInit());
                expect(initType, declaredType, expression.getInit());
            }
        }
    }

    private void onFinalAnnotations(Set<AnnotationDeclaration> list) {
        // invoke annotations that implement after init checking
        for (var annotation : list) {
            var res = decoratorInfoMap.get(annotation.getName().string());
            res.onTargetEvaluated(annotation);
        }
    }

    @Override
    public Type visit(OutputDeclaration expression) {
        var declaredType = visit(expression.getType());

        if (expression.getInit() != null) {
            validateInitializer(expression, declaredType);
        }

        // Update OutputDeclaration type if it differs (important for reference types set by parser)
        if (expression.getType().getType().getKind() != declaredType.getKind()) {
            expression.getType().setType(declaredType);
        }

        onFinalAnnotations(expression.getAnnotations());
        return env.init(expression.getId(), declaredType);
    }

    private void validateInitializer(OutputDeclaration expression, Type declaredType) {
        switch (expression.getInit()) {
            case ArrayExpression arrayExpression -> {
                if (expression.getType() instanceof ArrayTypeIdentifier arrayTypeIdentifier) {
                    arrayExpression.setType(arrayTypeIdentifier);
                }
                var initType = visit(arrayExpression);
                expect(initType, declaredType, expression.getInit());
            }
            default -> {
                var initType = visit(expression.getInit());
                expect(initType, declaredType, expression.getInit());
            }
        }
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
        return switch (expression.getProperty()) {
            case SymbolIdentifier resourceName -> visitSymbolMember(expression, resourceName);
            case StringLiteral stringLiteral -> visitStringMember(expression, stringLiteral);
            case NumberLiteral numberLiteral -> visitNumberMember(expression, numberLiteral);
            case null, default -> throw new OperationNotImplementedException(
                    "Membership expression not implemented for: " + printer.visit(expression)
            );
        };
    }

    private Type visitSymbolMember(MemberExpression expression, SymbolIdentifier resourceName) {
        var objectType = executeBlock(expression.getObject(), env);

        return switch (objectType) {
            case SchemaType schemaType -> lookupSchemaInstance(schemaType, resourceName);
            case ResourceType resourceType -> lookupResourceProperty(expression, resourceType, resourceName);
            case ComponentType componentType -> lookupComponentMember(componentType, resourceName);
            case ObjectType obj -> accessMemberType(expression, resourceName.string(), obj);
            case null, default -> objectType;
        };
    }

    private Type lookupComponentMember(ComponentType componentType, SymbolIdentifier memberName) {
        String name = memberName.string();

        // Check if this is a component definition (no instance name)
        if (componentType.getName() == null) {
            var context = ExecutionContext(ComponentStatement.class);
            if (context instanceof ComponentStatement statement && statement.hasName()) {
                throw new TypeError(
                        "Cannot access component definition '%s.%s'. Only component instances can be referenced."
                                .formatted(componentType.getType(), memberName.string())
                );
            }
        }

        Type member = componentType.lookup(name);

        if (member == null) {
            throw new TypeError(format("Component '{0}' does not have member '{1}'", componentType.getType(), name));
        }

        // If accessing a component INSTANCE, only allow outputs (not resources or inputs)
        if (member instanceof ResourceType) {
            throw new TypeError(
                    format("Cannot access resource `{0}` from component instance `{1}`. Only outputs are accessible. Consider exposing `{0}` as an output.", name, componentType.getName()));
        }

//        // Block direct input access - inputs are private
//        if (member instanceof Input || isInputType(member)) {  // Check however you identify inputs
//            String componentRef = componentType.getName() != null ?
//                    "instance `" + componentType.getName() + "`" :
//                    "definition `" + componentType.getType() + "`";
//            throw new TypeError(format(
//                    "Cannot access input `{0}` from component {1}. Inputs are private to the component.",
//                    name, componentRef
//            ));
//        }

        return member;
    }

    private Type lookupSchemaInstance(SchemaType schemaType, SymbolIdentifier resourceName) {
        // When retrieving the type of a resource, we first check the "instances" field for existing resources
        // Since that environment points to the parent (type env), it will also find the properties
        var result = CycleDetectionSupport.propertyOrDeferred(
                schemaType.getInstances().getVariables(),
                resourceName.string()
        );

        return result instanceof Deferred deferred ? new AnyType(deferred) : (Type) result;
    }

    private Type lookupResourceProperty(MemberExpression expression, ResourceType resourceType, SymbolIdentifier resourceName) {
        try {
            return resourceType.lookup(resourceName.string());
        } catch (NotFoundException e) {
            throw new TypeError(propertyNotFoundOnObject(expression, resourceName));
        }
    }

    private Type visitStringMember(MemberExpression expression, StringLiteral stringLiteral) {
        var objectType = executeBlock(expression.getObject(), env);

        if (objectType instanceof ObjectType obj) {
            return accessMemberType(expression, stringLiteral.getValue(), obj);
        }

        Type lookup = env.lookup(stringLiteral.getValue());
        if (lookup == null) {
            throw new TypeError(
                    "Property '" + stringLiteral.getValue() + "' not found on object: "
                    + printer.visit(expression.getObject()) + " in expression: " + printer.visit(expression)
            );
        }
        return lookup;
    }

    private Type visitNumberMember(MemberExpression expression, NumberLiteral numberLiteral) {
        // For computed expressions, we forward to the object evaluation
        return executeBlock(expression.getObject(), env);
    }

    private @NotNull Type accessMemberType(MemberExpression expression, String resourceName, ObjectType objectType) {
        Type directProperty = objectType.getProperty(resourceName);
        if (directProperty != null) {
            // found the property in the object:
            // var a = "a"
            // var b = "b"
            // var c = 0
            // var x = { a: 1 } ; x.a is found
            return directProperty;
        }

        try {
            // if property was not found check if is a variable declared in a higher scope
            var variableType = objectType.lookup(resourceName);

            // if we found the var type, we know it's value so we try to access the member of the object using the variable value
            // x[a] -> x["a"] works only with strings
            if (variableType instanceof StringType stringType) {
                return objectType.lookup(stringType.getString());
            }

            throw new TypeError(propertyNotFoundOnObject(expression, resourceName));
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
        var returnType = expression.getReturnType();

        var funType = new FunType(params.values(), returnType.getType());
        env.init(expression.getName(), funType); // save function signature in env so that we're able to call it later to validate types

        var actualReturn = validateBody(returnType.getType(), expression.getBody(), params);
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

        return expect(actualReturnType, returnType, body);
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
        expect(condition, ValueType.Boolean, statement); // condition should always be boolean
        return visit(statement.getBody());
    }

    @Override
    public Type visit(ForStatement statement) {
        var typeEnv = new TypeEnvironment(env);

        if (statement.hasRange()) {
            typeEnv.init(statement.getItem(), ValueType.Number);
        } else if (statement.getArray() != null) {
            var arrayType = validateAndGetArrayType(statement);
            typeEnv.init(statement.getItem(), arrayType.getType());
        }

        return executeBlock(statement.getBody(), typeEnv);
    }

    private ArrayType validateAndGetArrayType(ForStatement statement) {
        Type iterableType = visit(statement.getArray());
        // Ensure it's an iterable array (for item in vars); vars must be an array of any kind
        expect(iterableType, ArrayType.ARRAY_TYPE, statement.getArray());
        return (ArrayType) iterableType;
    }

    @Override
    public Type visit(SchemaDeclaration schema) {
        var name = schema.getName();
        var body = schema.getProperties();

        var schemaType = new SchemaType(name.string(), new TypeEnvironment(name.string(), env));
        env.init(name, schemaType);
        for (SchemaProperty property : body) {
            var vardeclaration = VarDeclaration.var(property.name(), property.type(), property.init());
            executeBlock(vardeclaration, schemaType.getEnvironment());
        }

        return schemaType;
    }

    @Override
    public Type visit(ResourceStatement resource) {
        validateResourceName(resource);

        var installedSchema = lookupSchema(resource);

        String resourceName = resourceName(resource);
        if (isCounted(resource.targetType())) {
            return installedSchema.getInstance(resourceName);
        }

        var resourceEnv = createResourceEnvironment(installedSchema, resource, resourceName);
        validateResourceProperties(resourceEnv, installedSchema, resource);

        var resourceType = new ResourceType(resourceName, installedSchema, resourceEnv);
        if (ExecutionContextIn(ComponentStatement.class)) {
            env.init(resourceName, resourceType);
        } else {
            installedSchema.addInstance(resourceName, resourceType);
        }

        return resourceType;
    }

    private void validateResourceName(ResourceStatement resource) {
        if (resource.getName() == null) {
            throw new InvalidInitException("Resource does not have a name: " + resourceName(resource));
        }
    }

    private SchemaType lookupSchema(ResourceStatement resource) {
        // SchemaValue already installed globally when evaluating a SchemaDeclaration.
        // This means the schema must be declared before the resource
        var installedSchema = (SchemaType) env.lookup(resource.getType().string());
        if (installedSchema == null) {
            throw new InvalidInitException("Schema not found during " + resourceName(resource) + " initialization");
        }
        return installedSchema;
    }

    /**
     * Method used for the @counted decorator. Once we evaluate the type using the @counted we don't need
     * to initialise again when the ast reaches the type declaration.
     */
    private boolean isCounted(Type type) {
        if (type instanceof CountAnnotatable annotatable && annotatable.isCounted()) {
            annotatable.setCounted(false);
            return true;
        }
        return false;
    }

    private TypeEnvironment createResourceEnvironment(SchemaType installedSchema, ResourceStatement resource, String resourceName) {
        var schemaEnv = installedSchema.getEnvironment();
        // Clone/inherit all default properties from schema properties to the new resource
        var resourceEnv = new TypeEnvironment(resourceName, env, schemaEnv.getVariables());
        // Init resource environment with values defined by the user
        executeBlock(resource.getArguments(), resourceEnv);
        return resourceEnv;
    }

    private void validateResourceProperties(TypeEnvironment resourceEnv, SchemaType installedSchema, ResourceStatement resource) {
        // Validate each property in the resource matches the type defined in the schema
        for (var argument : resourceEnv.getVariables().entrySet()) {
            if (!Objects.equals(installedSchema.getProperty(argument.getKey()), argument.getValue())) {
                throw new InvalidInitException(
                        "Property type mismatch for " + argument.getKey() + " in:\n " + printer.visit(resource)
                );
            }
        }
    }

    @Override
    public Type visit(ComponentStatement expression) {
        validateComponentStatement(expression);

        if (isCounted(expression.targetType())) {
            return visit(expression.getType());
        }

        return createComponent(expression);
    }

    private void validateComponentStatement(ComponentStatement expression) {
        if (!expression.hasType()) {
            throw new TypeError("Invalid component declaration: " + printer.visit(expression));
        }
    }

    /**
     * Creates either:
     * - Component declaration: component typeName {...}
     * - Component instance: component typeName name {...}
     */
    private Type createComponent(ComponentStatement expression) {
        String typeName = expression.getType().string();
        boolean typeExists = env.lookupKey(typeName);

        if (typeExists && expression.isDefinition()) {
            throw new TypeError("Component type already exists: " + printer.visit(expression));
        }

        if (!typeExists && expression.hasName()) {
            throw new InvalidInitException(format(
                    "Component type {0} not declared: {1}", printer.visit(expression.getType()), printer.visit(expression)
            ));
        }

        return typeExists ? initializeComponent(expression) : declareComponent(expression);
    }

    /**
     * Declares a new component type and registers it in both the registry and environment
     */
    private Type declareComponent(ComponentStatement expression) {
        String typeName = expression.getType().string();

        // Register in component registry for later instantiation
        componentRegistry.registerDeclaration(typeName, expression);

        // Create component type with its own environment
        var componentType = new ComponentType(typeName, new TypeEnvironment(typeName, env));

        // Execute declaration block to validate and initialize inputs, outputs, and resources
        executeBlock(expression.getArguments(), componentType.getEnvironment());

        // Register in type environment
        return env.init(typeName, componentType);
    }

    /**
     * Creates a new instance of a declared component type
     */
    private Type initializeComponent(ComponentStatement expression) {
        validateNotNestedInitialization(expression);

        String instanceName = expression.name();
        String componentType = expression.getType().string();

        // Get the declaration from registry
        ComponentStatement declaration = componentRegistry.getDeclaration(componentType);
        if (declaration == null) {
            throw new TypeError("Component declaration not found: " + componentType);
        }

        // Create instance with its own environment
        var instance = new ComponentType(componentType, instanceName, new TypeEnvironment(env));

        try {
            var result = env.init(instanceName, instance);

            // First execute declaration block to set up base structure (resources, etc.)
            executeBlock(declaration.getArguments(), instance.getEnvironment());

            // Then execute initialization block (typically sets input values)
            if (!expression.getArguments().isEmpty()) {
                validateInitializationBlock(expression.getArguments()); // ADD THIS
                executeBlock(expression.getArguments(), instance.getEnvironment());
            }

            return result;
        } catch (DeclarationExistsException e) {
            throw new InvalidInitException(format("Component instance already exists: {0}", printer.visit(expression)));
        }
    }

    private void validateInitializationBlock(List<Statement> statements) {
        for (Statement stmt : statements) {
            if (stmt instanceof ExpressionStatement statement) {
                if (!(statement.getStatement() instanceof AssignmentExpression)) {
                    throw new TypeError("Cannot declare resources in component initialization");
                }
            } else {
                throw new TypeError("Cannot declare resources in component initialization");
            }
        }
    }

    /**
     * Validates that component initialization is not happening inside a component definition
     */
    private void validateNotNestedInitialization(ComponentStatement expression) {
        var parentContext = ExecutionContext(ComponentStatement.class);

        if (parentContext instanceof ComponentStatement parentStatement) {
            if (parentStatement.isDefinition()) {
                throw new InvalidInitException(
                        "Component initialization not allowed inside component definition: %s"
                                .formatted(printer.visit(expression))
                );
            }
        }
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

        if (typeIdentifier != null) {
            return initWithExplicitType(expression, typeIdentifier, var, implicitType);
        }

        if (implicitType == ValueType.Null) {
            throw new TypeError("Explicit type required for: " + printer.visit(expression));
        }

        if (Objects.equals(implicitType, ValueType.String) && init instanceof StringLiteral stringLiteral) {
            return env.init(var, new StringType(stringLiteral.getValue()));
        }

        return env.init(var, implicitType);
    }

    private Type initWithExplicitType(Expression expression, TypeIdentifier typeIdentifier, String var, Type implicitType) {
        var explicitType = visit(typeIdentifier);

        if (explicitType instanceof UnionType unionType) {
            var resolvedType = expect(implicitType, unionType, expression);
            return env.init(var, resolvedType);
        }

        expect(implicitType, explicitType, expression);

        // Use implicit type for objects to preserve environment variable types
        var typeToInit = implicitType.getKind() == ObjectType.INSTANCE.getKind()
                ? implicitType
                : explicitType;

        return env.init(var, typeToInit);
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

        decoratorInfo.validate(declaration);

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
        /*
         todo when a resource property is deferred through implicit dependency we skip
          any type checking here because the type of the property is not yet known. We would need to evaluate the dependency
          resource first and then validate the property type by revisiting this resource.
          but for now, I don't care since the dependency cycle and value is done in the interpreter.
          if the language becomes popular, we should consider doing this type checking as well instead of just skipping it.
          ex:
          resource vm main { a = second.a }  // references a resource not evaluated yet so we must deferr evaluation
          resource vm main { a = "second" }
        */
        if (valueType instanceof AnyType any && any.getAny() instanceof Deferred deferred) {
            return any;
        }
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
         * Check if right hand side type is immutable. For example a val object once it's assigned we can't change its properties
         * val x = { env: "test" }; x.env -> error
         */
        Type lookup = env.lookup(identifier);
        boolean isImmutable = lookup instanceof ObjectType objectType && objectType.isImmutable();

        if (!isImmutable && !vals.contains(identifier)) {
            env.assign(identifier, expected);
            return;
        }

        Type rightType = visit(right);

        // Allow reassigning object types if they're different instances (not the same reference)
        if (Objects.equals(ObjectType.INSTANCE.getValue(), rightType.getValue())
            && rightType != lookup
            && !vals.contains(identifier)) {
            env.assign(identifier, expected);
            return;
        }

        throw new TypeError(
                "Cannot assign `" + printer.visit(right) + "` to val `" + identifier
                + "` in expression: " + printer.visit(expression)
        );
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
