# Kite Language Features

## Import Statement Type Checking

Type-checks imported files at compile time, detecting errors before runtime.

**Example:**
```kite
import * from "stdlib.kite"

var result = double(5)  // 'double' function type is validated
var msg = greeting      // 'greeting' variable type is validated
```

**Features:**
- Parses and type-checks imported files
- Merges exported types (functions, variables, schemas) into current environment
- Detects circular imports at type-check time
- Validates import file paths exist
- Type-checks function calls from imported modules

**Reference:** `src/main/java/cloud/kitelang/semantics/TypeChecker.java:788-826`

**Tests:** `src/test/java/cloud/kitelang/semantics/typechecker/ImportStatementTest.java`