# Kite Decorator System

Kite has a comprehensive decorator system with 15 built-in decorators.

## Validation Decorators

| Decorator | Purpose | Example |
|-----------|---------|---------|
| `@minValue(n)` | Minimum value for numbers/arrays | `@minValue(1)` |
| `@maxValue(n)` | Maximum value for numbers/arrays | `@maxValue(100)` |
| `@minLength(n)` | Minimum length for strings/arrays | `@minLength(3)` |
| `@maxLength(n)` | Maximum length for strings/arrays | `@maxLength(255)` |
| `@nonEmpty` | Ensures strings/arrays are not empty | `@nonEmpty` |
| `@validate(regex)` | Custom validation with regex | `@validate(regex: "^[a-z]+$")` |
| `@allowed([values])` | Whitelist of allowed values | `@allowed(["dev", "prod"])` |
| `@unique` | Ensures array elements are unique | `@unique` |

## Resource Decorators

| Decorator | Purpose | Example |
|-----------|---------|---------|
| `@existing` | Reference existing cloud resources | `@existing` |
| `@sensitive` | Mark sensitive data | `@sensitive` |
| `@dependsOn([resources])` | Explicit dependency declaration | `@dependsOn(["vpc"])` |
| `@tags({key: value})` | Add cloud provider tags | `@tags({env: "prod"})` |
| `@provisionOn(["providers"])` | Target specific cloud providers | `@provisionOn(["aws"])` |

## Metadata Decorators

| Decorator              | Purpose                                         | Example                       |
|------------------------|-------------------------------------------------|-------------------------------|
| `@description("text")` | Documentation for inputs/outputs                | `@description("Port number")` |
| `@count(n)`            | Create N instances (injects `$count` 0-indexed) | `@count(3)`                   |
| `@cloud`               | property is being set by cloud provider         | `@cloud`                      |

## Syntax Rules

```kite
// Valid - single positional argument
@provider("aws")
@provider(["aws", "azure"])  // Array is ONE argument

// Valid - multiple NAMED arguments
@annotation(first: "aws", second: "gcp")
@validate(regex: "^[a-z]+$", flags: ["i"])

// Invalid - multiple positional arguments (parse error)
@provider("aws", "azure")  // Use array: ["aws", "azure"]
```

**Design rationale:** Decorators are data-like (declarative metadata), not function-like.
Multiple values require explicit structure (arrays/objects) to avoid ambiguity.

## Object Key Validation

Decorator object keys must be alphanumeric (enforced at type-check time):

```kite
// Valid decorator keys
@existing({ env_stage: "prod" })      // underscore OK
@existing({ cloud-provider: "aws" })  // hyphen OK
@tags({ Environment: "production" })  // PascalCase OK

// Invalid decorator keys (TypeError)
@existing({ "env stage": "prod" })    // space not allowed
@existing({ "123start": "value" })    // can't start with number
```

**Pattern:** `^[a-zA-Z][a-zA-Z0-9_-]*$` (must start with letter)

## Magic Variables

The `@count` decorator injects a special `count` variable (0-indexed):

```kite
@count(3)
resource vm server {
    name = "server-$count"  // "server-0", "server-1", "server-2"
}
```

## Usage Examples

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

## Implementation

- **Type-check time validation:** `DecoratorChecker` subclasses in `io.kite.semantics.decorators/`
- **Runtime evaluation:** `DecoratorInterpreter` in `io.kite.execution.decorators/`
- 15 decorator implementations total
