# Kite Language - Project Context

## Overview

**Kite** is an Infrastructure as Code (IaC) programming language designed as an alternative to Terraform. It addresses
limitations in existing IaC tools through innovative features while maintaining clarity and developer-friendliness.

**Company:** EchoStream SRL (Romania)  
**Developer:** kmk (Senior DevSecOps Engineer)  
**Parser:** ANTLR4 (migrated from manual recursive descent parser)

## Core Features

### Multi-Cloud Provisioning

- `@provisionOn` annotations for cloud provider targeting
- Semantic abstractions for cloud provider differences
- Unified resource definitions across AWS, Azure, and GCP

### Language Constructs

- **Resources:** Infrastructure resource declarations
- **Components:** Collections of resources with inputs/outputs
- **Schemas:** Type definitions for structured data
- **Modules:** Versioned packages
- **Decorators:** Comprehensive annotation system (`@existing`, `@sensitive`, etc.)

### Type System

- Strong typing with type inference
- Union types: `type Status = "active" | "inactive" | "pending"`
- Array types with multi-dimensional support
- Object types (literal and keyword forms)
- Type aliases

## Grammar Documentation

### Array Comprehensions (3 Forms)

Kite supports three distinct array comprehension syntaxes:

```kite
// Form 1: Compact - body inside with colon separator
[for i in 0..10: i * 2]
[for i in arr: if i > 2 { i + 1 }]

// Form 2: Block - body outside after brackets (resource-specific)
[for env in environments]
resource Bucket photos {
  name = "photos-${env}"
}

// Form 3: Standalone for loop
for i in 0..10 {
  console.log(i)
}
```

**Note:** Form 2 is special syntax for resource/component generation.

### Control Flow

#### If Statements

- **Requires blocks** (no braceless bodies to avoid dangling-else problem)
- Parentheses around condition are optional (both or neither)

```kite
// Valid
if (x > 5) { doSomething() }
if x > 5 { doSomething() }

// Invalid
if (x > 5 doSomething()  // Missing closing paren
if x) { doSomething() }  // Unmatched paren
if x > 5 doSomething()   // Missing braces
```

#### While Loops

Same rules as if statements (blocks required, parens optional).

```kite
while (condition) { body }
while condition { body }
```

### Object Literals

- Use **colons** for property assignment (JavaScript/JSON style)
- Commas **required** between properties (standard across languages except Bicep)
- Trailing commas **allowed** (modern language feature)
- Newlines allowed for formatting

```kite
var config = {
  env: "production",
  port: 8080,
  features: {
    auth: true,
    logging: false,
  },  // Trailing comma OK
}
```

### Decorators/Annotations

- Allow newlines between multiple decorators
- Support object and array arguments
- Trailing commas allowed in arguments

```kite
@existing
@sensitive
@provisionOn(["aws", "azure"])
resource Database main {
  name = "prod-db"
}

@annotation(
  regex = "^[a-z0-9-]+$",
  flags = [1, 2, 3],  // Trailing comma OK
)
component Backend api { }
```

### Statement Separators

- Newlines (`\n`) **or** semicolons (`;`) separate statements
- Both are interchangeable (like Kotlin, Swift)

```kite
var x = 1; var y = 2  // Semicolons
var x = 1
var y = 2             // Newlines
```

### Type Aliases

```kite
// Union of literal values
type Status = "active" | "inactive" | "pending"

// Union of types
type Value = number | string | null

// Array types
type NumberList = [1, 2, 3]

// Object types - two forms
type Config = object      // Keyword
type Config = { }         // Literal
type Config = object()    // Keyword with parens
```

## Key Design Decisions

### 1. **Explicit Over Implicit**

- Imports must be explicit (no implicit visibility)
- Resource bodies contain only properties
- Language constructs remain outside resource blocks

### 2. **Block Requirements for Control Flow**

- If/while statements **must use blocks** `{ }`
- Prevents dangling-else ambiguity
- Improves code clarity for critical infrastructure code
- Easier to extend (adding statements doesn't require refactoring)

**Rationale:** Infrastructure code is critical and should prioritize clarity over brevity.

### 3. **Naming Conventions**

- PascalCase for types, resources, components (aligns with Terraform, Pulumi, CloudFormation)
- camelCase for variables, functions

### 4. **Terminology**

- Use "provisioning" (not "deploying") for infrastructure creation
- Clear distinction between compile-time and runtime concepts

### 5. **Parser vs Typechecker Separation**

- Parser validates **syntax** (is it grammatically correct?)
- Typechecker validates **semantics** (does it make sense?)
- Example: `type T = 1 | 1` parses successfully, typechecker catches duplicate

## Architecture

### Parser Stack

- **Lexer/Parser:** ANTLR4 generated from `Kite.g4`
- **Package:** `io.kite.Frontend.Parser.generated`
- **AST Builder:** `KiteASTBuilder.java` (visitor pattern)
- **Error Handling:** Custom `ValidationException` with context-aware messages

### AST Structure

- **Program:** Top-level container
- **Statements:** Declarations, expressions, control flow
- **Expressions:** Literals, identifiers, binary/unary operations
- **Types:** TypeIdentifier, ArrayTypeIdentifier, UnionTypeStatement

### Testing Approach

- **Parser tests:** Verify syntax is accepted/rejected correctly
- **Typechecker tests:** Verify semantic rules (type checking, duplicates, etc.)
- **Integration tests:** End-to-end via `RuntimeTest`

## Error Messages (Context-Aware)

Custom error messages improve developer experience:

```java
// Missing closing paren
"unmatched '(' - use both '(' and ')' or neither"

// Missing brace in if statement
"if/while statements require block { }"

// Decorator array issues
"missing ']' to close decorator array argument"

// Output declaration
"output declaration requires '=' and value"
```

**Implementation:** `improveErrorMessage()` in `KiteCompiler.java`

## Common Patterns

### Resource Declaration

```kite
@provisionOn(["aws"])
resource S3.Bucket photos {
  name = "my-photos-bucket"
  region = "us-east-1"
}
```

### Component with Inputs/Outputs

```kite
component WebServer api {
  input number port = 8080
  
  resource VM.Instance server {
    size = "t2.micro"
  }
  
  output string endpoint = server.publicIp
}
```

### Schema Definition

```kite
schema DatabaseConfig {
  string host
  number port = 5432
  boolean ssl = true
}
```

### Type Aliases

```kite
type Environment = "dev" | "staging" | "prod"
type Config = {
  env: Environment,
  debug: boolean
}
```

## Known Issues / TODOs

### Completed ✅

- ANTLR4 migration from manual recursive descent parser
- Expression chaining (`a().x`, `a.b[0].c()`)
- Empty blocks and statements handling
- Object literals with trailing commas
- Decorator argument parsing
- Better error messages for common mistakes

### Pending 🔄

- **Typechecker:** Duplicate detection in union types
- **LSP Support:** Language server protocol for IDE integration
- **Documentation Website:** Complete language reference
- **Standard Library:** Cloud provider abstractions
- **Monetization:** Finalize Sustainable Use License terms

### Future Considerations 💭

- Intent DSL for higher-level semantic abstractions
- Multi-language code generation tooling
- Enhanced debugging/logging capabilities

## Grammar Location

**Main Grammar:** `lang/src/main/antlr/Kite.g4`

**Generated Files:** `lang/build/generated-src/antlr/main/io/kite/Frontend/Parser/generated/`

- `KiteLexer.java`
- `KiteParser.java`
- `KiteBaseVisitor.java`
- `KiteVisitor.java`

**AST Builder:** `lang/src/main/java/io/kite/Frontend/Parser/KiteASTBuilder.java`

## Build Commands

```bash
# Clean and regenerate grammar
./gradlew clean :lang:generateGrammarSource

# Build
./gradlew :lang:build

# Run tests
./gradlew :lang:test

# Run specific test
./gradlew :lang:test --tests "*.testName"

# Test with coverage
./gradlew :lang:test jacocoTestReport
```

## Dependencies

- **ANTLR4:** Parser generation
- **Hibernate 7.1:** Data persistence (YAML configuration)
- **Spring Context:** Configuration management
- **Jackson:** YAML parsing
- **Apache Commons Lang3:** Utilities (Range, etc.)
- **Lombok:** Boilerplate reduction
- **JUnit 5:** Testing

## Performance Notes

- Grammar is optimized with left-factoring to avoid ambiguity
- Visitor pattern (not listener) for AST construction
- Lazy evaluation using `Supplier<T>` interfaces
- Minimal dependencies for fast startup

## Testing Examples

```java
// Parser test - verify syntax
@Test
void testIfStatement() {
    var res = parse("if x > 5 { doSomething() }");
    assertNotNull(res);
}

// Typechecker test - verify semantics
@Test
void testTypeMismatch() {
    var err = assertThrows(TypeError.class, () -> 
        eval("var number x = \"string\"")
    );
    assertTrue(err.getMessage().contains("type mismatch"));
}
```

## Code Style

- **Switch over if-else chains** for readability
- **Extract helper methods** for complex logic
- **Static constants** for repeated values
- **Explicit null checks** before operations
- **Comprehensive test coverage** for both happy and error paths

## References

- Authoritative documentation sources are preferred over tutorials
- Cloud providers have fundamentally different models (don't force uniformity)
- Performance improvements come from systematic refactoring

---

**Last Updated:** November 2025  
**Parser:** ANTLR4 (migration completed)  
**Tests Passing:** 280+  
**Status:** ✅ Core language features complete, typechecker enhancements in progress