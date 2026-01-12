package cloud.kitelang.integration;

import cloud.kitelang.execution.values.DeferredResourceTemplate;
import cloud.kitelang.execution.values.DeferredValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for deferred resource template creation when @count depends on @cloud properties.
 * When @count evaluates to a DeferredValue (because it references a cloud property),
 * a DeferredResourceTemplate is created instead of actual resources.
 */
@DisplayName("Deferred resource template tests (@count with @cloud)")
public class DeferredResourceTemplateTest extends BaseIntegrationTest {

    @Test
    @DisplayName("@count with cloud property creates deferred template")
    void countWithCloudPropertyCreatesDeferredTemplate() {
        eval("""
                schema Vpc {
                    string cidrBlock
                    @cloud number subnetCount
                }
                schema Subnet {
                    string cidrBlock
                }

                resource Vpc vpc {
                    cidrBlock = "10.0.0.0/16"
                }

                @count(vpc.subnetCount)
                resource Subnet subnet {
                    cidrBlock = "10.0.$count.0/24"
                }
                """);

        // The subnet should not be created yet (deferred)
        var subnet = interpreter.getInstance("subnet");
        assertNull(subnet);

        // Verify a deferred template was created
        assertTrue(interpreter.hasDeferredTemplates());
        assertEquals(1, interpreter.getDeferredTemplates().size());

        var template = interpreter.getDeferredTemplates().get(0);
        assertEquals("subnet", template.templateName());
        assertTrue(template.dependencies().contains("vpc"));
    }

    @Test
    @DisplayName("@count with literal number does not create deferred template")
    void countWithLiteralNumberDoesNotCreateDeferredTemplate() {
        eval("""
                schema Subnet {
                    string cidrBlock
                }

                @count(3)
                resource Subnet subnet {
                    cidrBlock = "10.0.$count.0/24"
                }
                """);

        // Resources should be created normally
        var subnet0 = interpreter.getInstance("subnet[0]");
        var subnet1 = interpreter.getInstance("subnet[1]");
        var subnet2 = interpreter.getInstance("subnet[2]");
        assertNotNull(subnet0);
        assertNotNull(subnet1);
        assertNotNull(subnet2);

        // No deferred templates
        assertFalse(interpreter.hasDeferredTemplates());
    }

    @Test
    @DisplayName("@count with variable containing number does not create deferred template")
    void countWithVariableContainingNumberDoesNotCreateDeferredTemplate() {
        eval("""
                schema Subnet {
                    string cidrBlock
                }

                var numSubnets = 2

                @count(numSubnets)
                resource Subnet subnet {
                    cidrBlock = "10.0.$count.0/24"
                }
                """);

        // Resources should be created normally
        var subnet0 = interpreter.getInstance("subnet[0]");
        var subnet1 = interpreter.getInstance("subnet[1]");
        assertNotNull(subnet0);
        assertNotNull(subnet1);

        // No deferred templates
        assertFalse(interpreter.hasDeferredTemplates());
    }

    @Test
    @DisplayName("DeferredResourceTemplate captures correct dependency information")
    void deferredResourceTemplateCapturesCorrectDependencyInfo() {
        eval("""
                schema Vpc {
                    string cidrBlock
                    @cloud number availabilityZoneCount
                }
                schema Subnet {
                    string vpcId
                    string cidrBlock
                }

                resource Vpc myVpc {
                    cidrBlock = "10.0.0.0/16"
                }

                @count(myVpc.availabilityZoneCount)
                resource Subnet mySubnet {
                    vpcId = "vpc-123"
                    cidrBlock = "10.0.$count.0/24"
                }
                """);

        assertTrue(interpreter.hasDeferredTemplates());
        var template = interpreter.getDeferredTemplates().get(0);

        assertEquals("mySubnet", template.templateName());
        assertEquals("Subnet", template.resourceType().string());
        assertTrue(template.dependencies().contains("myVpc"));

        // Verify the deferred value captured
        var deferredValue = template.deferredValue();
        assertNotNull(deferredValue);
        assertEquals("myVpc", deferredValue.dependencyName());
        assertEquals("availabilityZoneCount", deferredValue.propertyPath());
    }

    @Test
    @DisplayName("DeferredResourceTemplate provides meaningful reason")
    void deferredResourceTemplateProvidesReason() {
        eval("""
                schema Cloud {
                    @cloud number instanceCount
                }
                schema Instance {
                    string name
                }

                resource Cloud config {
                }

                @count(config.instanceCount)
                resource Instance server {
                    name = "server-$count"
                }
                """);

        assertTrue(interpreter.hasDeferredTemplates());
        var template = interpreter.getDeferredTemplates().get(0);

        String reason = template.getDeferredReason();
        assertTrue(reason.contains("config"));
        assertTrue(reason.contains("instanceCount"));
    }

    @Test
    @DisplayName("Multiple deferred templates can be created")
    void multipleDeferredTemplatesCanBeCreated() {
        eval("""
                schema Vpc {
                    @cloud number subnetCount
                    @cloud number securityGroupCount
                }
                schema Subnet {
                    string cidrBlock
                }
                schema SecurityGroup {
                    string name
                }

                resource Vpc vpc {
                }

                @count(vpc.subnetCount)
                resource Subnet subnet {
                    cidrBlock = "10.0.$count.0/24"
                }

                @count(vpc.securityGroupCount)
                resource SecurityGroup sg {
                    name = "sg-$count"
                }
                """);

        assertTrue(interpreter.hasDeferredTemplates());
        assertEquals(2, interpreter.getDeferredTemplates().size());

        var subnetTemplate = interpreter.getDeferredTemplates().stream()
                .filter(t -> t.templateName().equals("subnet"))
                .findFirst()
                .orElse(null);
        var sgTemplate = interpreter.getDeferredTemplates().stream()
                .filter(t -> t.templateName().equals("sg"))
                .findFirst()
                .orElse(null);

        assertNotNull(subnetTemplate);
        assertNotNull(sgTemplate);
    }

    @Test
    @DisplayName("DeferredResourceTemplate allDependenciesResolved check")
    void deferredResourceTemplateAllDependenciesResolvedCheck() {
        var deferred = new DeferredValue("vpc", "subnetCount");
        var template = new DeferredResourceTemplate(
                "subnet",
                null,
                null,
                java.util.Set.of("vpc"),
                null,
                deferred
        );

        assertFalse(template.allDependenciesResolved(java.util.Set.of()));
        assertFalse(template.allDependenciesResolved(java.util.Set.of("other")));
        assertTrue(template.allDependenciesResolved(java.util.Set.of("vpc")));
        assertTrue(template.allDependenciesResolved(java.util.Set.of("vpc", "other")));
    }

    @Test
    @DisplayName("DeferredResourceTemplate toString format")
    void deferredResourceTemplateToStringFormat() {
        var deferred = new DeferredValue("vpc", "subnetCount");
        var template = new DeferredResourceTemplate(
                "subnet",
                null,
                null,
                java.util.Set.of("vpc"),
                null,
                deferred
        );

        String str = template.toString();
        assertTrue(str.contains("subnet"));
        assertTrue(str.contains("[?]"));
        assertTrue(str.contains("deferred"));
    }
}
