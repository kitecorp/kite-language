# Kite Language Features

## Import Statement

Supports importing symbols from external Kite files with wildcard and named imports.

### Wildcard Import

Imports all exported symbols from a file.

**Example:**
```kite
import * from "stdlib.kite"

var result = double(5)  // 'double' function is imported
var msg = greeting      // 'greeting' variable is imported
```

### Named Import

Imports specific symbols from a file, keeping other symbols out of scope.

**Example:**
```kite
import add, PI from "math_utils.kite"

var sum = add(1, 2)     // add function is available
var area = PI * r * r   // PI constant is available
// multiply is NOT available (not imported)
```

**Features:**
- Wildcard import (`import * from "file.kite"`) - imports all symbols
- Named import (`import symbol1, symbol2 from "file.kite"`) - imports only specified symbols
- Parses and type-checks imported files at compile time
- Merges exported types (functions, variables, schemas) into current environment
- Detects circular imports at type-check time
- Validates import file paths exist
- Type-checks function calls from imported modules
- Caches parsed programs to avoid re-parsing
- Errors when importing non-existent symbols

**Reference:**
- `src/main/java/cloud/kitelang/syntax/ast/statements/ImportStatement.java`
- `src/main/java/cloud/kitelang/analysis/ImportResolver.java`

**Tests:**
- `src/test/java/cloud/kitelang/syntax/parser/ImportStatementParseTest.java`
- `src/test/java/cloud/kitelang/semantics/typechecker/ImportStatementTest.java`
- `src/test/java/cloud/kitelang/execution/ImportStatementTest.java`

## Component Declaration

Supports defining reusable component types with inputs, outputs, and resources, and creating instances with customized values.

### Component Type Declaration

Declares a component type with inputs/outputs/resources but no instance name.

**Example:**
```kite
component server {
    input string hostname
    input number port = 8080
}
```

### Component Instance

Creates an instance of a declared component type with a name.

**Example:**
```kite
component server main {
    hostname = "localhost"
    port = 3000
}

component server api {
    hostname = "api.example.com"
    port = main.port  // Reference another instance's property
}
```

### Accessing Component Properties

Component instance properties are accessed via `instanceName.propertyName`.

**Example:**
```kite
var endpoint = main.hostname  // Access property on instance
```

**Features:**
- Component type declaration (`component Type { ... }`) - defines inputs/outputs/resources
- Component instantiation (`component Type name { ... }`) - creates instance with overrides
- Instances inherit default values from type declaration
- Instance properties override declaration defaults
- **Member access rules:**
  - Inputs and outputs are accessible via `instanceName.propertyName`
  - Resources are private (cannot be accessed from outside the component)
  - Component types (not instances) do not allow property access
- **Resource namespacing:** Resources inside components are namespaced with the component instance name
  - e.g., resource `instance` in component `main` is stored as `main.instance`
  - Multiple component instances can have same-named resources without conflicts
  - Resources are created after input overrides are applied (see final input values)
- Input/output/resource names must be unique within a component (enforced by Environment)
- Components cannot be modified outside their block (throws RuntimeError)
- Supports string interpolation, computed values, and cross-instance references

**Reference:**
- `src/main/java/cloud/kitelang/syntax/ast/expressions/ComponentStatement.java`
- `src/main/java/cloud/kitelang/execution/Interpreter.java` (visit(ComponentStatement), declareComponent, initializeComponent)

**Tests:**
- `src/test/java/cloud/kitelang/execution/ComponentTest.java`

## @cloud Decorator

Marks schema properties as cloud-generated values that cannot be set by users. These properties are populated by the cloud provider after resource creation (e.g., ARNs, IDs, endpoints).

### Basic Usage

```kite
schema aws_instance {
    string name                      // User-set property
    @cloud string arn                // Cloud-generated, not importable
    @cloud(importable) string id     // Cloud-generated, can be used for import
}

resource aws_instance server {
    name = "web-server"
    // arn and id are NOT set - they come from AWS after apply
}
```

### Importable Argument

The optional `importable` argument indicates properties that can identify existing resources for import operations.

**Syntax forms:**
- `@cloud` - Cloud-generated, not importable (default)
- `@cloud(importable)` - Shorthand for importable=true
- `@cloud(importable=true)` - Explicit true
- `@cloud(importable=false)` - Same as plain @cloud

**Features:**
- Only valid on schema properties (enforced at type-check time)
- Blocks initialization in schema declarations (e.g., `@cloud string arn = "..."` is an error)
- Blocks assignment in resource declarations (e.g., `arn = "..."` in resource block is an error)
- Cloud properties are initialized to `null` and populated after apply
- Multiple properties can be marked as `@cloud(importable)`

**Reference:**
- `src/main/java/cloud/kitelang/semantics/decorators/CloudDecorator.java` (type checking)
- `src/main/java/cloud/kitelang/execution/values/SchemaValue.java` (cloud property tracking)
- `src/main/java/cloud/kitelang/execution/Interpreter.java` (runtime validation)
- `docs/DECORATORS.md` (full decorator documentation)

**Tests:**
- `src/test/java/cloud/kitelang/semantics/typechecker/SchemaTest.java` (type-check tests)
- `src/test/java/cloud/kitelang/integration/CloudDecoratorTest.java` (integration tests)