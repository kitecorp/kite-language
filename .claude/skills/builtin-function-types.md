# Skill: Built-in Function Type Registration

## When to Use

Use this skill when adding or modifying built-in functions in the TypeChecker. Instead of adding special case handling in `visit(CallExpression)`, define the function's parameter types correctly using union types.

## Principle

**"Let the type system do the work"**

Built-in functions should be registered with precise type signatures. The existing `checkArgs` method will automatically validate arguments against these types - no special case code needed.

## Pattern

### Before (Anti-pattern)

Adding special validation in `visit(CallExpression)`:

```java
// BAD: Special case handling for length()
var calleeName = printer.visit(expression.getCallee());
if ("length".equals(calleeName) && !passedArgumentsTypes.isEmpty()) {
    var argType = passedArgumentsTypes.getFirst();
    if (!"string".equals(argType.getValue()) && !(argType instanceof ArrayType)) {
        throw new TypeError("length() requires string or array, got " + argType.getValue());
    }
}
```

### After (Correct pattern)

Define the function with a proper union type parameter:

```java
// GOOD: Type signature handles validation automatically
env.init("length", TypeFactory.add(FunType.fun(
    ValueType.Number,  // return type
    UnionType.unionType("string|array", ValueType.String, ArrayType.arrayType(AnyType.INSTANCE))
)));
```

## Built-in Function Registration Examples

### Single parameter type

```java
// toString takes number, returns string
env.init("toString", TypeFactory.fromString("(number)->string"));
```

### Multiple parameter types

```java
// pow takes two numbers, returns number
env.init("pow", TypeFactory.fromString("(number,number)->number"));
```

### Any type parameter

```java
// print accepts any type
env.init("print", TypeFactory.add(FunType.fun(ValueType.Void, AnyType.INSTANCE)));
```

### Union type parameter

```java
// length accepts string or array, returns number
env.init("length", TypeFactory.add(FunType.fun(
    ValueType.Number,
    UnionType.unionType("string|array", ValueType.String, ArrayType.arrayType(AnyType.INSTANCE))
)));
```

### Optional parameters

For functions with optional parameters (like `substring(string, start, end?)`):

1. Register with the minimum required params
2. Add the function name to `FUNCTIONS_WITH_OPTIONAL_PARAMS`

```java
// In TypeChecker class constants
private static final Set<String> FUNCTIONS_WITH_OPTIONAL_PARAMS = Set.of("substring");

// Register with minimum required params (string, number)
// The optional 3rd param (end index) is handled by the interpreter
env.init("substring", TypeFactory.add(FunType.fun(ValueType.String, ValueType.String, ValueType.Number)));
```

## Type System Support

The `expect` method for UnionType handles array compatibility automatically:

```java
// In TypeChecker.java
private boolean unionTypeContains(UnionType unionType, Type actualType) {
    if (unionType.getTypes().contains(actualType)) {
        return true;
    }
    // any[] matches any array type (number[], string[], etc.)
    if (actualType instanceof ArrayType) {
        return unionType.getTypes().stream()
                .anyMatch(t -> t instanceof ArrayType);
    }
    return false;
}
```

## Checklist

When adding a new built-in function:

1. **Identify the parameter types**: What types should the function accept?
2. **Identify the return type**: What type does the function return?
3. **Choose the right type expression**:
   - Single type: Use `ValueType.X` or `ArrayType.ARRAY_TYPE`
   - Multiple types: Use `UnionType.unionType("type1|type2", ...)`
   - Any type: Use `AnyType.INSTANCE`
4. **Register in TypeChecker constructor**: Add `env.init("funcName", TypeFactory.add(...))`
5. **Add tests**: Verify accepted and rejected argument types

## Benefits

| Approach | Special Case | Union Type |
|----------|--------------|------------|
| Code location | Scattered in visit() | Centralized in constructor |
| Maintainability | Must update validation code | Change type signature only |
| Error messages | Custom messages needed | Consistent with other functions |
| New functions | Copy-paste validation | Follow established pattern |

## Files

- `TypeChecker.java` - Function registration in constructor
- `UnionType.java` - Union type implementation
- `FunType.java` - Function type definition
- `TypeFactory.java` - Type creation utilities

## Related

- Union types: `UnionType.unionType(name, types...)`
- Array types: `ArrayType.arrayType(innerType)` or `ArrayType.ARRAY_TYPE`
- Function types: `FunType.fun(returnType, paramTypes...)`