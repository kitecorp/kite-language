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
- Properties accessed via `instanceName.propertyName`
- Input/output/resource names must be unique within a component (enforced by Environment)
- Components cannot be modified outside their block (throws RuntimeError)
- Supports string interpolation, computed values, and cross-instance references

**Reference:**
- `src/main/java/cloud/kitelang/syntax/ast/expressions/ComponentStatement.java`
- `src/main/java/cloud/kitelang/execution/Interpreter.java` (visit(ComponentStatement), declareComponent, initializeComponent)

**Tests:**
- `src/test/java/cloud/kitelang/execution/ComponentTest.java`