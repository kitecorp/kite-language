package cloud.kitelang.execution;

import cloud.kitelang.analysis.ImportResolver;
import cloud.kitelang.api.ProviderSchemaLookup;
import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for provider imports (e.g., import Vpc from "aws/networking").
 * These tests use a mock ProviderSchemaLookup to simulate provider schemas.
 */
@DisplayName("Provider Imports")
class ProviderImportTest extends RuntimeTest {

    private MockProviderSchemaLookup mockLookup;

    @BeforeEach
    void setUp() {
        ImportResolver.clearCache();
        mockLookup = new MockProviderSchemaLookup();
        ImportResolver.setSchemaLookup(mockLookup);
    }

    @AfterEach
    void tearDown() {
        ImportResolver.setSchemaLookup(null);
    }

    @Test
    @DisplayName("should import specific type from provider domain")
    void importSpecificTypeFromDomain() {
        // Register a schema in the mock lookup
        mockLookup.registerSchema("aws", "networking", "Vpc", """
                schema Vpc {
                    string cidrBlock
                    @cloud string vpcId
                }
                """);

        eval("""
                import Vpc from "aws/networking"

                resource Vpc myVpc {
                    cidrBlock = "10.0.0.0/16"
                }
                """);

        assertTrue(interpreter.hasVar("Vpc"), "Vpc schema should be imported");
        assertTrue(interpreter.hasVar("myVpc"), "myVpc resource should be created");
    }

    @Test
    @DisplayName("should import multiple types from provider domain")
    void importMultipleTypesFromDomain() {
        mockLookup.registerSchema("aws", "networking", "Vpc", """
                schema Vpc {
                    string cidrBlock
                }
                """);
        mockLookup.registerSchema("aws", "networking", "Subnet", """
                schema Subnet {
                    string cidrBlock
                    string vpcId
                }
                """);

        eval("""
                import Vpc, Subnet from "aws/networking"

                resource Vpc myVpc {
                    cidrBlock = "10.0.0.0/16"
                }

                resource Subnet mySubnet {
                    cidrBlock = "10.0.1.0/24"
                    vpcId = "vpc-123"
                }
                """);

        assertTrue(interpreter.hasVar("Vpc"), "Vpc schema should be imported");
        assertTrue(interpreter.hasVar("Subnet"), "Subnet schema should be imported");
        assertTrue(interpreter.hasVar("myVpc"), "myVpc resource should be created");
        assertTrue(interpreter.hasVar("mySubnet"), "mySubnet resource should be created");
    }

    @Test
    @DisplayName("should import all types from provider domain with wildcard")
    void importAllTypesFromDomainWithWildcard() {
        mockLookup.registerSchema("aws", "storage", "S3Bucket", """
                schema S3Bucket {
                    string name
                }
                """);
        mockLookup.registerSchema("aws", "storage", "EbsVolume", """
                schema EbsVolume {
                    number sizeGb
                }
                """);

        eval("""
                import * from "aws/storage"

                resource S3Bucket myBucket {
                    name = "my-bucket"
                }

                resource EbsVolume myVolume {
                    sizeGb = 100
                }
                """);

        assertTrue(interpreter.hasVar("S3Bucket"), "S3Bucket schema should be imported");
        assertTrue(interpreter.hasVar("EbsVolume"), "EbsVolume schema should be imported");
    }

    @Test
    @DisplayName("should import all types from provider with wildcard")
    void importAllTypesFromProvider() {
        mockLookup.registerSchema("aws", "networking", "Vpc", """
                schema Vpc {
                    string cidrBlock
                }
                """);
        mockLookup.registerSchema("aws", "storage", "S3Bucket", """
                schema S3Bucket {
                    string name
                }
                """);

        eval("""
                import * from "aws"

                resource Vpc myVpc {
                    cidrBlock = "10.0.0.0/16"
                }

                resource S3Bucket myBucket {
                    name = "my-bucket"
                }
                """);

        assertTrue(interpreter.hasVar("Vpc"), "Vpc schema should be imported");
        assertTrue(interpreter.hasVar("S3Bucket"), "S3Bucket schema should be imported");
    }

    @Test
    @DisplayName("should error when importing non-existent type from provider")
    void errorWhenImportingNonExistentType() {
        mockLookup.registerSchema("aws", "networking", "Vpc", """
                schema Vpc {
                    string cidrBlock
                }
                """);

        var exception = assertThrows(RuntimeException.class, () -> eval("""
                import NonExistent from "aws/networking"
                """));

        assertTrue(exception.getMessage().contains("NonExistent") ||
                   exception.getMessage().contains("not found"),
                "Error should mention missing type: " + exception.getMessage());
    }

    @Test
    @DisplayName("should error when importing from unknown provider")
    void errorWhenImportingFromUnknownProvider() {
        // Don't register any schemas - the provider lookup will return false for isKnownProvider
        var exception = assertThrows(RuntimeException.class, () -> eval("""
                import Something from "unknown/domain"
                """));

        // Should fall back to directory import and fail
        assertTrue(exception.getMessage().contains("not found") ||
                   exception.getMessage().contains("directory"),
                "Error should mention missing directory: " + exception.getMessage());
    }

    @Test
    @DisplayName("should error when importing from empty domain")
    void errorWhenImportingFromEmptyDomain() {
        // Register the provider but not the domain
        mockLookup.registerSchema("aws", "networking", "Vpc", """
                schema Vpc {
                    string cidrBlock
                }
                """);

        var exception = assertThrows(RuntimeException.class, () -> eval("""
                import Something from "aws/compute"
                """));

        assertTrue(exception.getMessage().contains("No schemas found") ||
                   exception.getMessage().contains("compute"),
                "Error should mention empty domain: " + exception.getMessage());
    }

    @Test
    @DisplayName("should not import types that were not requested")
    void shouldNotImportUnrequestedTypes() {
        mockLookup.registerSchema("aws", "networking", "Vpc", """
                schema Vpc {
                    string cidrBlock
                }
                """);
        mockLookup.registerSchema("aws", "networking", "Subnet", """
                schema Subnet {
                    string cidrBlock
                }
                """);

        eval("""
                import Vpc from "aws/networking"
                """);

        assertTrue(interpreter.hasVar("Vpc"), "Vpc should be imported");
        assertFalse(interpreter.hasVar("Subnet"), "Subnet should NOT be imported");
    }

    @Test
    @DisplayName("should handle provider import with file import")
    void mixProviderAndFileImports() {
        mockLookup.registerSchema("aws", "networking", "Vpc", """
                schema Vpc {
                    string cidrBlock
                }
                """);

        eval("""
                import add from "imports/math_utils.kite"
                import Vpc from "aws/networking"

                var myMixedSum = add(1, 2)

                resource Vpc myMixedVpc {
                    cidrBlock = "10.0.0.0/16"
                }
                """);

        assertEquals(3, interpreter.getVar("myMixedSum"));
        assertTrue(interpreter.hasVar("Vpc"));
        assertTrue(interpreter.hasVar("myMixedVpc"));
    }

    /**
     * Mock implementation of ProviderSchemaLookup for testing.
     */
    private static class MockProviderSchemaLookup implements ProviderSchemaLookup {
        private final java.util.Map<String, java.util.Map<String, java.util.List<SchemaInfo>>> schemas = new java.util.HashMap<>();

        void registerSchema(String provider, String domain, String typeName, String schemaString) {
            schemas.computeIfAbsent(provider, k -> new java.util.HashMap<>())
                   .computeIfAbsent(domain, k -> new java.util.ArrayList<>())
                   .add(new SchemaInfo(typeName, domain, provider, schemaString));
        }

        @Override
        public List<SchemaInfo> getSchemas(String provider, String domain) {
            var providerSchemas = schemas.get(provider);
            if (providerSchemas == null) {
                return List.of();
            }
            return providerSchemas.getOrDefault(domain, List.of());
        }

        @Override
        public List<SchemaInfo> getAllSchemas(String provider) {
            var providerSchemas = schemas.get(provider);
            if (providerSchemas == null) {
                return List.of();
            }
            return providerSchemas.values().stream()
                    .flatMap(List::stream)
                    .toList();
        }

        @Override
        public boolean isKnownProvider(String provider) {
            return schemas.containsKey(provider);
        }

        @Override
        public SchemaInfo getSchema(String typeName) {
            return schemas.values().stream()
                    .flatMap(m -> m.values().stream())
                    .flatMap(List::stream)
                    .filter(s -> s.typeName().equals(typeName))
                    .findFirst()
                    .orElse(null);
        }
    }
}
