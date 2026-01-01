# Kite Decorator System

Kite has a comprehensive decorator system with 17 built-in decorators.

## Validation Decorators

### @minValue(n)

Minimum value constraint for numbers.

| Property | Value |
|----------|-------|
| **Argument** | `number` (0 to 999999) |
| **Targets** | `input`, `output` |
| **Applies to** | `number` |

```kite
@minValue(1)
input number port = 8080
```

### @maxValue(n)

Maximum value constraint for numbers.

| Property | Value |
|----------|-------|
| **Argument** | `number` (0 to 999999) |
| **Targets** | `input`, `output` |
| **Applies to** | `number` |

```kite
@maxValue(65535)
input number port = 8080
```

### @minLength(n)

Minimum length constraint for strings and arrays.

| Property | Value |
|----------|-------|
| **Argument** | `number` (0 to 999999) |
| **Targets** | `input`, `output` |
| **Applies to** | `string`, `array` |

```kite
@minLength(3)
input string name

@minLength(1)
input string[] tags
```

### @maxLength(n)

Maximum length constraint for strings and arrays.

| Property | Value |
|----------|-------|
| **Argument** | `number` (0 to 999999) |
| **Targets** | `input`, `output` |
| **Applies to** | `string`, `array` |

```kite
@maxLength(255)
input string name

@maxLength(10)
input string[] tags
```

### @nonEmpty

Ensures strings or arrays are not empty.

| Property | Value |
|----------|-------|
| **Argument** | none |
| **Targets** | `input` |
| **Applies to** | `string`, `array` |

```kite
@nonEmpty
input string name

@nonEmpty
input string[] tags
```

### @validate(regex: "pattern")

Custom validation with regex pattern or preset.

| Property | Value |
|----------|-------|
| **Arguments** | Named: `regex: string` or `preset: string` (one required) |
| **Targets** | `input`, `output` |
| **Applies to** | `string`, `array` |

```kite
@validate(regex: "^[a-z]+$")
input string name

@validate(regex: "^[a-z0-9-]+$")
input string slug

@validate(preset: "email")
input string email
```

### @allowed([values])

Whitelist of allowed values.

| Property | Value |
|----------|-------|
| **Argument** | `array` of literals (1 to 256 elements) |
| **Targets** | `input`, `schema property` |
| **Applies to** | `string`, `number`, `array` |

```kite
@allowed(["dev", "staging", "prod"])
input string environment = "dev"

@allowed([80, 443, 8080])
input number port = 80
```

**On schema properties:**

```kite
schema ServerConfig {
    @allowed(["default", "dedicated", "host"])
    string tenancy = "default"

    @allowed(["gp2", "gp3", "io1", "io2"])
    string volumeType = "gp3"
}
```

**Provider Integration:**

When building cloud providers with the Kite Provider SDK, the `@Property(validValues=...)` annotation on Java resource classes automatically generates `@allowed` decorators in the Kite schema documentation:

```java
// Java resource class (provider)
@Property(description = "Instance tenancy",
          validValues = {"default", "dedicated", "host"})
private String tenancy = "default";
```

Generates:
```kite
// Generated .kite schema
@allowed(["default", "dedicated", "host"])
string tenancy = "default"  // Instance tenancy
```

This ensures validation constraints defined in providers are reflected in the generated schemas.

### @unique

Ensures array elements are unique.

| Property | Value |
|----------|-------|
| **Argument** | none |
| **Targets** | `input` |
| **Applies to** | `array` |

```kite
@unique
input string[] tags = ["web", "api"]
```

## Schema Decorators

### @cloud

Marks schema properties as cloud-generated. These properties are set by the cloud provider after resource creation (e.g., ARNs, IDs, endpoints) and should NOT be set by the user.

| Property | Value |
|----------|-------|
| **Argument** | optional: `importable` (boolean) |
| **Targets** | `schema property` |

**Syntax forms:**
- `@cloud` - Cloud-generated, not importable (default)
- `@cloud(importable)` - Cloud-generated, importable (shorthand for `importable=true`)
- `@cloud(importable=true)` - Same as `@cloud(importable)`
- `@cloud(importable=false)` - Same as `@cloud`

```kite
schema aws_instance {
    string name                      // User-set property
    @cloud string arn                // Cloud-generated, not importable
    @cloud(importable) string id     // Cloud-generated, can be used for import
    @cloud string publicIp           // Cloud-generated, not importable
}

resource aws_instance server {
    name = "web-server"
    // arn, id, publicIp are NOT set - they come from AWS after apply
}

// Access cloud-generated properties in outputs
output string serverArn = server.arn
```

**The `importable` argument:**
When `importable=true`, the property can be used to identify existing resources for import operations. Typically used for unique identifiers like `id`, `arn`, or resource names.

**Use cases:**
- AWS ARNs, resource IDs (often importable)
- Generated endpoints and URLs
- Public/private IPs assigned by cloud provider
- Any value only known after `apply`

## Resource Decorators

### @existing("reference")

Reference existing cloud resources by ARN, URL, or ID.

| Property | Value |
|----------|-------|
| **Argument** | `string` (ARN, URL, EC2 instance ID, KMS alias, log group) |
| **Targets** | `resource` |

**Supported formats:**
- ARN: `arn:aws:s3:::bucket-name`
- URL: `https://example.com` or `s3://bucket/key`
- EC2 Instance ID: `i-0123456789abcdef0`
- KMS Alias: `alias/my-key`
- Log Group: `/aws/lambda/my-function`
- Tags: `Environment=prod,Team=platform`

```kite
@existing("arn:aws:s3:::my-bucket")
resource S3.Bucket existing_bucket {}

@existing("i-0123456789abcdef0")
resource EC2.Instance existing_instance {}
```

### @sensitive

Mark sensitive data (passwords, secrets, API keys).

| Property | Value |
|----------|-------|
| **Argument** | none |
| **Targets** | `input`, `output` |

```kite
@sensitive
input string api_key

@sensitive
output string connection_string
```

### @dependsOn(resources)

Explicit dependency declaration between resources/components.

| Property | Value |
|----------|-------|
| **Argument** | Resource/component reference, or `array` of references |
| **Targets** | `resource`, `component` (instances only) |

```kite
resource VPC.Subnet subnet { ... }

@dependsOn(subnet)
resource EC2.Instance server { ... }

@dependsOn([vpc, subnet, security_group])
resource RDS.Instance database { ... }
```

### @tags(tags)

Add cloud provider tags to resources.

| Property | Value |
|----------|-------|
| **Argument** | `object`, `array` of strings, or `string` |
| **Targets** | `resource`, `component` (instances only) |

**Formats:**
- Object: `@tags({ Environment: "prod", Team: "platform" })`
- Array: `@tags(["Environment=prod", "Team=platform"])`
- String: `@tags("Environment=prod")`

```kite
@tags({ Environment: "prod", Team: "platform" })
resource S3.Bucket photos { name = "photos" }

@tags(["Environment=staging"])
resource EC2.Instance server { ... }
```

### @provider(providers)

Target specific cloud providers for resource provisioning.

| Property | Value |
|----------|-------|
| **Argument** | `string` or `array` of strings |
| **Targets** | `resource`, `component` (instances only) |

```kite
@provider("aws")
resource S3.Bucket photos { name = "photos" }

@provider(["aws", "azure"])
resource Storage.Bucket multi_cloud { ... }
```

## Metadata Decorators

### @description("text")

Documentation for any declaration.

| Property | Value |
|----------|-------|
| **Argument** | `string` |
| **Targets** | `resource`, `component`, `input`, `output`, `var`, `schema`, `schema property`, `fun` |

```kite
@description("The port number for the web server")
input number port = 8080

@description("Main application database")
resource RDS.Instance database { ... }

@description("User configuration schema")
schema Config { ... }
```

### @count(n)

Create N instances of a resource or component. Injects `count` variable (0-indexed).

| Property | Value |
|----------|-------|
| **Argument** | `number` |
| **Targets** | `resource`, `component` (instances only) |

```kite
@count(3)
resource EC2.Instance server {
    name = "server-$count"  // "server-0", "server-1", "server-2"
}

@count(replicas)
component WebServer api {
    input number index = count
}
```

## Syntax Rules

```kite
// Valid - single positional argument
@provider("aws")
@provider(["aws", "azure"])  // Array is ONE argument

// Valid - multiple NAMED arguments
@validate(regex: "^[a-z]+$")

// Invalid - multiple positional arguments (parse error)
@provider("aws", "azure")  // Use array: ["aws", "azure"]
```

**Design rationale:** Decorators are data-like (declarative metadata), not function-like.
Multiple values require explicit structure (arrays/objects) to avoid ambiguity.

## Object Key Validation

Decorator object keys must be alphanumeric (enforced at type-check time):

```kite
// Valid decorator keys
@tags({ env_stage: "prod" })      // underscore OK
@tags({ cloud-provider: "aws" })  // hyphen OK
@tags({ Environment: "production" })  // PascalCase OK

// Invalid decorator keys (TypeError)
@tags({ "env stage": "prod" })    // space not allowed
@tags({ "123start": "value" })    // can't start with number
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
@existing("arn:aws:rds:us-east-1:123456789012:db:prod-db")
@sensitive
@provider(["aws"])
resource Database main {
  name = "prod-db"
}

@minValue(1)
@maxValue(100)
@description("Number of instances")
input number count = 10

@validate(regex: "^[a-z0-9-]+$")
@nonEmpty
input string name
```

## Quick Reference

| Decorator | Arguments | Targets |
|-----------|-----------|---------|
| `@minValue(n)` | number | input, output |
| `@maxValue(n)` | number | input, output |
| `@minLength(n)` | number | input, output |
| `@maxLength(n)` | number | input, output |
| `@nonEmpty` | none | input |
| `@validate(regex:, preset:)` | named strings | input, output |
| `@allowed([...])` | array | input, schema property |
| `@unique` | none | input |
| `@cloud` | optional: `importable` | schema property |
| `@existing("ref")` | string | resource |
| `@sensitive` | none | input, output |
| `@dependsOn(res)` | reference(s) | resource, component |
| `@tags({...})` | object/array/string | resource, component |
| `@provider("...")` | string/array | resource, component |
| `@description("...")` | string | all declarations |
| `@count(n)` | number | resource, component |

## Implementation

- **Type-check time validation:** `DecoratorChecker` subclasses in `cloud.kitelang.semantics.decorators/`
- **Runtime evaluation:** `DecoratorInterpreter` in `cloud.kitelang.execution.decorators/`
- 17 decorator implementations total
