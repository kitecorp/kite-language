# Skill: Law of Demeter (LoD)

## When to Use

Use this skill when writing code that accesses nested objects through method chaining. Before writing `object.getX().doY()`, check if `object.doY()` exists or should be created.

## Principle

The Law of Demeter states: **"Only talk to your immediate friends"**

A method should only call methods on:
1. Its own object (`this`)
2. Objects passed as parameters
3. Objects it creates
4. Its direct component objects

**Avoid:** `a.getB().getC().doSomething()` (train wreck / method chaining)

## Pattern

### Before (Violates LoD)

```java
// Test code reaching into interpreter's internal structure
assertTrue(interpreter.getEnv().hasVar("ServerConfig"));
assertFalse(interpreter.getEnv().hasVar("nonExistent"));

// Multiple places repeating the same chain
var value = interpreter.getEnv().getVar("myVar");
interpreter.getEnv().setVar("newVar", someValue);
```

### After (Encapsulated)

```java
// Add delegate methods to Interpreter
public class Interpreter {
    private Environment env;

    // Delegate methods - encapsulate Environment access
    public boolean hasVar(String name) {
        return env.hasVar(name);
    }

    public Object getVar(String name) {
        return env.getVar(name);
    }

    public void setVar(String name, Object value) {
        env.setVar(name, value);
    }
}

// Clean test code
assertTrue(interpreter.hasVar("ServerConfig"));
assertFalse(interpreter.hasVar("nonExistent"));
var value = interpreter.getVar("myVar");
```

## Checklist

When you see method chaining (`a.getB().doC()`):

1. **Check if delegate exists**: Does `a.doC()` already exist?
2. **If not, add it**: Create a delegate method on `a` that forwards to `b.doC()`
3. **Update callers**: Replace all `a.getB().doC()` with `a.doC()`
4. **Consider visibility**: Can `getB()` be made private/package-private?

## Benefits

| Benefit | Explanation |
|---------|-------------|
| **Encapsulation** | Internal structure hidden from callers |
| **Flexibility** | Can change internal implementation without breaking callers |
| **Readability** | `interpreter.hasVar(x)` clearer than `interpreter.getEnv().hasVar(x)` |
| **Single point of change** | Logic changes in one place, not scattered |
| **Testability** | Easier to mock/stub single method than chain |

## Common Violations in This Codebase

| Violation | Fix |
|-----------|-----|
| `interpreter.getEnv().hasVar(name)` | `interpreter.hasVar(name)` |
| `interpreter.getEnv().lookup(name)` | `interpreter.getVar(name)` |
| `interpreter.getEnv().get(name)` | `interpreter.getVarOrNull(name)` |
| `interpreter.getEnv().init(name, val)` | `interpreter.initVar(name, val)` |
| `interpreter.getEnv().getResources()` | `interpreter.getInstances()` |
| `checker.getScope().lookup(name)` | `checker.lookup(name)` |

## Available Delegate Methods in Interpreter

```java
// Variable access (throws if not found)
public Object getVar(String name)

// Variable check (returns boolean)
public boolean hasVar(String name)

// Variable access (returns null if not found)
public Object getVarOrNull(String name)

// Variable initialization
public Object initVar(String name, Object value)

// Resource access
public Map<String, ResourceValue> getInstances()
```

## Exceptions

LoD can be relaxed for:
- **Data structures**: DTOs, records, value objects with no behavior
- **Fluent APIs**: Designed for chaining (`builder.setX().setY().build()`)
- **Stream operations**: `list.stream().filter().map().collect()`

## Implementation Steps

1. Identify the chain: `interpreter.getEnv().hasVar(name)`
2. Add delegate to outer class:
   ```java
   // In Interpreter.java
   public boolean hasVar(String name) {
       return env.hasVar(name);
   }
   ```
3. Find all usages of the chain (use IDE "Find Usages" on `getEnv()`)
4. Replace each chain with delegate call
5. If `getEnv()` is no longer needed externally, make it private

## Related Principles

- **Tell, Don't Ask**: Instead of asking for data, tell objects what to do
- **Information Hiding**: Hide internal structure from clients
- **Single Responsibility**: Each class handles its own concerns