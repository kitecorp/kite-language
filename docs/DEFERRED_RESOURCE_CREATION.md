# Deferred Resource Creation

## Problem

When using `@count` with an expression that depends on a `@cloud` property, the count value is only available after the dependency is created in the cloud.

```kite
resource Subnet subnet {
    vpcId = vpc.vpcId
    cidrBlock = "10.0.0.0/24"
}

@count(length([subnet]))  // Works: length() is evaluated at plan time
resource S3Bucket bucket { ... }

@count(subnet.subnetId)   // Problem: subnetId is @cloud, not available until apply
resource S3Bucket bucket { ... }
```

## Solution: Apply-Time Resource Creation

Extend the observer pattern to support **deferred resource creation** during the apply phase.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           PLAN PHASE                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  @count(subnet.subnetId)                                                │
│       │                                                                 │
│       ▼                                                                 │
│  ┌─────────────────────┐                                                │
│  │  CountDecorator     │                                                │
│  │  execute()          │                                                │
│  └─────────┬───────────┘                                                │
│            │                                                            │
│            ▼                                                            │
│  ┌─────────────────────────────────────────┐                            │
│  │ Evaluate count expression               │                            │
│  │   → DeferredValue("subnet", "subnetId") │                            │
│  └─────────────────────────────────────────┘                            │
│            │                                                            │
│            ▼                                                            │
│  ┌─────────────────────────────────────────┐                            │
│  │ Create DeferredResourceTemplate         │                            │
│  │   - templateName: "bucket"              │                            │
│  │   - countExpression: subnet.subnetId    │                            │
│  │   - resourceStatement: ResourceStatement│                            │
│  │   - dependencies: ["subnet"]            │                            │
│  └─────────────────────────────────────────┘                            │
│            │                                                            │
│            ▼                                                            │
│  ┌─────────────────────────────────────────┐                            │
│  │ Register with Interpreter               │                            │
│  │   deferredTemplates.add(template)       │                            │
│  └─────────────────────────────────────────┘                            │
│                                                                         │
│  Plan Output:                                                           │
│    + Subnet.subnet (create)                                             │
│    ~ S3Bucket.bucket[?] (deferred - count depends on subnet.subnetId)   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                           APPLY PHASE                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  Step 1: Create subnet                                                  │
│  ┌─────────────────────────────────────────┐                            │
│  │ provider.create(subnet)                 │                            │
│  │   → returns { subnetId: "subnet-abc" }  │                            │
│  └─────────────────────────────────────────┘                            │
│            │                                                            │
│            ▼                                                            │
│  Step 2: Check for deferred templates depending on "subnet"             │
│  ┌─────────────────────────────────────────┐                            │
│  │ deferredTemplates.findByDependency      │                            │
│  │   → [bucket template]                   │                            │
│  └─────────────────────────────────────────┘                            │
│            │                                                            │
│            ▼                                                            │
│  Step 3: Re-evaluate count expression                                   │
│  ┌─────────────────────────────────────────┐                            │
│  │ evaluate(subnet.subnetId)               │                            │
│  │   → "subnet-abc" (string, not number!)  │                            │
│  │   → ERROR: @count requires a number     │                            │
│  └─────────────────────────────────────────┘                            │
│                                                                         │
│  OR (valid case):                                                       │
│  ┌─────────────────────────────────────────┐                            │
│  │ @count(length([subnet]))                │                            │
│  │ evaluate(length([subnet]))              │                            │
│  │   → 1 (number)                          │                            │
│  └─────────────────────────────────────────┘                            │
│            │                                                            │
│            ▼                                                            │
│  Step 4: Create resources dynamically                                   │
│  ┌─────────────────────────────────────────┐                            │
│  │ for i in 0..count:                      │                            │
│  │   instantiate(template, count=i)        │                            │
│  │   → bucket[0], bucket[1], ...           │                            │
│  │   provider.create(bucket[i])            │                            │
│  └─────────────────────────────────────────┘                            │
│                                                                         │
│  Apply Output:                                                          │
│    ✓ Created Subnet.subnet                                              │
│    Resolving deferred resources...                                      │
│    + S3Bucket.bucket[0] (create)                                        │
│    ✓ Created S3Bucket.bucket[0]                                         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## New Classes

### DeferredResourceTemplate

```java
package cloud.kitelang.execution.values;

/**
 * Template for resources whose count depends on @cloud properties.
 * Created during plan when @count evaluates to a DeferredValue.
 * Instantiated during apply after dependencies are created.
 */
public record DeferredResourceTemplate(
    String templateName,              // e.g., "bucket"
    Expression countExpression,       // The expression to re-evaluate
    ResourceStatement resourceStatement, // Template to instantiate
    Set<String> dependencies,         // Resources this depends on
    SchemaValue schema                // Schema for the resource type
) implements DeferredObserverValue {

    /**
     * Called when a dependency is created during apply.
     * Re-evaluates the count and creates resources if ready.
     */
    @Override
    public Object notifyDependencyResolved(ApplyContext context, String resolvedResource) {
        // Check if all dependencies are resolved
        // Re-evaluate count expression
        // Create resources dynamically
    }
}
```

### Changes to Interpreter

```java
public class Interpreter {
    // Existing
    private final DeferredObservable deferredObservable;

    // New: Track deferred resource templates
    private final List<DeferredResourceTemplate> deferredTemplates = new ArrayList<>();

    public List<DeferredResourceTemplate> getDeferredTemplates() {
        return deferredTemplates;
    }

    public void addDeferredTemplate(DeferredResourceTemplate template) {
        deferredTemplates.add(template);
    }
}
```

### Changes to CountDecorator

```java
@Override
public Object execute(AnnotationDeclaration declaration) {
    var value = declaration.getValue();
    if (!(value instanceof Expression expr)) {
        throw new RuntimeError("@count requires an expression");
    }

    var evaluated = interpreter.visit(expr);

    // Check if it's a deferred cloud property
    if (evaluated instanceof DeferredValue deferred) {
        // Create deferred template instead of actual resources
        var template = new DeferredResourceTemplate(
            getResourceName(declaration),
            expr,
            (ResourceStatement) declaration.getTarget(),
            Set.of(deferred.dependencyName()),
            getSchema(declaration)
        );
        interpreter.addDeferredTemplate(template);
        return null; // No resources created yet
    }

    // Normal case: count is known
    if (!(evaluated instanceof Number)) {
        throw new RuntimeError("@count requires a number, got " + evaluated.getClass().getSimpleName());
    }

    var count = ((Number) evaluated).intValue();
    // ... create resources as before
}
```

### Changes to ResourceManager

```java
public void apply(Plan plan) {
    Map<String, Object> createdResources = new HashMap<>();

    for (MergeResult mergeResult : plan.getMergeResults()) {
        // ... existing apply logic ...

        // After creating a resource, check for deferred templates
        if (result != null) {
            createdResources.put(result.getResourceNameString(), result.getProperties());

            // Process deferred templates that depend on this resource
            processDeferredTemplates(result.getResourceNameString(), createdResources);
        }
    }
}

private void processDeferredTemplates(String createdResource, Map<String, Object> createdResources) {
    var templates = plan.getDeferredTemplates().stream()
        .filter(t -> t.dependencies().contains(createdResource))
        .toList();

    for (var template : templates) {
        // Check if all dependencies are resolved
        if (!allDependenciesResolved(template, createdResources)) {
            continue;
        }

        // Re-evaluate count expression with resolved values
        var count = evaluateCount(template, createdResources);

        // Create resources dynamically
        for (int i = 0; i < count; i++) {
            var resource = instantiateTemplate(template, i);
            var result = provider.create(resource.getProperties());
            // Save to database, etc.
        }
    }
}
```

---

## Plan Output Changes

```
$ kite plan -e dev

Plan: 2 to add, 0 to change, 0 to destroy.

+ Subnet.subnet
    cidrBlock: "10.0.0.0/24"
    vpcId: -> (deferred)

~ S3Bucket.bucket[?]
    (count depends on subnet.subnetId - will be determined during apply)
    bucket: "my-bucket-${count}"
```

---

## Edge Cases

### 1. Nested Deferred Dependencies

```kite
resource Vpc vpc { ... }
resource Subnet subnet { vpcId = vpc.vpcId }

@count(length([subnet]))  // subnet depends on vpc
resource S3Bucket bucket { ... }
```

Solution: Topological sort ensures vpc → subnet → bucket order.

### 2. Multiple Deferred Templates

```kite
@count(subnet.count)
resource S3Bucket bucket1 { ... }

@count(subnet.count)
resource S3Bucket bucket2 { ... }
```

Solution: Both templates register dependency on "subnet", both resolved after subnet is created.

### 3. Invalid Count After Resolution

```kite
@count(subnet.subnetId)  // subnetId is a string like "subnet-abc"
resource S3Bucket bucket { ... }
```

Solution: Error during apply: "@count requires a number, got String"

---

## Implementation Plan

1. **Phase 1**: Create `DeferredResourceTemplate` class
2. **Phase 2**: Update `CountDecorator` to create templates when count is deferred
3. **Phase 3**: Update `Interpreter` to track deferred templates
4. **Phase 4**: Update `Plan` to include deferred templates
5. **Phase 5**: Update `ResourceManager.apply()` to process deferred templates
6. **Phase 6**: Update plan output formatting
7. **Phase 7**: Add tests for all edge cases
