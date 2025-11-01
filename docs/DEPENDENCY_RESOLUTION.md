# Resource Dependency Resolution System

## Overview

The dependency resolution system uses an **Observer Pattern** to handle resources with unresolved dependencies.
Resources that depend on others subscribe to be notified when those dependencies are ready, enabling declaration-order
independence.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Interpreter                             │
│                                                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              resolveDependencies()                         │ │
│  │                                                            │ │
│  │  Phase 1: Collect    ──────────────────────┐               │ │
│  │  collectResourceDependencies()             │               │ │
│  │    • Evaluate properties                   │               │ │
│  │    • Process @dependsOn decorators         │               │ │
│  │    • Return list of Deferred               ▼               │ │
│  │                                    ┌──────────────┐        │ │
│  │  Phase 2: Validate  ───────────────│ Dependencies │        │ │
│  │  validateNoCycles()                │   [A, B, C]  │        │ │
│  │    • Run DFS cycle detection       └──────────────┘        │ │
│  │    • Throw if cycle found                  │               │ │
│  │                                            │               │ │
│  │  Phase 3: Register  ◄──────────────────────┘               │ │
│  │  registerDeferredObservers()                               │ │
│  │    • Subscribe to DeferredObservable                       │ │
│  │    • Increment unresolvedDependencyCount                   │ │
│  │                                                            │ │
│  │  Phase 4: Notify                                           │ │
│  │  notifyDependentResources()                                │ │
│  │    • Trigger observers if fully evaluated                  │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │              DeferredObservable                            │ │
│  │                                                            │ │
│  │  Map<String, Set<DeferredObserverValue>>                   │ │
│  │  ┌──────────────────────────────────────┐                  │ │
│  │  │ "resourceA" → [resourceB, resourceC] │                  │ │
│  │  │ "resourceD" → [resourceC]            │                  │ │
│  │  └──────────────────────────────────────┘                  │ │
│  │                                                            │ │
│  │  notifyObservers(resourceName)                             │ │
│  │    • Lookup observers waiting for resourceName             │ │
│  │    • Call notifyDependencyResolved() on each               │ │
│  │    • Remove fully evaluated observers                      │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## Sequence Diagram: Resource Evaluation Flow

```
User     Interpreter    Resource(B)   DeferredObs    Resource(A)
 │            │              │              │              │
 │─evaluate─> │              │              │              │
 │            │              │              │              │
 │            │──visit(A)──> │              │              │
 │            │              │              │              │
 │            │      resolveDependencies()  │              │
 │            │              │              │              │
 │            │  Phase 1: Collect deps      │              │
 │            │    property = B.name ───────┼─────────────>│
 │            │              │              │   (Deferred) │
 │            │              │              │              │
 │            │  Phase 2: Validate cycles   │              │
 │            │    CycleDetection.detect()  │              │
 │            │              │              │              │
 │            │  Phase 3: Register observers│              │
 │            │              │─addObserver─>│              │
 │            │              │  (A, "B")    │              │
 │            │              │              │              │
 │            │          A.unresolvedCount = 1             │
 │            │              │              │              │
 │            │  Phase 4: Notify (skipped - A not ready)   │
 │            │              │              │              │
 │            │──visit(B)──────────────────────────────────>
 │            │              │              │              │
 │            │      resolveDependencies()  │              │
 │            │  (B has no dependencies)    │              │
 │            │              │              │              │
 │            │  Phase 4: Notify dependents │              │
 │            │              │◄─notifyObs── │              │
 │            │              │   ("B")      │              │
 │            │              │              │              │
 │            │              │              │──notify──────>
 │            │              │              │  DependencyResolved("B")
 │            │              │              │              │
 │            │              │              │   A.unresolvedCount--
 │            │              │              │   (now 0)
 │            │              │              │              │
 │            │              │              │   re-evaluate A
 │            │◄──visit(A)──────────────────────────────────
 │            │              │              │              │
 │            │      (A now fully evaluated)│              │
 │            │              │              │              │
 │<───done────│              │              │              │
```

---

## Resource State Diagram

```
                    ┌─────────────┐
                    │   CREATED   │
                    └──────┬──────┘
                           │
                    visit(Resource)
                           │
                           ▼
         ┌─────────────────────────────────┐
         │                                 │
    No Dependencies              Has Dependencies
         │                                 │
         ▼                                 ▼
  ┌─────────────┐                  ┌──────────────┐
  │  EVALUATED  │                  │  EVALUATING  │
  │ (complete)  │                  │ (partial)    │
  └─────┬───────┘                  └──────┬───────┘
        │                                 │
        │                      Dependencies Resolved
        │                                 │
        │                                 ▼
        │                          ┌─────────────┐
        │                          │ RE-EVALUATE │
        │                          └──────┬──────┘
        │                                 │
        │                      All deps satisfied?
        │                                 │
        │                          Yes ───┤
        │                                 │
        └─────────────────────────────────┘
                           │
                           ▼
                  ┌──────────────┐
                  │   NOTIFY     │
                  │  Observers   │
                  └──────────────┘
```

---

## Example Scenario Walkthrough

### Code Example

```java
schema vm {
string name
}

@dependsOn([first,main,third])
resource vm

second {
    name = first.name + main.name + third.name
}

resource vm

first {
    name = "A"
}

resource vm

main {
    name = "B"
}

resource vm

third {
    name = "C"
}
```

### Execution Timeline

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 1: Visit "second"                                          │
├─────────────────────────────────────────────────────────────────┤
│ Phase 1: Collect                                                │
│   - first.name  → Deferred("first")                             │
│   - main.name   → Deferred("main")                              │
│   - third.name  → Deferred("third")                             │
│                                                                 │
│ Phase 2: Validate                                               │
│   - Check cycles: [first, main, third] → No cycles ✓            │
│                                                                 │
│ Phase 3: Register                                               │
│   - Subscribe to "first"  (unresolvedCount = 1)                 │
│   - Subscribe to "main"   (unresolvedCount = 2)                 │
│   - Subscribe to "third"  (unresolvedCount = 3)                 │
│                                                                 │
│ Phase 4: Notify                                                 │
│   - second.isEvaluated = false → Skip notification              │
│                                                                 │
│ DeferredObservable State:                                       │
│   "first" → [second]                                            │
│   "main"  → [second]                                            │
│   "third" → [second]                                            │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ Step 2: Visit "first"                                           │
├─────────────────────────────────────────────────────────────────┤
│ Phase 1: Collect    → No dependencies                           │
│ Phase 2: Validate   → Nothing to validate                       │
│ Phase 3: Register   → No observers to register                  │
│ Phase 4: Notify     → first.isEvaluated = true                  │
│                                                                 │
│   notifyObservers("first")                                      │
│     → Find observers: [second]                                  │
│     → Call second.notifyDependencyResolved("first")             │
│         - second.unresolvedCount-- (3 → 2)                      │
│         - Still has unresolved deps → Skip re-evaluation        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ Step 3: Visit "main"                                            │
├─────────────────────────────────────────────────────────────────┤
│ Phase 4: Notify     → main.isEvaluated = true                   │
│                                                                 │
│   notifyObservers("main")                                       │
│     → Find observers: [second]                                  │
│     → Call second.notifyDependencyResolved("main")              │
│         - second.unresolvedCount-- (2 → 1)                      │
│         - Still has unresolved deps → Skip re-evaluation        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ Step 4: Visit "third"                                           │
├─────────────────────────────────────────────────────────────────┤
│ Phase 4: Notify     → third.isEvaluated = true                  │
│                                                                 │
│   notifyObservers("third")                                      │
│     → Find observers: [second]                                  │
│     → Call second.notifyDependencyResolved("third")             │
│         - second.unresolvedCount-- (1 → 0) ✓                    │
│         - ALL dependencies resolved!                            │
│         - Re-evaluate "second"                                  │
│                                                                 │
│   Re-evaluation of "second":                                    │
│     name = first.name + main.name + third.name                  │
│     name = "A" + "B" + "C"                                      │
│     name = "ABC" ✓                                              │
│                                                                 │
│   second.isEvaluated = true                                     │
│   notifyObservers("second") → No observers                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Performance Comparison

### Before Optimization

```
Resource with 3 dependencies (A, B, C):

Step 1: Evaluate resource
  - Property evaluation → finds Deferred(A)
  - Cycle detection (1st time)
  - Register observer for A
  - Property evaluation → finds Deferred(B)
  - Cycle detection (2nd time) ← Redundant!
  - Register observer for B
  - Property evaluation → finds Deferred(C)
  - Cycle detection (3rd time) ← Redundant!
  - Register observer for C

Step 2: A resolves → Full re-evaluation ← Expensive!
Step 3: B resolves → Full re-evaluation ← Expensive!
Step 4: C resolves → Full re-evaluation ← Expensive!

Total: 3 cycle detections + 4 full evaluations
```

### After Optimization

```
Resource with 3 dependencies (A, B, C):

Step 1: Evaluate resource
  - Collect all dependencies [A, B, C]
  - Cycle detection (1 time) ✓
  - Register observers for all 3
  - unresolvedCount = 3

Step 2: A resolves → unresolvedCount-- (skip re-eval) ✓
Step 3: B resolves → unresolvedCount-- (skip re-eval) ✓
Step 4: C resolves → unresolvedCount = 0 → Full re-evaluation ✓

Total: 1 cycle detection + 2 evaluations (initial + final)

Improvement: 3x fewer cycle detections, 2x fewer re-evaluations!
```

---

## Key Classes

### ResourceStatement (AST Node)

```java
class ResourceStatement {
    private boolean isEvaluated;           // Fully resolved?
    private boolean isEvaluating;          // Currently processing?
    private int unresolvedDependencyCount; // How many deps pending?

    void incrementUnresolvedDependencyCount()

    void decrementUnresolvedDependencyCount()

    boolean hasUnresolvedDependencies()

    // Observer interface
    Object notifyDependencyResolved(Interpreter, String resourceName)
}
```

### DeferredObservable (Observer Registry)

```java
class DeferredObservable {
    // resourceName → Set of resources waiting for it
    Map<String, Set<DeferredObserverValue>> deferredResources;

    void addObserver(DeferredObserverValue observer, Deferred dependency)

    void notifyObservers(Interpreter, String resolvedResourceName)
}
```

### Deferred (Placeholder)

```java
record Deferred(String resource) {
    // Indicates a resource reference that hasn't been evaluated yet
}
```

### Dependency (Resolved Reference)

```java
record Dependency(ResourceValue resource, Object value) {
    // Wraps a resolved resource property access
}
```

---

## Design Patterns Used

1. **Observer Pattern**: Resources observe dependencies for resolution
2. **Lazy Evaluation**: Properties only evaluated when dependencies ready
3. **Visitor Pattern**: AST traversal for interpretation
4. **Null Object Pattern**: `NullValue` for uninitialized values
5. **Builder Pattern**: ResourceStatement construction

---

## Edge Cases Handled

✅ **Cyclic Dependencies**: Detected via DFS traversal  
✅ **Forward References**: Resources can reference not-yet-declared resources  
✅ **Multiple Dependencies**: Counted and tracked individually  
✅ **No Dependencies**: Fast path with no observer registration  
✅ **Late Initialization**: `@dependsOn` can reference resources declared later  
✅ **Re-evaluation Safety**: Only happens when ALL dependencies satisfied

---

## Future Optimizations

### Potential: Property-Level Granularity

Currently, entire resources are re-evaluated. Could track which **specific properties** need deferred evaluation:

```java
class PropertyDependency {
    String propertyName;
    Set<String> dependsOn;
}
```

This would allow re-evaluating only `name = B.name` rather than the entire resource block, but adds significant
complexity.

### Trade-off Analysis

- **Current**: Simple, works well for typical resource sizes (<20 properties)
- **Granular**: Better for huge resources (>50 properties), much more complex
- **Decision**: Current approach is sufficient for 99% of use cases