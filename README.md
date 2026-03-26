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
./gradlew :kite-language:build

# Run tests
./gradlew :kite-language:test

# Run a specific test
./gradlew :kite-language:test --tests "*.StructTest"

# Regenerate ANTLR grammar sources
./gradlew clean :kite-language:generateGrammarSource
```

> **Note:** This module depends on [`kite-api`](https://github.com/kitecorp/kite-api), which is included as a composite build. Make sure both submodules are initialized (see [Getting Started](#getting-started) below).

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

This repository is a **Git submodule** of the main [kite](https://github.com/kitecorp/kite) project. To set up your development environment:

1. **Clone the parent repository with submodules:**

   ```bash
   git clone --recurse-submodules git@github.com:kitecorp/kite.git
   cd kite
   ```

   If you already cloned without `--recurse-submodules`, initialize them manually:

   ```bash
   git submodule update --init --recursive
   ```

2. **Verify the setup** — both `kite-language` and `kite-api` (a build dependency) must be present:

   ```bash
   ls kite-language/   # This module
   ls kite-api/        # Required dependency
   ```

3. **Build and run tests:**

   ```bash
   ./gradlew :kite-language:test
   ```

### Working on the Submodule

When contributing to `kite-language`, keep in mind that submodules track a specific commit. After making changes:

```bash
# Work inside the submodule directory
cd kite-language

# Create a feature branch
git checkout -b feature/my-change

# Make your changes, commit, and push
git add .
git commit -m "Add my feature"
git push origin feature/my-change
```

Then open a pull request against the [`kite-language`](https://github.com/kitecorp/kite-language) repository.

> **Important:** After your changes are merged, the parent `kite` repository also needs to be updated to point to the new submodule commit.

### Development Practices

- **Test-Driven Development (TDD)** — write tests first, watch them fail, then implement
- **Clean Architecture** — DRY, KISS, SOLID principles
- **Explicit over implicit** — parser validates syntax, type checker validates semantics
- **Naming conventions** — PascalCase for types/resources/components, camelCase for variables/functions

### Running Tests

```bash
# All tests
./gradlew :kite-language:test

# Specific test class
./gradlew :kite-language:test --tests "*.StructTest"

# Tests matching a pattern
./gradlew :kite-language:test --tests "*typechecker*"
```

### Grammar Changes

If you modify the ANTLR grammar files (`grammar/KiteLexer.g4` or `grammar/KiteParser.g4`), regenerate sources before building:

```bash
./gradlew clean :kite-language:generateGrammarSource
```

### Pull Request Guidelines

1. Ensure all tests pass (`./gradlew :kite-language:test`)
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
