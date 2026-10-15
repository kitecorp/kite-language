# Kite Language

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-green)](LICENSE)
[![Java 25](https://img.shields.io/badge/Java-25-blue)](https://openjdk.org/projects/jdk/25/)
[![Parser: ANTLR 4.13](https://img.shields.io/badge/Parser-ANTLR%204.13-orange)](https://www.antlr.org/)
[![Docs: kitelang.cloud](https://img.shields.io/badge/Docs-kitelang.cloud-0969da)](https://kitelang.cloud/docs/)

Kite is a declarative infrastructure-as-code language: an alternative to Terraform, with its own syntax. You describe the cloud resources you want, group them into reusable components, and Kite works out the order in which to create them. This repository is the language itself: the grammar, parser, type checker and interpreter that turn a `.kite` file into a list of resources, sorted so that each resource comes after the resources it depends on.

<p align="center">
  <img src="docs/demo/kite.gif" alt="A terminal session: kite new creates a project, kite providers install downloads the aws and azure providers in parallel with one progress bar each, then kite plan and kite apply create two local files from a small Kite file, and a second kite plan reports nothing to change." width="900">
</p>

<sub>Recorded from [`docs/demo/kite.tape`](docs/demo/kite.tape) with [VHS](https://github.com/charmbracelet/vhs); re-record it with `vhs docs/demo/kite.tape`.</sub>

## A quick look

> [!NOTE]
> GitHub cannot highlight Kite yet, so the code blocks on this page are colored with GitHub's **Kotlin** highlighter, the closest match ([comparison](https://github.com/kitecorp/kite-language/issues/4)). The code itself is Kite.

```kotlin
schema Instance {
  string size
  @cloud string publicIp     // filled in by the cloud provider
}

component WebServer {
  @minValue(1) @maxValue(65535)
  input number port = 8080

  @provider("aws")
  resource Instance server {
    size = "t2.micro"
  }

  output string endpoint = "http://${server.publicIp}:$port"
}

component WebServer api {
  port = 9090
}
```

- `schema` describes a resource type. Real schemas come from providers such as AWS; this one is declared inline so the example stands alone.
- `component WebServer { ... }` declares a reusable component with an `input` and an `output`. `component WebServer api { ... }` creates an instance of it and overrides `port`.
- Decorators such as `@provider`, `@cloud` and `@minValue` attach metadata and validation to declarations.
- `$port` and `${server.publicIp}` insert values into a string.

## What is in this repository

| Path | What it holds |
|------|---------------|
| [`grammar/`](https://github.com/kitecorp/kite-grammar) | The ANTLR 4 grammar: `KiteLexer.g4` and `KiteParser.g4`. A git submodule of [kitecorp/kite-grammar](https://github.com/kitecorp/kite-grammar). |
| `src/main/java/cloud/kitelang/syntax/` | `KiteCompiler`, the parsing entry point, and the AST built from the ANTLR parse tree |
| `src/main/java/cloud/kitelang/semantics/` | `TypeChecker`, scopes, types and the checks for each decorator |
| `src/main/java/cloud/kitelang/execution/` | `Interpreter`, environments, runtime values, dependency resolution and cycle detection |
| `src/main/java/cloud/kitelang/stdlib/` | Built-in functions |
| `src/main/java/cloud/kitelang/analysis/` | Import resolution and AST visitors such as `SyntaxPrinter` |
| `src/main/java/cloud/kitelang/tool/`, `utils/` | Terminal color themes and file helpers |
| `src/test/` | JUnit 5 tests for syntax, semantics, execution, the standard library and integration |
| [`docs/`](docs/) | Design notes for contributors |

### Where it fits in Kite

Kite runs in two phases, and this repository is the first:

```
Phase 1 (this repository): source → lexer → parser → AST → type checker → interpreter → sorted resources
Phase 2 (Kite engine):     sorted resources → provider calls → cloud APIs → saved state
```

Phase 1 runs entirely in memory and makes no cloud calls, so syntax errors, type errors and dependency cycles are all reported before anything is created.

## Build and test

You need **JDK 25** already installed (the build will not download one) and **Git**. The Gradle wrapper (Gradle 9.4.1) is included.

The build depends on [kite-api](https://github.com/kitecorp/kite-api), the interfaces shared between the language and the Kite engine. [`settings.gradle`](settings.gradle) expects it in a sibling directory, `../kite-api`. Clone both side by side, with `--recurse-submodules` so the grammar is checked out too:

```bash
git clone --recurse-submodules https://github.com/kitecorp/kite-language.git
git clone https://github.com/kitecorp/kite-api.git
cd kite-language
./gradlew build
```

If you cloned without `--recurse-submodules`, run `git submodule update --init` before building.

| Task | Command |
|------|---------|
| Run all tests | `./gradlew test` |
| Run one test class | `./gradlew test --tests "cloud.kitelang.semantics.typechecker.ImportStatementTest"` |
| Run one package | `./gradlew test --tests "cloud.kitelang.semantics.typechecker.*"` |
| Regenerate only the parser | `./gradlew generateGrammarSource` |

The build regenerates the parser whenever `grammar/` changes, so after a grammar change a normal `./gradlew build` is enough.

Maintainers who work in the Kite superproject, where this repository is the `kite-language` submodule, run the same tests from the superproject root with `./gradlew :kite-language:test`.

## The language in a few examples

**Schemas and types**

```kotlin
schema DatabaseConfig {
  string host
  number port = 5432
  boolean ssl = true
}

type Status = "active" | "inactive" | "pending"
```

**Variables, functions and strings**

```kotlin
fun add(number a, number b) number {
  return a + b
}

var environment = "production"
var label = "Environment: $environment"      // $name inserts a variable
var summary = "1 + 2 = ${add(1, 2)}"         // ${...} inserts any expression
var tags = { env: environment, team: "platform" }
```

Object literals use `:` between a key and its value, while resource properties use `=`. Single-quoted strings, such as `'$environment'`, are not interpolated.

**Loops**

```kotlin
schema Bucket {
  string name
}

var environments = ["dev", "staging", "prod"]

[for env in environments]
resource Bucket logs {
  name = "logs-$env"
}

var ports = [for i in 0..3: 8080 + i]
```

`[for ...]` on the line before a resource creates one resource per item. `[for ...: ...]` builds a list.

**Imports**

```kotlin
import * from "network.kite"
import Vpc, Subnet from "network.kite"
```

<details>
<summary><b>All language features</b></summary>

| Feature | Example or detail |
|---------|-------------------|
| Resources | `resource Instance server { size = "t2.micro" }` |
| Components | Declare with `component WebServer { ... }`, create an instance with `component WebServer api { ... }` |
| Inputs and outputs | `input number port = 8080`, `output string endpoint = server.publicIp` |
| Schemas | `schema Config { string host; number port = 5432 }` |
| Structs | `struct Point { number x, number y }` |
| Type aliases and unions | `type Status = "active" \| "inactive"` |
| Function types | `(number) -> number`, used as a parameter or return type |
| Functions and lambdas | `fun add(number a, number b) number { return a + b }`, `(number y) -> y * 2` |
| Imports | `import * from "file.kite"`, `import a, b from "dir"` |
| Decorators | `@allowed`, `@cloud`, `@count`, `@dependsOn`, `@description`, `@existing`, `@maxLength`, `@maxValue`, `@minLength`, `@minValue`, `@nonEmpty`, `@provider`, `@sensitive`, `@tags`, `@unique`, `@validate` |
| String interpolation | `"Hello $name"` and `"Sum: ${a + b}"`; `'single quotes'` are not interpolated |
| Control flow | `if`/`else` and `while` (braces required), `for x in list { ... }` |
| Statement separators | A newline or `;` |
| Standard library | Built-in functions in [`stdlib/`](src/main/java/cloud/kitelang/stdlib) |

</details>

## Documentation

The user documentation lives at **[kitelang.cloud/docs](https://kitelang.cloud/docs/)**:

| Getting started | Language guide | More |
|-----------------|----------------|------|
| [Introduction](https://kitelang.cloud/docs/introduction/) | [Syntax basics](https://kitelang.cloud/docs/syntax/) | [Standard library](https://kitelang.cloud/docs/stdlib/) |
| [Install](https://kitelang.cloud/docs/install/) | [Resources](https://kitelang.cloud/docs/resources/) · [Components](https://kitelang.cloud/docs/components/) | [Architecture](https://kitelang.cloud/docs/architecture/) |
| [IDE support](https://kitelang.cloud/docs/ide-support/) | [Schemas](https://kitelang.cloud/docs/schemas/) · [Structs](https://kitelang.cloud/docs/structs/) | [Terraform bridge](https://kitelang.cloud/docs/terraform-bridge/) |
| | [Functions](https://kitelang.cloud/docs/functions/) · [Control flow](https://kitelang.cloud/docs/control-flow/) | |
| | [Strings](https://kitelang.cloud/docs/strings/) · [Imports](https://kitelang.cloud/docs/imports/) · [Decorators](https://kitelang.cloud/docs/decorators/) | |

<details>
<summary><b>Design notes in this repository</b></summary>

| Document | What it covers |
|----------|----------------|
| [`docs/SYNTAX.md`](docs/SYNTAX.md) | Syntax reference |
| [`docs/FEATURES.md`](docs/FEATURES.md) | Feature catalog with examples |
| [`docs/DECORATORS.md`](docs/DECORATORS.md) | The built-in decorators |
| [`docs/TESTING.md`](docs/TESTING.md) | Testing strategy and organization |
| [`docs/DEPENDENCY_RESOLUTION.md`](docs/DEPENDENCY_RESOLUTION.md) | How resource dependencies are resolved (observer pattern, four phases) |
| [`docs/LOOP_RESOURCE_DEPENDENCIES.md`](docs/LOOP_RESOURCE_DEPENDENCIES.md) | Indexed resource names in loops |
| [`docs/DEFERRED_RESOURCE_CREATION.md`](docs/DEFERRED_RESOURCE_CREATION.md) | Creating resources whose `@count` depends on values known only after other resources exist |

</details>

## Contributing

1. Write the test first, watch it fail, then implement. Every change needs tests.
2. Keep each check in its place: the parser checks syntax, the type checker checks meaning.
3. Use PascalCase for types, resources and components, and camelCase for variables and functions.
4. Update `docs/` when a change affects language behavior, and `docs/FEATURES.md` when you finish a feature.
5. Make sure `./gradlew test` passes, then open a pull request against [kitecorp/kite-language](https://github.com/kitecorp/kite-language).

The grammar files belong to the [kitecorp/kite-grammar](https://github.com/kitecorp/kite-grammar) submodule: commit a grammar change there first, then commit the updated `grammar` submodule pointer here.

## License

[Apache License 2.0](LICENSE)
