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
- Function types: `(number, string) -> boolean`
- Type aliases

**Union Type Deduplication:**
Unions normalize based on **type kind**, not literal values:

- `type Numbers = 1 | 2 | 3` → normalized to `number` (single type)
- `type Mixed = 1 | "hello" | true` → `number | string | boolean` (three types)
- Literal value constraints are interpreter/runtime concerns, not typechecker concerns
- Warning logged for duplicates (can be disabled)

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
- **Keywords allowed as property names** (e.g., `type`, `for`, `if`)

```kite
var config = {
  env: "production",
  port: 8080,
  type: "web",      // Keywords allowed as keys
  for: "production", // 'for' is a keyword but valid here
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
- Support single positional argument OR multiple named arguments
- Empty parentheses allowed: `@existing()`
- Trailing commas allowed in arguments

**Argument Rules (enforced at parse time):**

```kite
// Valid - single positional argument
@provider("aws")
@provider(["aws", "azure"])  // Array is ONE argument

// Valid - multiple NAMED arguments  
@annotation(first: "aws", second: "gcp")
@validate(regex: "^[a-z]+$", flags: ["i"])

// Invalid - multiple positional arguments (parse error)
@provider("aws", "azure")  // ❌ Use array: ["aws", "azure"]
```

**Design rationale:** Decorators are data-like (declarative metadata), not function-like. Multiple values require
explicit structure (arrays/objects) to avoid ambiguity.

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

**Object Key Validation:**
Decorator object keys must be alphanumeric (enforced at type-check time):

```kite
// Valid decorator keys
@existing({ env_stage: "prod" })      // ✅ underscore
@existing({ cloud-provider: "aws" })  // ✅ hyphen  
@tags({ Environment: "production" })  // ✅ PascalCase

// Invalid decorator keys (TypeError)
@existing({ "env stage": "prod" })    // ❌ space (not alphanumeric)
@existing({ "123start": "value" })    // ❌ starts with number
```

**Pattern:** `^[a-zA-Z][a-zA-Z0-9_-]*$` (must start with letter)

**Rationale:** Decorator keys map to cloud provider tags/labels which enforce similar restrictions. General object
literals allow any string keys, but decorator arguments require structured identifiers.

### Statement Separators

- Newlines (`\n`) **or** semicolons (`;`) separate statements
- Both are interchangeable (like Kotlin, Swift)
- Works consistently across all contexts: statements, schema properties, object properties

```kite
// Variable declarations
var x = 1; var y = 2  // Semicolons
var x = 1
var y = 2             // Newlines

// Schema properties - both work
schema User {
  string name; number age  // Semicolons
}

schema User {
  string name
  number age               // Newlines
}
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

**Examples:**

| Code                                 | Parser         | Typechecker                                    |
|--------------------------------------|----------------|------------------------------------------------|
| `type T = 1 \| 1`                    | ✅ Valid syntax | Warns about duplicate (normalizes to `number`) |
| `type T = number \| number`          | ✅ Valid syntax | Warns about duplicate                          |
| `@provider("aws", "azure")`          | ❌ Parse error  | N/A (rejected before typechecker)              |
| `@provider(["aws", "azure"])`        | ✅ Valid syntax | ✅ Valid semantics                              |
| `@existing({ "env stage": "prod" })` | ✅ Valid syntax | ❌ TypeError (non-alphanumeric key)             |
| `var number x = "string"`            | ✅ Valid syntax | ❌ TypeError (type mismatch)                    |

**Principle:** Keep parser concerns (syntax) separate from typechecker concerns (semantics) for:

- Cleaner architecture
- Better error messages
- Easier language evolution
- Clear separation of responsibilities

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

### Function Types

Kite supports first-class functions with explicit type signatures:

```kite
// Function declarations with return types
fun add(number x, number y) number {
  return x + y
}

// Higher-order functions
fun outer(number x, number y) (number) -> number {
  fun inner(number p) number {
    return p + x + y
  }
  return inner
}

// Function type aliases
type MathOp = (number, number) -> number
type Predicate = (string) -> boolean

// Lambda expressions
var double = (x: number) -> x * 2
```

**Type syntax:** `(param1Type, param2Type) -> returnType`

### Unary Operators

Kite supports both prefix and postfix increment/decrement operators:

```kite
var x = 5

// Prefix (increment/decrement before use)
++x  // x becomes 6, expression evaluates to 6
--x  // x becomes 5, expression evaluates to 5

// Postfix (increment/decrement after use)  
x++  // expression evaluates to 5, then x becomes 6
x--  // expression evaluates to 6, then x becomes 5

// Also supports unary minus and logical not
-x   // negation
!condition  // logical NOT
```

### Object Syntax

Kite provides flexible object literal syntax:

```kite
// Literal form (shorthand)
var config1 = {
  env: "production",
  port: 8080
}

// Keyword form with braces (explicit)
var config2 = object({
  env: "production",
  port: 8080
})

// Empty object - two forms
var empty1 = {}
var empty2 = object()
var empty3 = object({})

// In type declarations
type EmptyObject = object      // keyword without parens
type EmptyObject = object()    // keyword with empty parens
type EmptyObject = {}          // literal form
```

**Note:** Prefer literal `{}` syntax for values, keyword `object` for type declarations.

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
- Union type deduplication uses HashSet for O(1) lookups

## Recent Grammar Changes (January 2025)

**See:** `lang/src/main/antlr/Kite.g4` for the complete, authoritative grammar.

**Key additions:**

- **Function types:** `(param1Type, param2Type) -> returnType` syntax
- **Postfix operators:** `x++`, `x--` support
- **Flexible object syntax:** `object()`, `object({})`, and `{}` all valid
- **Decorator arguments:** Single positional OR multiple named (enforced at parse time)
- **Assignment flexibility:** Arrays/objects allowed on right-hand side
- **Keywords as object keys:** Reserved words like `type`, `for`, `if` allowed as property names
- **Consistent separators:** `statementTerminator` (`;` or `\n`) used throughout schemas and properties

**Key grammar rules to reference:**

- `functionType` - Function type syntax
- `postfixExpression` - Postfix increment/decrement
- `objectDeclaration` - Object literal forms
- `decoratorArgs` - Decorator argument rules
- `objectKey` - Allows keywords via `keyword` rule
- `schemaPropertyList` - Uses `statementTerminator` for consistency

## Code Style

- **Switch over if-else chains** for readability
- **Extract helper methods** for complex logic
- **Static constants** for repeated values
- **Explicit null checks** before operations
- **Comprehensive test coverage** for both happy and error paths

## Testing Strategy

### Parser Tests (Syntax Validation)

**Location:** `lang/src/test/java/io/kite/Frontend/Parse/`

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

### Typechecker Tests (Semantic Validation)

**Location:** `lang/src/test/java/io/kite/TypeChecker/`

**Purpose:** Verify type rules, decorator validation, and semantic correctness

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

### Integration Tests

**Location:** `lang/src/test/java/io/kite/Base/RuntimeTest.java`

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

### Test Organization

```
lang/src/test/java/io/kite/
├── Frontend/Parse/          # Parser tests (syntax)
│   ├── UnionTypeSyntaxTest.java
│   ├── DecoratorSyntaxTest.java
│   └── ...
├── TypeChecker/             # Semantic validation tests
│   ├── UnionTypeDeduplicationTest.java
│   ├── Decorators/
│   │   └── ExistingTest.java
│   └── ...
└── Base/
    └── RuntimeTest.java     # Integration test base class
```

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

## Key Design Insights

### Keyword Flexibility in Object Literals

**Keywords can be used as object property names:**

```kite
// Valid - keywords as property names
var resource = {
  type: "web",
  for: "production",
  if: true
}

// Especially useful in decorators
@tags({type: "database", env: "prod"})
```

**Why this works:**

- Context matters: `type` in `type Foo = ...` is a declaration keyword
- In object literals, `{type: "value"}`, it's clearly a property name
- Parser uses context to disambiguate
- Common in modern languages (JavaScript, Python, Rust)

**Implementation:** Grammar uses a `keyword` rule listing all reserved words, allowing them in `objectKey` positions.

### Consistent Statement Separators

**Semicolons and newlines are interchangeable everywhere:**

```kite
// Variables
var x = 1; var y = 2

// Schema properties
schema User { string name; number age }

// Resource properties
resource VM server { name = "prod"; size = "large" }
```

**Why consistency matters:**

- Developer experience: No mental context switching
- Copy-paste friendly: Code snippets work regardless of separator
- Tooling simplicity: Formatters don't need special cases
- Aligns with Kotlin/Swift philosophy

**Implementation:** Use `statementTerminator` (`NL | ';'`) consistently across all grammar rules for property/statement
lists.

### Type System Philosophy

**TypeChecker works with TYPE KINDS, not literal values:**

```kite
// All normalize to 'number' type (single type in union)
type Numbers = 1 | 2 | 3

// Normalizes to 'number | string | boolean' (three types)
type Mixed = 1 | "hello" | true
```

**Rationale:**

- Literal value constraints (`x must be 1, 2, or 3`) are runtime/interpreter concerns
- TypeChecker ensures type safety (`x must be a number`)
- Keeps type checking fast and simple
- Aligns with mainstream type systems (TypeScript, Rust, etc.)

### Decorator Design

**Decorators are data-like (declarative), not function-like:**

```kite
// ✅ Explicit structure for multiple values
@provider(["aws", "azure"])
@tags({ env: "prod", team: "platform" })

// ❌ Ambiguous - use array instead
@provider("aws", "azure")
```

**Why this matters:**

- Infrastructure metadata should be **explicit and unambiguous**
- `@provider("aws", "azure")` - is this two args or a tuple?
- Enforcing structure (arrays/objects) removes ambiguity
- Maps naturally to cloud provider APIs (tags, labels)

### Parser vs Typechecker Trade-offs

**When to enforce in parser (syntax):**

- Grammar structure: "Must use blocks for if/while"
- Token patterns: "Parentheses must match"
- Argument count: "Single positional OR multiple named"

**When to enforce in typechecker (semantics):**

- Type compatibility: "Can't assign string to number"
- Business rules: "Decorator keys must be alphanumeric"
- Duplicate detection: "Union types should deduplicate"
- Context-dependent rules: "Component initialization not allowed in definition"

**Golden rule:** If it requires **understanding the meaning** of the code (not just its structure), it belongs in the
typechecker.

---

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

**Recent Achievements (January 2025):**

- ✅ ANTLR4 migration complete (~1,700 lines manual parser → 500 lines grammar)
- ✅ Function types for higher-order functions
- ✅ Postfix operators (`x++`, `x--`)
- ✅ Union type deduplication (type-kind based)
- ✅ Flexible object syntax (`object()`, `object({})`, `{}`)
- ✅ Comprehensive decorator validation
- ✅ Parser/typechecker separation fully implemented
- ✅ Keywords allowed as object property names (e.g., `{type: "web"}`)
- ✅ Consistent statement separators (`;` or `\n`) across all contexts

**Production Readiness:**

- ✅ Parser (ANTLR4)
- ✅ Type system with function types
- ✅ Decorator system (15 decorators)
- ✅ Dependency resolution (Observer pattern, cycle detection)
- ✅ Loop resource handling
- ✅ Union type deduplication
- 🔄 LSP support (planned)
- 🔄 Cloud provider plugins (AWS in development)
- 🔄 Documentation website (planned)

**Test Coverage:**

- Parser: ~80 tests (syntax validation)
- Typechecker: ~120 tests (semantic validation)
- Integration: ~80 tests (end-to-end evaluation)