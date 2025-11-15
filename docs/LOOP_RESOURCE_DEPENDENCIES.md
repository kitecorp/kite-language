# Loop Resource Dependencies

This document explains how resource dependencies work within loops, including the translation from user-friendly syntax
to fully-qualified indexed names.

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         USER CODE (Source .kite file)                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  for i in 0..2 {                                                        │
│      resource vm vpc {                                                  │
│          name = 'vpc-$i'                                                │
│      }                                                                  │
│      resource vm cidr {                                                 │
│          name = vpc.name    ← User writes simple "vpc"                  │
│      }                                                                  │
│  }                                                                      │
│                                                                         │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
                         Parse & Interpret
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    INTERPRETER (Loop Context Tracking)                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────┐             │
│  │ Loop Iteration: i = 0                                  │             │
│  ├────────────────────────────────────────────────────────┤             │
│  │ Step 1: Evaluate first resource                        │             │
│  │   resource vm vpc { name = 'vpc-0' }                   │             │
│  │   → Store as: vpc[0]                                   │             │
│  │                                                         │             │
│  │ Step 2: Evaluate second resource                       │             │
│  │   resource vm cidr { name = vpc.name }                 │             │
│  │   → Resolve "vpc" in loop context                      │             │
│  │   → vpc + [i] = vpc + [0] = vpc[0]                     │             │
│  │   → Create Dependency: vpc[0]                          │             │
│  │   → Store as: cidr[0] with dependencies: ["vpc[0]"]    │             │
│  └────────────────────────────────────────────────────────┘             │
│                             ↓                                            │
│  ┌────────────────────────────────────────────────────────┐             │
│  │ Loop Iteration: i = 1                                  │             │
│  ├────────────────────────────────────────────────────────┤             │
│  │ Step 1: Evaluate first resource                        │             │
│  │   resource vm vpc { name = 'vpc-1' }                   │             │
│  │   → Store as: vpc[1]                                   │             │
│  │                                                         │             │
│  │ Step 2: Evaluate second resource                       │             │
│  │   resource vm cidr { name = vpc.name }                 │             │
│  │   → Resolve "vpc" in loop context                      │             │
│  │   → vpc + [i] = vpc + [1] = vpc[1]                     │             │
│  │   → Create Dependency: vpc[1]                          │             │
│  │   → Store as: cidr[1] with dependencies: ["vpc[1]"]    │             │
│  └────────────────────────────────────────────────────────┘             │
│                                                                          │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
                         In-Memory Storage
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                   RESOURCE INSTANCES MAP                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  instances = {                                                           │
│      "vpc[0]": ResourceValue {                                          │
│          type: "vm",                                                     │
│          properties: { name: "vpc-0" },                                 │
│          dependencies: []                                                │
│      },                                                                  │
│      "cidr[0]": ResourceValue {                                         │
│          type: "vm",                                                     │
│          properties: { name: "vpc-0" },                                 │
│          dependencies: ["vpc[0]"]  ← Fully qualified                    │
│      },                                                                  │
│      "vpc[1]": ResourceValue {                                          │
│          type: "vm",                                                     │
│          properties: { name: "vpc-1" },                                 │
│          dependencies: []                                                │
│      },                                                                  │
│      "cidr[1]": ResourceValue {                                         │
│          type: "vm",                                                     │
│          properties: { name: "vpc-1" },                                 │
│          dependencies: ["vpc[1]"]  ← Fully qualified                    │
│      }                                                                   │
│  }                                                                       │
│                                                                          │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
                         Topology Sort
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      DEPENDENCY GRAPH                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   ┌────────┐                      ┌────────┐                            │
│   │ vpc[0] │ (no dependencies)    │ vpc[1] │ (no dependencies)          │
│   └────┬───┘                      └────┬───┘                            │
│        │                               │                                 │
│        │ referenced by                 │ referenced by                   │
│        ▼                               ▼                                 │
│   ┌─────────┐                     ┌─────────┐                           │
│   │ cidr[0] │                     │ cidr[1] │                           │
│   │  deps:  │                     │  deps:  │                           │
│   │ vpc[0]  │                     │ vpc[1]  │                           │
│   └─────────┘                     └─────────┘                           │
│                                                                          │
│  Sorted Execution Order: [vpc[0], cidr[0], vpc[1], cidr[1]]            │
│                                                                          │
└──────────────────────────────────┬──────────────────────────────────────┘
                                   │
                         Send to Engine
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        ENGINE EXECUTION                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  Step 1: Create vpc[0]                                                  │
│  ┌──────────────────────────────────────────┐                           │
│  │ Cloud API Call                           │                           │
│  │ → Create VM with name="vpc-0"            │                           │
│  │ → Response: ID="i-abc123"                │                           │
│  │                                           │                           │
│  │ Database Save:                            │                           │
│  │   resource_name: "vpc[0]"                │                           │
│  │   cloud_id: "i-abc123"                   │                           │
│  │   dependencies: []                        │                           │
│  └──────────────────────────────────────────┘                           │
│                     ↓                                                    │
│  Step 2: Create cidr[0]                                                 │
│  ┌──────────────────────────────────────────┐                           │
│  │ Dependency Check:                         │                           │
│  │ → vpc[0] exists? YES ✓                   │                           │
│  │ → Retrieve vpc[0] metadata               │                           │
│  │                                           │                           │
│  │ Cloud API Call                           │                           │
│  │ → Create VM with name="vpc-0"            │                           │
│  │ → Response: ID="i-def456"                │                           │
│  │                                           │                           │
│  │ Database Save:                            │                           │
│  │   resource_name: "cidr[0]"               │                           │
│  │   cloud_id: "i-def456"                   │                           │
│  │   dependencies: ["vpc[0]"]               │                           │
│  └──────────────────────────────────────────┘                           │
│                     ↓                                                    │
│  Step 3: Create vpc[1]                                                  │
│  ┌──────────────────────────────────────────┐                           │
│  │ Cloud API Call                           │                           │
│  │ → Create VM with name="vpc-1"            │                           │
│  │ → Response: ID="i-ghi789"                │                           │
│  │                                           │                           │
│  │ Database Save:                            │                           │
│  │   resource_name: "vpc[1]"                │                           │
│  │   cloud_id: "i-ghi789"                   │                           │
│  │   dependencies: []                        │                           │
│  └──────────────────────────────────────────┘                           │
│                     ↓                                                    │
│  Step 4: Create cidr[1]                                                 │
│  ┌──────────────────────────────────────────┐                           │
│  │ Dependency Check:                         │                           │
│  │ → vpc[1] exists? YES ✓                   │                           │
│  │ → Retrieve vpc[1] metadata               │                           │
│  │                                           │                           │
│  │ Cloud API Call                           │                           │
│  │ → Create VM with name="vpc-1"            │                           │
│  │ → Response: ID="i-jkl012"                │                           │
│  │                                           │                           │
│  │ Database Save:                            │                           │
│  │   resource_name: "cidr[1]"               │                           │
│  │   cloud_id: "i-jkl012"                   │                           │
│  │   dependencies: ["vpc[1]"]               │                           │
│  └──────────────────────────────────────────┘                           │
│                                                                          │
│  Result: All 4 resources created in correct dependency order ✓          │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Key Translation Rules

### User Code → Interpreter → Storage

| Context  | User Writes         | Interpreter Resolves | Stored As             |
|----------|---------------------|----------------------|-----------------------|
| Loop i=0 | `resource vm vpc`   | Add loop index       | `vpc[0]`              |
| Loop i=0 | `vpc.name`          | Resolve in context   | Reference to `vpc[0]` |
| Loop i=0 | `cidr` dependencies | Track reference      | `["vpc[0]"]`          |
| Loop i=1 | `resource vm vpc`   | Add loop index       | `vpc[1]`              |
| Loop i=1 | `vpc.name`          | Resolve in context   | Reference to `vpc[1]` |
| Loop i=1 | `cidr` dependencies | Track reference      | `["vpc[1]"]`          |

---

## Context Resolution Algorithm

```java
// When interpreter encounters: vpc.name inside loop

String resolveResourceReference(String resourceName, Map<String, Object> loopContext) {
    // Check if we're inside a loop
    if (loopContext.containsKey("i")) {
        Object loopIndex = loopContext.get("i");

        // Build fully qualified name
        String fullyQualifiedName = resourceName + "[" + loopIndex + "]";

        // Verify it exists (or will exist in this iteration)
        if (resourceExists(fullyQualifiedName) ||
            isBeingCreatedThisIteration(resourceName)) {
            return fullyQualifiedName;  // "vpc[0]"
        }
    }

    // Outside loop or not found
    return resourceName;  // "vpc"
}
```

---

## Dependency Tracking Example

### Input Code

```java
for i in 0..2 {
    resource vm vpc  { name = 'vpc-$i' }
    resource vm cidr { name = vpc.name }
    resource vm route { vpc_ref = cidr.vpc_ref }
}
```

### Resolved Dependencies

```
vpc[0]:   dependencies = []
cidr[0]:  dependencies = ["vpc[0]"]
route[0]: dependencies = ["cidr[0]"]  → implicit: ["vpc[0]", "cidr[0]"]

vpc[1]:   dependencies = []
cidr[1]:  dependencies = ["vpc[1]"]
route[1]: dependencies = ["cidr[1]"]  → implicit: ["vpc[1]", "cidr[1]"]
```

### Topology Sort Result

```
[vpc[0], cidr[0], route[0], vpc[1], cidr[1], route[1]]
```

### Parallel Execution Opportunity

```
Group 1 (can run in parallel):
  - vpc[0], vpc[1]

Group 2 (can run in parallel after Group 1):
  - cidr[0], cidr[1]

Group 3 (can run in parallel after Group 2):
  - route[0], route[1]
```

---

## Benefits of Indexed Dependencies

✅ **Precision**: Engine knows exactly which resource depends on which
✅ **Parallelization**: Independent resources can be created in parallel
✅ **Error Messages**: Can report "cidr[0] failed because vpc[0] doesn't exist"
✅ **State Tracking**: Database knows exact dependency chain
✅ **Debugging**: Clear execution order for troubleshooting
✅ **Rollback**: Can delete in reverse dependency order per iteration

---

## Cross-Iteration Dependencies (Not Allowed)

This would cause an error:

```java
for i in 0..2 {
    resource vm vpc { name = 'vpc-$i' }
}

for j in 0..2 {
    resource vm cidr {
        name = vpc[j].name  // ❌ ERROR: Cross-loop reference
    }
}
```

**Why?** The second loop doesn't know which `vpc` to reference without explicit indexing. The loop context only tracks
the current iteration.

**Fix:**

```java
for i in 0..2 {
    resource vm vpc  { name = 'vpc-$i' }
    resource vm cidr { name = vpc.name }  // ✓ Same iteration
}
```

---

## Database Schema Example

```sql
CREATE TABLE resources (
    id SERIAL PRIMARY KEY,
    resource_name VARCHAR(255) NOT NULL,  -- "vpc[0]", "cidr[1]"
    cloud_id VARCHAR(255),                -- "i-abc123"
    type VARCHAR(50),                     -- "vm"
    properties JSONB,                     -- {"name": "vpc-0"}
    dependencies TEXT[],                  -- ["vpc[0]"]
    created_at TIMESTAMP,
    status VARCHAR(50)                    -- "created", "failed", etc.
);

-- Example rows:
-- resource_name  | cloud_id    | dependencies
-- vpc[0]         | i-abc123    | []
-- cidr[0]        | i-def456    | ["vpc[0]"]
-- vpc[1]         | i-ghi789    | []
-- cidr[1]        | i-jkl012    | ["vpc[1]"]
```

---

## Summary

The loop resource dependency system maintains a clean separation:

- **User Code**: Simple, readable syntax (`vpc.name`)
- **Interpreter**: Context-aware resolution with loop tracking
- **Storage**: Precise, indexed names (`vpc[0]`)
- **Engine**: Exact execution order with parallel opportunities
- **Database**: Complete dependency audit trail

This design gives users simplicity while providing the runtime with the precision it needs for correct execution!