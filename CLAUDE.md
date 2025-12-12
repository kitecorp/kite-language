# Kite Language - Project Context

**Kite** is an Infrastructure as Code (IaC) language designed as an alternative to Terraform.

| | |
|---|---|
| **Company** | EchoStream SRL (Romania) |
| **Parser** | ANTLR4 |
| **Java** | 25 |
| **Status** | Core features complete |

## Development Practices

- Test-driven development (TDD)
- Clean architecture, DRY, YAGNI
- Explicit over implicit
- Parser validates syntax, typechecker validates semantics

## Project Structure

```
kite/
├── api/      # Shared interfaces
├── lang/     # Parser, typechecker, interpreter
├── engine/   # Cloud API calls, state management
├── cli/      # User commands (plan, apply, destroy)
└── plugins/  # Cloud providers (aws, files)
```

## Lang Module Structure

```
lang/src/main/java/cloud/kitelang/
├── syntax/      # Lexer, parser, AST (KiteCompiler.java, KiteASTBuilder.java)
├── semantics/   # TypeChecker, scope, decorators (15 validators)
├── execution/   # Interpreter, environment, values
├── stdlib/      # Built-in functions (88 functions)
├── analysis/    # Visitors, SyntaxPrinter
└── tool/        # Terminal theming
```

## Two-Phase Execution

```
Phase 1: Source → Lexer → Parser → AST → TypeChecker → Interpreter → ResourceValue[]
Phase 2: ResourceValue[] → Engine → Cloud APIs → Database State
```

All errors caught in Phase 1 before any cloud provisioning.

## Key Language Features

- **Resources/Components:** Infrastructure declarations with inputs/outputs
- **Schemas:** Type definitions for structured data
- **Imports:** `import * from "filepath"` with environment isolation
- **Decorators:** 15 built-in (`@existing`, `@sensitive`, `@count`, etc.)
- **String interpolation:** `"Hello $name"` and `"Sum: ${a + b}"`
- **Union types:** Normalize by type kind, alphabetically sorted
- **Function types:** `(number, string) -> boolean`

## Quick Syntax Reference

```kite
// Resource
@provisionOn(["aws"])
resource S3.Bucket photos { name = "bucket" }

// Component
component WebServer api {
  input number port = 8080
  resource VM.Instance server { size = "t2.micro" }
  output string endpoint = server.publicIp
}

// Schema
schema Config { string host; number port = 5432 }

// Types
type Status = "active" | "inactive"
type MathOp = (number, number) -> number
```

## Build Commands

```bash
./gradlew clean :lang:generateGrammarSource  # Regenerate grammar
./gradlew :lang:build                         # Build
./gradlew :lang:test                          # Run tests
./gradlew :lang:test --tests "*.testName"     # Specific test
```

## Grammar Files

- `kite-language/grammar/KiteLexer.g4` - Lexer with string interpolation modes
- `kite-language/grammar/KiteParser.g4` - Parser grammar

**IMPORTANT:** When writing Kite code, always check the grammar files for correct syntax. Kite has its own syntax - do NOT assume Terraform or HCL conventions. Key differences:

| Construct | Kite Syntax | NOT like Terraform |
|-----------|-------------|-------------------|
| Resource properties | `name = "value"` | Same |
| Object literals | `{ key: "value", other: 123 }` | Uses `:` and `,` |
| Blocks | `resource Type name { }` | Similar but different rules |

Always reference `KiteParser.g4` for authoritative syntax rules.

## Design Decisions

1. **Blocks required** for if/while (prevents dangling-else)
2. **Keywords as object keys** allowed (`{type: "web"}`)
3. **Semicolons/newlines interchangeable** everywhere
4. **Decorators are data-like** - use arrays for multiple values
5. **Union types deduplicate by kind** - `1 | 2 | 3` becomes `number`

## Interpreter Patterns

### ExecutionContext for Nested Context Detection

Use `ExecutionContext(Class)` to check if currently executing inside a specific statement type. This is preferred over adding new tracking fields.

```java
// Check if inside a component and get its name
if (ExecutionContext(ComponentStatement.class) instanceof ComponentStatement comp) {
    path.setParentPath(comp.name());
}

// Check if inside a for loop
if (ExecutionContext(ForStatement.class) instanceof ForStatement forStmt) {
    // handle loop context
}
```

**Prefer this pattern over:**
- Adding new fields to track context (e.g., `currentComponentInstanceName`)
- Overriding methods to inject context
- Using try/finally blocks to set/clear context

## Naming Conventions

- PascalCase: types, resources, components
- camelCase: variables, functions

## Documentation Index

| Document | Content |
|----------|---------|
| `docs/SYNTAX.md` | Complete language syntax reference |
| `docs/DECORATORS.md` | 15 built-in decorators |
| `docs/TESTING.md` | Testing strategy and organization |
| `docs/DEPENDENCY_RESOLUTION.md` | Observer pattern, 4-phase resolution |
| `docs/LOOP_RESOURCE_DEPENDENCIES.md` | Indexed resource names in loops |

## Dependencies

**Core:** ANTLR4 4.13.1, Hibernate 7.1, Spring Context 6.2, Jackson 2.20, Lombok, Log4j 2.24
**Test:** JUnit 5, Mockito, H2
**Build:** Gradle 9.1, Java 25

## Test Coverage

- 121 test files, ~28,675 lines
- 8500+ tests (98.3% pass rate)
- Organized: syntax/parser → semantics → execution → integration
