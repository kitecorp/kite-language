# TODO

## Completed

### Component member access restriction (Done)
- Inputs and outputs are accessible on component **instances** via `instanceName.propertyName`
- Resources are private and cannot be accessed from outside the component
- Component **types** (not instances) do not allow external property access
- Outputs are now stored in component environment for member access
- Resources inside components are created per-instance (fixed duplication issue)
- Resources inside components are namespaced with component instance name (e.g., `main.instance` instead of `instance`)
- Resource execution deferred until after input overrides are applied (resources see final input values)
- Forward reference validation: referencing non-existent resources in components now throws errors
- ResourcePath.parentPath changed from String to ResourcePath for full hierarchy storage in database
  - Enables traversing the complete scope chain via `getParentPath()`
  - Supports deeply nested components (e.g., `parent.main.child.instance.vm.server`)

### Component InputResolver integration (Done)
- Inputs in components use InputResolver chain (files, env vars, CLI) when not provided explicitly
- Uses dot notation for qualified names (e.g., `api.hostname`, `prod.region`)
- Env variables support double underscore for dots (e.g., `KITE_INPUT_API__HOSTNAME=localhost`)
- Explicit overrides in component instances take precedence over resolved values
- Same behavior as top-level inputs

## Future Work

### Add struct support
- Currently only object literals are supported but are not ideal because they are not nominal types
- Object literals are anonymous key/value pairs and difficult to deduce type from
- structs define the actual properties and types of the object
- structs can be used as inputs and outputs
- structs can be nested inside other structs
- structs can be used as return types
- structs can be used as schema properties
- structs have their own type environment (e.g., `struct Point { number x, number y = 10 }`)
- structs can be used as resource properties (e.g., `resource S3Bucket s3 { point = { x: 10, y: 20 } }`)
- structs can be used as function parameters (e.g., `fun getPoint(Point p) { return p }`
- structs use the same notation as object literals (`{ x: 10, y: 20 }`)
- structs are passed by value (e.g., `fun getPoint(Point p) { return p }` returns a copy of the struct)
- structs are implemented using java records
- structs can be used as return types (e.g., `fun getPoint() Point { return { x: 10, y: 20 } }`)
- structs are accessed using dot notation (e.g., `getPoint().x`, `getPoint().y`, `point.x`, `point.y`)
- structs can be used as function parameters (e.g., `fun getPoint(Point p) { return p.x + p.y }`)
- structs can be used as variable types (e.g., `var Point p = { x: 10, y: 20 }`). If type is missing, var is `object`