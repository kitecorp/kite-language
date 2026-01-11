package cloud.kitelang.integration;

import cloud.kitelang.execution.values.DeferredValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Deferred cloud property reference tests")
public class DeferredCloudPropertyTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Referencing @cloud property creates DeferredValue")
    void referencingCloudPropertyCreatesDeferredValue() {
        eval("""
                schema Vpc {
                    string cidrBlock
                    @cloud string vpcId
                }
                schema Subnet {
                    string vpcId
                    string cidrBlock
                }

                resource Vpc example {
                    cidrBlock = "10.0.0.0/24"
                }

                resource Subnet subnet {
                    vpcId = example.vpcId
                    cidrBlock = "10.0.0.0/25"
                }
                """);

        var subnet = interpreter.getInstance("subnet");
        assertNotNull(subnet);

        // The vpcId should be null (cloud property not yet resolved)
        assertNull(subnet.lookup("vpcId"));

        // The resource should have a deferred property
        assertTrue(subnet.hasDeferredProperties());
        assertEquals(1, subnet.getDeferredProperties().size());

        // Verify the deferred reference
        var deferred = subnet.getDeferredProperties().get("vpcId");
        assertNotNull(deferred);
        assertEquals("example", deferred.dependencyName());
        assertEquals("vpcId", deferred.propertyPath());
    }

    @Test
    @DisplayName("Referencing regular property does not create DeferredValue")
    void referencingRegularPropertyDoesNotCreateDeferredValue() {
        eval("""
                schema Vpc {
                    string cidrBlock
                    string name
                }
                schema Subnet {
                    string vpcName
                    string cidrBlock
                }

                resource Vpc example {
                    cidrBlock = "10.0.0.0/24"
                    name = "my-vpc"
                }

                resource Subnet subnet {
                    vpcName = example.name
                    cidrBlock = "10.0.0.0/25"
                }
                """);

        var subnet = interpreter.getInstance("subnet");
        assertNotNull(subnet);

        // The vpcName should have the actual value (not deferred)
        assertEquals("my-vpc", subnet.lookup("vpcName"));

        // No deferred properties
        assertFalse(subnet.hasDeferredProperties());
    }

    @Test
    @DisplayName("Multiple deferred cloud property references")
    void multipleDeferredCloudPropertyReferences() {
        eval("""
                schema Vpc {
                    string cidrBlock
                    @cloud string vpcId
                    @cloud string arn
                }
                schema Subnet {
                    string vpcId
                    string vpcArn
                    string cidrBlock
                }

                resource Vpc example {
                    cidrBlock = "10.0.0.0/24"
                }

                resource Subnet subnet {
                    vpcId = example.vpcId
                    vpcArn = example.arn
                    cidrBlock = "10.0.0.0/25"
                }
                """);

        var subnet = interpreter.getInstance("subnet");
        assertNotNull(subnet);

        // Both properties should be deferred
        assertTrue(subnet.hasDeferredProperties());
        assertEquals(2, subnet.getDeferredProperties().size());

        assertNotNull(subnet.getDeferredProperties().get("vpcId"));
        assertNotNull(subnet.getDeferredProperties().get("vpcArn"));
    }

    @Test
    @DisplayName("Deferred references from multiple resources")
    void deferredReferencesFromMultipleResources() {
        eval("""
                schema Vpc {
                    string cidrBlock
                    @cloud string vpcId
                }
                schema SecurityGroup {
                    string name
                    @cloud string groupId
                }
                schema Instance {
                    string vpcId
                    string securityGroupId
                }

                resource Vpc vpc {
                    cidrBlock = "10.0.0.0/24"
                }

                resource SecurityGroup sg {
                    name = "my-sg"
                }

                resource Instance server {
                    vpcId = vpc.vpcId
                    securityGroupId = sg.groupId
                }
                """);

        var server = interpreter.getInstance("server");
        assertNotNull(server);

        // Both properties should be deferred
        assertTrue(server.hasDeferredProperties());
        assertEquals(2, server.getDeferredProperties().size());

        // Verify deferred references point to correct resources
        var vpcIdDeferred = server.getDeferredProperties().get("vpcId");
        assertEquals("vpc", vpcIdDeferred.dependencyName());
        assertEquals("vpcId", vpcIdDeferred.propertyPath());

        var sgIdDeferred = server.getDeferredProperties().get("securityGroupId");
        assertEquals("sg", sgIdDeferred.dependencyName());
        assertEquals("groupId", sgIdDeferred.propertyPath());
    }

    @Test
    @DisplayName("Deferred property adds dependency for topological sort")
    void deferredPropertyAddsDependency() {
        eval("""
                schema Vpc {
                    string cidrBlock
                    @cloud string vpcId
                }
                schema Subnet {
                    string vpcId
                    string cidrBlock
                }

                resource Vpc example {
                    cidrBlock = "10.0.0.0/24"
                }

                resource Subnet subnet {
                    vpcId = example.vpcId
                    cidrBlock = "10.0.0.0/25"
                }
                """);

        var subnet = interpreter.getInstance("subnet");
        assertNotNull(subnet);

        // The subnet should depend on example
        assertTrue(subnet.hasDependencies());
        assertTrue(subnet.getDependencies().contains("example"));
    }

    @Test
    @DisplayName("DeferredValue toString format")
    void deferredValueToStringFormat() {
        var deferred = new DeferredValue("myVpc", "vpcId");
        assertEquals("${myVpc.vpcId}", deferred.toString());
    }
}
