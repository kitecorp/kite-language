# Kite Language

[![Java 25](https://img.shields.io/badge/Java-25-blue)](https://openjdk.org/projects/jdk/25/)
[![ANTLR4](https://img.shields.io/badge/Parser-ANTLR4-orange)](https://www.antlr.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)
[![Gradle](https://img.shields.io/badge/Build-Gradle%209.1-02303A)](https://gradle.org/)

The language module for [Kite](https://github.com/kitecorp/kite) - an Infrastructure as Code (IaC) language designed as a modern alternative to Terraform.

This module contains the **parser**, **type checker**, **interpreter**, and **standard library** that power the Kite language.

## Overview

```kite
// import statements
import Bucket from "aws/s3"
import Instance from "aws/ec2"

// Declare a resource
resource Bucket photos { 
   name = "my-photos-bucket" 
}

// Define a reusable component
component WebServer {
  input number port = 8080
  
  resource Instance server { 
      size = "t2.micro" 
  }
  
  output string endpoint = server.publicIp
}
// instantiate the component
component WebServer server {
  port = 8080
}

// Use types and string interpolation
var name = "production"
var label = "Environment: $name"
```

## Module Structure

```
kite-language/
├── grammar/                 # ANTLR4 grammar files (KiteLexer.g4, KiteParser.g4)
├── src/
│   ├── main/java/cloud/kitelang/
│   │   ├── syntax/          # Lexer, parser, AST
│   │   ├── semantics/       # TypeChecker, scope, decorators
│   │   ├── execution/       # Interpreter, environment, values
│   │   ├── stdlib/          # 88 built-in functions
│   │   ├── analysis/        # Visitors, SyntaxPrinter
│   │   └── tool/            # Terminal theming
│   └── test/java/cloud/kitelang/
│       └── ...              # 121 test files, 8500+ tests
├── docs/                    # Architecture and design documentation
└── build.gradle
```

## Prerequisites

- **Java 25** or later
- **Gradle 9.4** (wrapper included)

## Building

```bash
# Build the module
./gradlew build

# Run tests
./gradlew test

# Run a specific test
./gradlew test --tests "*.StructTest"

# Regenerate ANTLR grammar sources
./gradlew clean generateGrammarSource
```

> **Note:** This module depends on [`kite-api`](https://github.com/kitecorp/kite-api), which must be cloned as a sibling directory. See [Getting Started](#getting-started) for setup instructions.

## Key Features

| Feature | Description |
|---------|-------------|
| **Resources & Components** | Infrastructure declarations with inputs/outputs |
| **Schemas & Structs** | Type definitions for structured data |
| **Imports** | `import * from "filepath"` with environment isolation |
| **Decorators** | 15 built-in (`@existing`, `@sensitive`, `@count`, `@cloud`, etc.) |
| **String Interpolation** | `"Hello $name"` and `"Sum: ${a + b}"` |
| **Union Types** | `type Status = "active" \| "inactive"` |
| **Function Types** | `(number, number) -> number` |
| **Standard Library** | 88 built-in functions |

## Documentation

| Document | Description |
|----------|-------------|
| [`docs/SYNTAX.md`](docs/SYNTAX.md) | Complete language syntax reference |
| [`docs/FEATURES.md`](docs/FEATURES.md) | Feature catalog with examples |
| [`docs/DECORATORS.md`](docs/DECORATORS.md) | All 15 built-in decorators |
| [`docs/TESTING.md`](docs/TESTING.md) | Testing strategy and organization |
| [`docs/DEPENDENCY_RESOLUTION.md`](docs/DEPENDENCY_RESOLUTION.md) | Observer pattern, 4-phase resolution |
| [`docs/LOOP_RESOURCE_DEPENDENCIES.md`](docs/LOOP_RESOURCE_DEPENDENCIES.md) | Indexed resource names in loops |

## How to Contribute

### Getting Started

`kite-language` is a standalone module. The only build dependency is [`kite-api`](https://github.com/kitecorp/kite-api) (shared interfaces and annotations). Clone both repos side by side:

```bash
# Clone both repos into the same parent directory
git clone git@github.com:kitecorp/kite-language.git
git clone git@github.com:kitecorp/kite-api.git
```

Your directory structure should look like:

```
parent-dir/
├── kite-language/   # This repo
└── kite-api/        # Build dependency (interfaces only)
```

> `kite-api` must be in a sibling directory (`../kite-api`) — this is configured in [`settings.gradle`](settings.gradle) as a [Gradle composite build](https://docs.gradle.org/current/userguide/composite_builds.html).

Build and verify:

```bash
cd kite-language
./gradlew build
./gradlew test
```

### Making Changes

```bash
# Create a feature branch
git checkout -b feature/my-change

# Make your changes, commit, and push
git add .
git commit -m "Add my feature"
git push origin feature/my-change
```

Then open a pull request against the [`kite-language`](https://github.com/kitecorp/kite-language) repository.

### Development Practices

- **Test-Driven Development (TDD)** — write tests first, watch them fail, then implement
- **Clean Architecture** — DRY, KISS, SOLID principles
- **Explicit over implicit** — parser validates syntax, type checker validates semantics
- **Naming conventions** — PascalCase for types/resources/components, camelCase for variables/functions

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests "*.StructTest"

# Tests matching a pattern
./gradlew test --tests "*typechecker*"
```

### Grammar Changes

If you modify the ANTLR grammar files (`grammar/KiteLexer.g4` or `grammar/KiteParser.g4`), regenerate sources before building:

```bash
./gradlew clean generateGrammarSource
```

### Pull Request Guidelines

1. Ensure all tests pass (`./gradlew test`)
2. Add tests for new features or bug fixes
3. Update documentation in `docs/` if your change affects language behavior
4. Update `docs/FEATURES.md` when completing a new feature
5. Keep commits focused and descriptive

## Architecture

Kite uses a two-phase execution model that catches all errors before any cloud provisioning happens:

```
Phase 1: Source → Lexer → Parser → AST → TypeChecker → Interpreter → ResourceValue[]
Phase 2: ResourceValue[] → Engine → Cloud APIs → Database State
```

## License

This project is licensed under the [Apache License 2.0](LICENSE).
