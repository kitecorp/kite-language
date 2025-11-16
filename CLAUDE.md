# Kite Language - Project Context

## Overview

**Kite** is an Infrastructure as Code (IaC) programming language designed as an alternative to Terraform. It addresses
limitations in existing IaC tools through innovative features while maintaining clarity and developer-friendliness.

**Company:** EchoStream SRL (Romania)
**Developer:** kmk (Senior DevSecOps Engineer)
**Parser:** ANTLR4 (migrated from manual recursive descent parser)
**Java Version:** 25 (latest)

## Project Structure

Kite is a **multi-module Gradle project** with clear separation of concerns:

```
kite/
├── api/          # Shared interfaces and contracts
├── lang/         # Language implementation (parser, typechecker, interpreter)
├── engine/       # Execution engine (cloud API calls, state management)
├── cli/          # Command-line interface
└── plugins/      # Cloud provider implementations
    ├── aws/      # AWS provider
    └── files/    # File system provider
```

### Module Responsibilities

| Module      | Purpose                                                      | Key Components                          |
|-------------|--------------------------------------------------------------|-----------------------------------------|
| **lang**    | Parse, typecheck, and evaluate Kite source code              | Lexer, Parser, TypeChecker, Interpreter |
| **engine**  | Execute resources on cloud providers, manage state in DB     | CloudProvider interface, StateStore     |
| **cli**     | User-facing commands (plan, apply, destroy)                  | Command handlers, output formatting     |
| **api**     | Shared types and interfaces between modules                  | ResourceValue, contracts                |
| **plugins** | Provider-specific implementations (AWS SDK, Azure SDK, etc.) | AWSProvider, AzureProvider              |

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

Kite has a **comprehensive decorator system** with 15 built-in decorators:

**Validation Decorators:**

- `@minValue(n)` - Minimum value for numbers/arrays
- `@maxValue(n)` - Maximum value for numbers/arrays
- `@minLength(n)` - Minimum length for strings/arrays
- `@maxLength(n)` - Maximum length for strings/arrays
- `@nonEmpty` - Ensures strings/arrays are not empty
- `@validate(regex)` - Custom validation with regex patterns
- `@allowed([values])` - Whitelist of allowed values
- `@unique` - Ensures array elements are unique

**Resource Decorators:**

- `@existing` - Reference existing cloud resources (don't create)
- `@sensitive` - Mark sensitive data (e.g., passwords, keys)
- `@dependsOn([resources])` - Explicit dependency declaration
- `@tags({key: value})` - Add cloud provider tags
- `@provisionOn(["aws", "azure"])` - Target specific cloud providers

**Metadata Decorators:**

- `@description("text")` - Documentation for inputs/outputs
- `@count(n)` - Create N instances of a resource

**Syntax Features:**
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

@minValue(1)
@maxValue(100)
@description("Number of instances")
input number count = 10

@validate(regex: "^[a-z0-9-]+$")
input string name
```

**Implementation:** Decorators are validated at type-check time by `DecoratorChecker` subclasses in
`io.kite.TypeChecker.Types.Decorators/`

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

### Two-Phase Execution Model

Kite uses a **two-phase execution strategy** similar to Terraform (plan → apply):

```
Phase 1: EVALUATION (In-Memory, No Cloud Calls)
─────────────────────────────────────────────────
Source Code → Lexer → Parser → AST → TypeChecker → Interpreter
                                                   ↓
                                            ResourceValue[]
                                            (sorted by deps)
                                                   ↓
                                         ALL errors caught here ✓
                                         Safe to show plan ✓

Phase 2: EXECUTION (Cloud API Calls)
─────────────────────────────────────
ResourceValue[] → Engine → CloudProvider APIs → Database State
                              ↓
                    Create actual infrastructure
                    Track state for rollback
```

**Benefits:**

- ✅ Fast feedback - catch all errors before provisioning
- ✅ Safe - rollback on partial failure
- ✅ Testable - lang module works without cloud access
- ✅ Terraform-like workflow users already understand

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

### Dependency Resolution System

Kite uses an **Observer Pattern** for lazy dependency resolution:

**4-Phase Process:**

1. **Collect** - Gather all `Deferred` dependencies from property evaluation
2. **Validate** - Run DFS-based cycle detection (batched for performance)
3. **Register** - Subscribe resources as observers to `DeferredObservable`
4. **Notify** - Trigger re-evaluation when dependencies resolve

**Key Classes:**

- `DeferredObservable` - Observer registry (resourceName → Set<observers>)
- `Deferred` - Placeholder for unresolved resource reference
- `Dependency` - Resolved resource reference wrapper
- `ResourceStatement.notifyDependencyResolved()` - Observer callback

**Performance Optimizations:**

- Batched cycle detection (run once per resource, not per dependency)
- Counter-based re-evaluation (only when ALL deps satisfied)
- Prevents redundant evaluations

**Example:** Resource B depends on A → B subscribes to A → When A evaluates, B is notified → B re-evaluates with
resolved value

📖 **See:** `lang/docs/DEPENDENCY_RESOLUTION.md` for complete architecture with sequence diagrams

### Loop Resource Dependencies

Resources created in loops get **indexed names** for precise dependency tracking:

```kite
for i in 0..2 {
  resource vm vpc { name = "vpc-$i" }
  resource vm cidr { name = vpc.name }  // User writes "vpc"
}
```

**Runtime Translation:**

- User code: `vpc.name` → Runtime: `vpc[0].name`, `vpc[1].name`
- Stored as: `cidr[0]` depends on `["vpc[0]"]`, `cidr[1]` depends on `["vpc[1]"]`
- Engine knows exact execution order and can parallelize iterations

📖 **See:** `lang/docs/LOOP_RESOURCE_DEPENDENCIES.md` for complete flow diagrams

### Testing Approach

- **Parser tests:** Verify syntax is accepted/rejected correctly
- **Typechecker tests:** Verify semantic rules (type checking, duplicates, etc.)
- **Integration tests:** End-to-end via `RuntimeTest`

**Test Coverage:**

- 121 test files
- ~28,675 lines of test code
- Organized by layer: Frontend/Parse, TypeChecker, Runtime, Integration

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

## Lang Module Structure

The `lang` module is organized into clear phases of compilation:

```
lang/src/main/java/io/kite/
├── Frontend/
│   ├── Parser/
│   │   ├── generated/          # ANTLR4 generated files
│   │   │   ├── KiteLexer.java
│   │   │   ├── KiteParser.java
│   │   │   └── KiteVisitor.java
│   │   ├── KiteASTBuilder.java # Visitor → AST transformation
│   │   ├── Expressions/        # AST node classes
│   │   └── Statements/
│   └── annotations/
│       └── Annotatable.java    # Interface for decorated elements
│
├── TypeChecker/
│   ├── TypeChecker.java        # Main type validation engine
│   ├── Types/
│   │   ├── Type.java           # Type system foundation
│   │   ├── DecoratorType.java
│   │   └── Decorators/         # 15 decorator implementations
│   │       ├── MinValueDecorator.java
│   │       ├── MaxValueDecorator.java
│   │       ├── DependsOnDecorator.java
│   │       └── ...
│   └── TypeError.java
│
├── Runtime/
│   ├── Interpreter.java        # Main evaluation engine
│   ├── DeferredObservable.java # Observer pattern registry
│   ├── CycleDetection.java     # Dependency cycle detection
│   ├── Values/
│   │   ├── Deferred.java       # Unresolved dependency marker
│   │   ├── Dependency.java     # Resolved reference
│   │   └── ResourceValue.java  # Evaluated resource
│   └── Decorators/
│       └── DecoratorInterpreter.java
│
└── Visitors/
    └── SyntaxPrinter.java      # AST → source code formatting
```

## Grammar Location

**Main Grammar:** `lang/src/main/antlr/Kite.g4`

**Generated Files:** `lang/build/generated-src/antlr/main/io/kite/Frontend/Parser/generated/`
- `KiteLexer.java`
- `KiteParser.java`
- `KiteBaseVisitor.java`
- `KiteVisitor.java`

**AST Builder:** `lang/src/main/java/io/kite/Frontend/Parser/KiteASTBuilder.java`

### Compilation Pipeline

```
1. Source Code (.kite)
   ↓
2. ANTLR4 Lexer → Tokens
   ↓
3. ANTLR4 Parser → Parse Tree
   ↓
4. KiteASTBuilder → AST (Program, Statements, Expressions)
   ↓
5. TypeChecker → Type validation, decorator validation
   ↓
6. Interpreter → ResourceValue[] (evaluated, dependency-sorted)
   ↓
7. Engine Module → Cloud provisioning
```

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

### Core Dependencies

- **ANTLR4 4.13.1:** Parser generation and runtime
- **Hibernate 7.1:** ORM for state persistence
- **Spring Context 6.2:** Dependency injection and configuration
- **Jackson 2.20:** JSON/YAML parsing and serialization
- **Apache Commons Lang3 3.19:** Utilities (Range, StringUtils, etc.)
- **Lombok 1.18.42:** Boilerplate reduction (@Data, @Getter, etc.)
- **Log4j 2.24:** Logging infrastructure
- **Jansi 2.4:** ANSI color output for terminal

### Testing Dependencies

- **JUnit 5 (Jupiter) 5.13:** Testing framework
- **Mockito 3.12:** Mocking for unit tests
- **H2 Database 2.3:** In-memory database for tests

### Cloud Provider SDKs (in plugins/)

- **AWS SDK 2.28:** AWS resource provisioning
- **PostgreSQL 42.7:** State database (production)

### Build Tools

- **Gradle 9.1:** Build automation
- **Java Toolchain 25:** Latest Java language features

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

## Documentation

Kite has comprehensive internal documentation with ASCII diagrams for terminal-friendly viewing:

### Architecture Documentation (`docs/`)

**Project-Level:**

- `ARCHITECTURE.md` - Complete system architecture, module boundaries, two-phase execution
- `README.md` - Documentation index and navigation guide

**Lang Module (`lang/docs/`):**

- `DEPENDENCY_RESOLUTION.md` - Observer pattern, 4-phase resolution, sequence diagrams
- `LOOP_RESOURCE_DEPENDENCIES.md` - Context-aware name resolution, indexed dependencies
- `README.md` - Quick reference for common tasks

### Key Documentation Features

- 📊 ASCII diagrams (render everywhere, including terminals)
- 📝 Complete code examples with expected behavior
- 🔍 References to specific line numbers (e.g., `Interpreter.java:690`)
- ⚡ Performance notes where relevant
- 🔗 Javadoc `@see` references linking code to documentation

### Navigation Tips

- Use `docs/README.md` as entry point
- Follow "When to read this" sections for targeted learning
- Check "Key classes" sections for code navigation

## References

- Authoritative documentation sources are preferred over tutorials
- Cloud providers have fundamentally different models (don't force uniformity)
- Performance improvements come from systematic refactoring

---

## Project Status

**Last Updated:** January 2025
**Java Version:** 25
**Parser:** ANTLR4 (migration completed ✅)
**Test Suite:** 121 test files, ~28,675 lines of test code
**Test Status:** 280+ tests passing ✅
**Modules:** 5 (api, lang, engine, cli, plugins)
**Decorators:** 15 built-in validators and metadata annotations
**Documentation:** Comprehensive internal docs with diagrams

**Current Phase:** ✅ Core language features complete, advanced features in development

**Production Readiness:**

- ✅ Parser (ANTLR4)
- ✅ Type system
- ✅ Decorator system (15 decorators)
- ✅ Dependency resolution (Observer pattern, cycle detection)
- ✅ Loop resource handling
- 🔄 LSP support (planned)
- 🔄 Cloud provider plugins (AWS in development)
- 🔄 Documentation website (planned)