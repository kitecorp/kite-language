# Kite Language Documentation

Welcome to the Kite language implementation documentation. This directory contains detailed architectural and design
documentation for developers working on the Kite interpreter.

## Architecture Documents

### [Dependency Resolution System](DEPENDENCY_RESOLUTION.md)

Comprehensive guide to the resource dependency resolution mechanism:

- **What it covers:**
    - Observer pattern implementation for lazy dependency resolution
    - 4-phase dependency resolution process
    - Performance optimizations (batched cycle detection, counter-based re-evaluation)
    - Complete sequence diagrams and examples

- **When to read this:**
    - Understanding how `@dependsOn` decorator works
    - Debugging resource evaluation order issues
    - Optimizing resource dependency performance
    - Adding features that interact with resource dependencies

- **Key classes:**
    - `Interpreter.resolveDependencies()` - Main orchestration
    - `DeferredObservable` - Observer registry
    - `ResourceStatement.notifyDependencyResolved()` - Observer callback
    - `Deferred` - Unresolved dependency marker
    - `Dependency` - Resolved resource reference

### [Loop Resource Dependencies](LOOP_RESOURCE_DEPENDENCIES.md)

How resource dependencies work inside loops with context-aware name resolution:

- **What it covers:**
  - Translation from user code (`vpc.name`) to indexed names (`vpc[0]`)
  - Loop context tracking and dependency resolution
  - Complete flow from source code to engine execution
  - Parallel execution opportunities

- **When to read this:**
  - Understanding loop-based resource creation
  - Debugging "resource not found" errors in loops
  - Understanding how dependencies are stored for looped resources
  - Implementing features that work with resource loops

- **Key concepts:**
  - Context-aware resource resolution
  - Fully qualified indexed names
  - Dependency graph with loop iterations
  - Engine execution order

## Quick Reference

### For Common Tasks

| Task                           | Reference                                                |
|--------------------------------|----------------------------------------------------------|
| Add a new decorator            | See `Interpreter` constructor (line 115)                 |
| Understand resource evaluation | See [DEPENDENCY_RESOLUTION.md](DEPENDENCY_RESOLUTION.md) |
| Debug cycle detection          | See `CycleDetection.detect()` and dependency diagrams    |
| Add observer patterns          | See `DeferredObservable` implementation                  |

## Code Navigation

### Key Source Files

```
lang/src/main/java/io/kite/
├── Runtime/
│   ├── Interpreter.java           # Main evaluation engine
│   ├── DeferredObservable.java    # Observer registry
│   ├── CycleDetection.java        # Dependency cycle detection
│   └── Values/
│       ├── Deferred.java          # Unresolved dependency marker
│       ├── Dependency.java        # Resolved reference wrapper
│       └── DeferredObserverValue.java  # Observer interface
│
├── Frontend/Parser/Expressions/
│   └── ResourceStatement.java     # Resource AST node + observer
│
└── TypeChecker/Types/Decorators/
    └── DependsOnDecorator.java    # @dependsOn implementation
```

## Contributing Documentation

When adding new features or making significant changes:

1. **Update existing docs** - If your change affects documented behavior
2. **Add diagrams** - ASCII diagrams are preferred for terminal rendering
3. **Include examples** - Show real Kite code demonstrating the feature
4. **Link from code** - Add Javadoc `@see` references to documentation files

### Documentation Standards

- Use **GitHub-flavored Markdown**
- Include **ASCII diagrams** for architecture (they render everywhere)
- Add **code examples** with expected behavior
- Keep **performance notes** where relevant
- Reference **specific line numbers** for code locations (e.g., `Interpreter.java:690`)

## Further Reading

- **Main README**: `/README.md` - Project overview and getting started
- **Tests**: `/lang/src/test/java/io/kite/` - Examples of all language features
- **Type System**: `TypeChecker/` - Type inference and validation

---

*This documentation is maintained alongside the codebase. If you find outdated information, please update it!*