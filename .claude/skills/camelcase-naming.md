# Skill: camelCase Naming Convention

## When to Use

Use this skill when writing Kite language code (`.kite` files), writing tests with Kite code, or creating examples. **Always use camelCase for identifiers, never snake_case.**

## Design Decision

Kite uses **camelCase** as its standard naming convention:

| Element | Convention | Example |
|---------|------------|---------|
| Variables | camelCase | `userName`, `maxRetries`, `isEnabled` |
| Functions | camelCase | `getUserById`, `calculateTotal`, `formatDate` |
| Inputs | camelCase | `input string apiKey`, `input number maxCount` |
| Outputs | camelCase | `output string connectionUrl` |
| Struct properties | camelCase | `struct Point { number xCoord, number yCoord }` |
| Schema fields | camelCase | `schema Config { string hostName; number portNumber }` |
| Resources (instances) | camelCase | `resource S3.Bucket photoBucket { }` |
| Components (instances) | camelCase | `component WebServer apiServer { }` |

**PascalCase** is used for:
| Element | Convention | Example |
|---------|------------|---------|
| Types | PascalCase | `type UserRole = "admin" \| "user"` |
| Resource types | PascalCase | `S3.Bucket`, `VM.Instance` |
| Component definitions | PascalCase | `component WebServer { }` |
| Schema definitions | PascalCase | `schema ServerConfig { }` |
| Struct definitions | PascalCase | `struct Point { }` |

## Pattern

### Correct (camelCase)

```kite
// Variables
var userName = "john"
var maxRetryCount = 3
var isAuthenticated = true

// Struct with camelCase properties
struct UserProfile {
    string firstName
    string lastName
    string emailAddress
    number accountAge = 0
}

// Schema with camelCase fields
schema DatabaseConfig {
    string hostName
    number portNumber = 5432
    string databaseName
    boolean sslEnabled = true
}

// Component with camelCase inputs/outputs
component ApiGateway {
    input string apiEndpoint
    input number requestTimeout = 30
    input boolean enableCaching = false

    output string baseUrl = "https://${apiEndpoint}"
    output number effectiveTimeout = requestTimeout * 1000
}

// Resource with camelCase instance name
resource S3.Bucket storageBucket {
    bucketName = "my-storage"
    enableVersioning = true
}

// Function with camelCase name and parameters
fun calculateTotalPrice(number basePrice, number taxRate) {
    return basePrice * (1 + taxRate)
}
```

### Incorrect (snake_case) - DO NOT USE

```kite
// WRONG - snake_case
var user_name = "john"           // Should be: userName
var max_retry_count = 3          // Should be: maxRetryCount
var is_authenticated = true      // Should be: isAuthenticated

struct User_Profile {            // Should be: UserProfile
    string first_name            // Should be: firstName
    string last_name             // Should be: lastName
    string email_address         // Should be: emailAddress
}

schema Database_Config {         // Should be: DatabaseConfig
    string host_name             // Should be: hostName
    number port_number           // Should be: portNumber
}

component Api_Gateway {          // Should be: ApiGateway
    input string api_endpoint    // Should be: apiEndpoint
    input number request_timeout // Should be: requestTimeout
}

resource S3.Bucket storage_bucket {  // Should be: storageBucket
    bucket_name = "my-storage"       // Should be: bucketName
}

fun calculate_total_price(number base_price, number tax_rate) {  // Wrong
    return base_price * (1 + tax_rate)
}
```

## Checklist

When writing Kite code:

1. **Variables**: Use `camelCase` - `userName`, not `user_name`
2. **Functions**: Use `camelCase` - `getUserData`, not `get_user_data`
3. **Struct properties**: Use `camelCase` - `firstName`, not `first_name`
4. **Schema fields**: Use `camelCase` - `portNumber`, not `port_number`
5. **Inputs/Outputs**: Use `camelCase` - `apiKey`, not `api_key`
6. **Instance names**: Use `camelCase` - `webServer`, not `web_server`
7. **Type definitions**: Use `PascalCase` - `UserRole`, not `user_role`

## Rationale

1. **Consistency with Java ecosystem**: Kite is built in Java, and camelCase aligns with Java conventions
2. **Readability**: camelCase is more compact than snake_case while remaining readable
3. **Modern language convention**: TypeScript, JavaScript, Kotlin, Swift all use camelCase
4. **Clear type/instance distinction**: PascalCase for types, camelCase for instances

## Common Mistakes

| Mistake | Correction |
|---------|------------|
| `api_key` | `apiKey` |
| `user_id` | `userId` |
| `max_connections` | `maxConnections` |
| `is_enabled` | `isEnabled` |
| `get_user_by_id` | `getUserById` |
| `connection_string` | `connectionString` |
| `port_number` | `portNumber` |
| `file_path` | `filePath` |

## Acronyms

For acronyms, treat them as words:

| Acronym | camelCase | PascalCase |
|---------|-----------|------------|
| API | `apiKey`, `restApi` | `ApiGateway` |
| URL | `baseUrl`, `urlPath` | `UrlParser` |
| HTTP | `httpClient` | `HttpServer` |
| ID | `userId`, `recordId` | `IdGenerator` |
| SQL | `sqlQuery` | `SqlDatabase` |

## Related

- See `CLAUDE.md` section "Naming Conventions" for summary
- PascalCase for type definitions (schemas, structs, components, resources)
- camelCase for everything else (variables, properties, functions, instances)