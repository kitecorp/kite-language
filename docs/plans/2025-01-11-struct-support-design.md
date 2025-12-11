# Struct Support Design

## Overview

Structs are nominal typed data containers for Kite. They provide type safety and code reusability while remaining distinct from schemas (resource shapes) and objects (dynamic).

## Syntax

### Block Style
```kite
struct Point {
    number x
    number y = 0
}

struct User {
    @validate(regex="^[a-z]+$")
    string username

    @sensitive
    string password

    @cloud
    string id
}
```

### Inline Style
```kite
struct Point { number x, number y = 0 }
```

### Property Rules
- Type is mandatory
- Default value is optional
- Properties without defaults are required
- All decorators work (`@validate`, `@sensitive`, `@cloud`, etc.)

## Instantiation

### Positional Constructor
```kite
var p = Point(10, 20)
```

### Named Constructor
```kite
var p = Point(x: 10, y: 20)
var p = Point(y: 5, x: 10)  // order doesn't matter
var p = Point(x: 10)        // y uses default (0)
```

### Object Literal with Type Annotation
```kite
var Point p = { x: 10, y: 20 }
```

### Validation
- Type checker ensures all required properties are provided
- Type checker validates property types match
- Missing required property = compile-time error
- Extra properties in literal = compile-time error

## Usage

### Property Access (Mutable)
```kite
var p = Point(10, 20)
var x = p.x          // read
p.x = 30             // write - structs are mutable
```

### Function Parameters
```kite
fun distance(Point a, Point b) -> number {
    return sqrt((b.x - a.x)^2 + (b.y - a.y)^2)
}

distance(Point(0, 0), Point(3, 4))           // explicit
distance({ x: 0, y: 0 }, { x: 3, y: 4 })     // auto-coerced
```

### Return Types
```kite
fun origin() -> Point {
    return Point(0, 0)
}

fun origin() -> Point {
    return { x: 0, y: 0 }  // literal auto-coerced
}
```

### Nested Structs
```kite
struct Rectangle { Point topLeft, Point bottomRight }

var r = Rectangle(Point(0, 0), Point(10, 10))
var r = Rectangle({ x: 0, y: 0 }, { x: 10, y: 10 })  // auto-coerced
r.topLeft.x = 5  // nested access works
```

## Integration with Kite Constructs

### As Input/Output Types
```kite
struct ServerConfig { string host, number port = 8080 }

component WebServer {
    input ServerConfig config
    output string endpoint = "http://${config.host}:${config.port}"
}

component WebServer api {
    config = ServerConfig("localhost", 3000)
    // or: config = { host: "localhost", port: 3000 }
}
```

### As Resource Properties
```kite
struct Tags { string env, string team }

resource EC2.Instance server {
    tags = Tags("prod", "platform")
    // or: tags = { env: "prod", team: "platform" }
}
```

### As Schema Properties
```kite
struct Metadata { string createdBy, string version }

schema aws_lambda {
    string name
    Metadata metadata
}
```

### As Variable Types
```kite
var Point p = { x: 10, y: 20 }   // typed variable
var q = Point(5, 5)              // inferred type
```

## Implementation

### Grammar Changes

**KiteLexer.g4:**
- Add `STRUCT` keyword

**KiteParser.g4:**
- Add `structDeclaration` rule (similar to schema, supports inline/block)
- Add `struct` as valid type identifier

### AST Classes

- `StructDeclaration` - holds struct name and properties
- `StructProperty` - type, name, default, decorators
- `StructInstantiation` - constructor call expression

### Type System

- Register struct types in type environment
- Struct type is nominal (Point ≠ {x: number, y: number})
- Auto-coercion from object literal when target type is known

### Interpreter

- `StructValue` - runtime representation (mutable map-like)
- Constructor creates instance, validates required fields
- Property access/assignment via dot notation

### Type Checker Additions

- Validate struct property types and decorators
- Validate constructor arguments match struct definition
- Auto-coerce object literals to struct when type is declared
