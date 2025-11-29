# Kite Language Syntax Reference

This document covers the complete syntax of the Kite language.

## Array Comprehensions (3 Forms)

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

## Control Flow

### If/While Statements
- **Requires blocks** (no braceless bodies - prevents dangling-else)
- Parentheses around condition are optional (both or neither)

```kite
// Valid
if (x > 5) { doSomething() }
if x > 5 { doSomething() }
while (condition) { body }
while condition { body }

// Invalid
if x > 5 doSomething()   // Missing braces
if (x > 5 doSomething()  // Missing closing paren
```

## Import Statements

```kite
import * from "filepath"
```

- **Environment Isolation:** Imported files execute in isolated environment
- **Selective Merging:** Only user-defined variables/functions imported (stdlib excluded)
- **Parent Chain Access:** Imported files can access parent scope variables

**Implementation:** `Interpreter.java:516-550`

## Object Literals

```kite
var config = {
  env: "production",
  port: 8080,
  type: "web",       // Keywords allowed as keys
  for: "production", // Reserved words valid here
  features: {
    auth: true,
  },  // Trailing comma OK
}
```

- Use **colons** for property assignment (JavaScript/JSON style)
- Commas **required** between properties
- **Keywords allowed as property names** via grammar `keyword` rule

## String Interpolation

Double-quoted strings support interpolation:

```kite
var name = "World"
var count = 42

// Short form: $identifier
var greeting = "Hello $name!"              // "Hello World!"

// Full form: ${expression}
var msg = "Count is ${count}"              // "Count is 42"
var calc = "Double: ${count * 2}"          // "Double: 84"
```

| Syntax | Description | Example |
|--------|-------------|---------|
| `$identifier` | Simple variable reference | `"Hello $name"` |
| `${expression}` | Any expression | `"Sum: ${a + b}"` |
| `\$` | Escaped dollar (literal) | `"Price: \$100"` |
| `'...'` | No interpolation | `'$name stays literal'` |

**Implementation:** Lexer modes (`STRING_MODE`) with brace counting in `KiteLexer.g4`

## Statement Separators

Newlines (`\n`) **or** semicolons (`;`) are interchangeable everywhere:

```kite
var x = 1; var y = 2  // Semicolons
var x = 1
var y = 2             // Newlines

schema User { string name; number age }  // Both work
```

## Type Aliases

```kite
type Status = "active" | "inactive" | "pending"  // Union of literals
type Value = number | string | null              // Union of types
type NumberList = [1, 2, 3]                      // Array type
type Config = object                             // Object keyword
type Config = {}                                 // Object literal
```

## Function Types

```kite
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

## Unary Operators

```kite
var x = 5
++x  // Prefix: increment first, then evaluate (6)
x++  // Postfix: evaluate first, then increment (5, then x=6)
-x   // Negation
!condition  // Logical NOT
```

## Resource Declaration

```kite
@provisionOn(["aws"])
resource S3.Bucket photos {
  name = "my-photos-bucket"
  region = "us-east-1"
}
```

## Component with Inputs/Outputs

```kite
component WebServer api {
  input number port = 8080

  resource VM.Instance server {
    size = "t2.micro"
  }

  output string endpoint = server.publicIp
}
```

## Schema Definition

```kite
schema DatabaseConfig {
  string host
  number port = 5432
  boolean ssl = true
}
```

## Grammar Files

- **Lexer:** `lang/src/main/antlr/KiteLexer.g4`
- **Parser:** `lang/src/main/antlr/KiteParser.g4`
- **AST Builder:** `lang/src/main/java/io/kite/syntax/parser/KiteASTBuilder.java`

Key grammar rules:
- `functionType` - Function type syntax
- `postfixExpression` - Postfix increment/decrement
- `objectDeclaration` - Object literal forms
- `decoratorArgs` - Decorator argument rules
- `stringPart` - String interpolation
