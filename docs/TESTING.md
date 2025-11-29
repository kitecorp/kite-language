# Kite Testing Strategy

This document covers testing approaches and organization for the Kite language.

## Test Organization

```
lang/src/test/java/io/kite/
├── syntax/                # Syntax tests
│   ├── parser/            # Parser tests (syntax validation)
│   │   ├── UnionTypeSyntaxTest.java
│   │   ├── DecoratorSyntaxTest.java
│   │   └── literals/
│   ├── token/             # Token/lexer tests
│   └── ast/               # AST structure tests
├── semantics/             # Semantic validation tests
│   ├── UnionTypeDeduplicationTest.java
│   ├── decorators/        # Decorator validation tests
│   └── typechecker/       # Type checking tests
├── execution/             # Runtime/execution tests
│   ├── functions/         # Built-in function tests
│   └── decorators/        # Runtime decorator tests
├── integration/           # End-to-end integration tests
└── base/
    └── RuntimeTest.java   # Integration test base class
```

## Three-Layer Testing

### 1. Parser Tests (Syntax Validation)

**Location:** `lang/src/test/java/io/kite/syntax/parser/`

**Purpose:** Verify grammar accepts/rejects syntax correctly

```java
@Test
void unionWithKeywords() {
    var res = parse("type custom = object | string | number");
    assertNotNull(res); // Just verify it parses
}

@Test
void decoratorMultiplePositionalArgs() {
    // Should reject at parse time
    assertThrows(ValidationException.class, () ->
            parse("@provider(\"aws\", \"azure\")")
    );
}
```

**Key principle:** Parser tests verify **grammar correctness**, not semantic meaning.

### 2. Typechecker Tests (Semantic Validation)

**Location:** `lang/src/test/java/io/kite/semantics/`

**Purpose:** Verify type rules, decorator validation, semantic correctness

```java
@Test
void typeMismatch() {
    assertThrows(TypeError.class, () ->
            eval("var number x = \"string\"")
    );
}

@Test
void unionDeduplication() {
    eval("type custom = 1 | 2 | 3");
    var unionType = (UnionType) checker.getEnv().lookup("custom");
    assertEquals(1, unionType.getTypes().size()); // All normalize to 'number'
}

@Test
void decoratorInvalidKey() {
    assertThrows(TypeError.class, () ->
            eval("@existing({ \"env stage\": \"prod\" })")
    );
}
```

**Key principle:** Typechecker tests verify **semantic rules** after successful parsing.

### 3. Integration Tests

**Location:** `lang/src/test/java/io/kite/base/RuntimeTest.java`

**Purpose:** End-to-end evaluation including interpreter

```java
@Test
void componentWithDependencies() {
    var result = eval("""
            component WebApp api {
                resource Server main { }
                output string url = main.endpoint
            }
            """);
    assertNotNull(result);
}
```

## Parser vs Typechecker Separation

| Code | Parser | Typechecker |
|------|--------|-------------|
| `type T = 1 \| 1` | Valid syntax | Warns (normalizes to `number`) |
| `@provider("aws", "azure")` | Parse error | N/A |
| `@provider(["aws", "azure"])` | Valid syntax | Valid semantics |
| `@existing({ "env stage": "prod" })` | Valid syntax | TypeError |
| `var number x = "string"` | Valid syntax | TypeError |

**Golden rule:** If it requires understanding the **meaning** of code (not just structure), it belongs in the typechecker.

## Running Tests

```bash
# Run all tests
./gradlew :lang:test

# Run specific test
./gradlew :lang:test --tests "*.testName"

# Test with coverage
./gradlew :lang:test jacocoTestReport
```

## Test Coverage

- **121 test files**
- **~28,675 lines of test code**
- **8500+ tests passing** (98.3% pass rate)

Breakdown:
- Parser: ~80 tests (syntax validation)
- Typechecker: ~120 tests (semantic validation)
- Integration: ~80 tests (end-to-end evaluation)
