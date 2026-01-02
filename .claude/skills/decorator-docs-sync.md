# Skill: Keeping Decorator Documentation in Sync

## When to Use

Use this skill when modifying decorator classes in `kite-language/`. The documentation in `docs/DECORATORS.md` must stay in sync with the implementation.

## Architecture

```
DecoratorChecker (type-check)              DECORATORS.md (documentation)
─────────────────────────────────          ────────────────────────────────
AllowedDecorator.java                      ### @allowed([values])
  - NAME = "allowed"                       | Property | Value |
  - targets: INPUT, SCHEMA_PROPERTY        | **Targets** | `input`, `schema property` |
  - applies to: string, number, array      | **Applies to** | `string`, `number`, `array` |
                                           ```kite
DecoratorInterpreter (runtime)             @allowed(["dev", "prod"])
─────────────────────────────────          input string env
AllowedDecorator.java                      ```
  - execute(): validates values
```

## File Locations

| File | Location | Purpose |
|------|----------|---------|
| `DECORATORS.md` | `kite-language/docs/` | User-facing documentation |
| `*Decorator.java` (checker) | `kite-language/src/main/java/.../semantics/decorators/` | Type-check validation |
| `*Decorator.java` (interpreter) | `kite-language/src/main/java/.../execution/decorators/` | Runtime execution |
| `*Test.java` | `kite-language/src/test/java/.../decorators/` | Test coverage |

## Sync Checklist

When modifying a decorator:

### 1. Update the Checker (semantics)
```java
// AllowedDecorator.java in semantics/decorators/
public AllowedDecorator(TypeChecker checker) {
    super(checker, NAME, decorator(
        List.of(ArrayType.ARRAY_TYPE, ...),           // Argument types
        Set.of(DecoratorType.Target.INPUT,            // Targets
               DecoratorType.Target.SCHEMA_PROPERTY)),
        Set.of(SystemType.STRING, SystemType.NUMBER)); // Applies to types
}
```

### 2. Update the Interpreter (execution)
```java
// AllowedDecorator.java in execution/decorators/
@Override
public Object execute(AnnotationDeclaration declaration) {
    // Handle new targets
    var target = declaration.getTarget();
    return switch (target) {
        case InputDeclaration input -> ...
        case SchemaProperty property -> ...  // NEW
        default -> throw new IllegalStateException(...);
    };
}
```

### 3. Add Tests
```java
// AllowedTest.java
@Test
@DisplayName("@allowed on schema property")
void schemaPropertyAllowedStrings() {
    eval("""
        schema Config {
            @allowed(["dev", "prod"])
            string environment = "dev"
        }
        """);
}
```

### 4. Update DECORATORS.md

Update the decorator section with:

| Section | What to Update |
|---------|----------------|
| Property table | `**Targets**`, `**Applies to**`, `**Argument**` |
| Code examples | Add examples for new targets/use cases |
| Quick Reference table | Update targets column |

**Example update for @allowed:**
```markdown
### @allowed([values])

| Property | Value |
|----------|-------|
| **Argument** | `array` of literals (1 to 256 elements) |
| **Targets** | `input`, `schema property` |        <!-- UPDATED -->
| **Applies to** | `string`, `number`, `array` |

**On schema properties:**                            <!-- NEW SECTION -->

```kite
schema ServerConfig {
    @allowed(["default", "dedicated"])
    string tenancy = "default"
}
```
```

### 5. Update Quick Reference Table

At the bottom of DECORATORS.md:
```markdown
| Decorator | Arguments | Targets |
|-----------|-----------|---------|
| `@allowed([...])` | array | input, schema property |  <!-- UPDATED -->
```

### 6. Verify Build and Tests
```bash
./gradlew :kite-language:test --tests "*AllowedTest*"
./gradlew :kite-language:build
```

## Provider SDK Integration

When `@allowed` or `@cloud` decorators change, also update:

| File | Location | What to Update |
|------|----------|----------------|
| `KiteSchemaGenerator.java` | `kite-provider-sdk/src/main/java/.../docgen/` | Schema generation for `@allowed` |
| `README.md` | `kite-provider-sdk/` | `@Property(validValues=...)` documentation |

The `@Property(validValues=...)` annotation maps to `@allowed` in generated schemas:

```java
// Java (provider)
@Property(validValues = {"a", "b", "c"})
private String field;

// Generated Kite schema
@allowed(["a", "b", "c"])
string field
```

## Common Mistakes

- Adding new target to checker but not interpreter (runtime crash)
- Adding target to interpreter but not checker (type-check won't validate)
- Updating code but forgetting DECORATORS.md (docs out of sync)
- Forgetting to update Quick Reference table
- Not adding test cases for new functionality
- Forgetting Provider SDK integration for `@allowed`/`@cloud` changes

## Decorator Target Reference

| Target | DecoratorType.Target | Where Used |
|--------|---------------------|------------|
| `input` | `INPUT` | `input string x` |
| `output` | `OUTPUT` | `output string x` |
| `resource` | `RESOURCE` | `resource Type name {}` |
| `component` | `COMPONENT` | `component Type name {}` |
| `schema` | `SCHEMA` | `schema Name {}` |
| `schema property` | `SCHEMA_PROPERTY` | `schema { string x }` |
| `var` | `VAR` | `var x = ...` |
| `fun` | `FUN` | `fun name() {}` |
