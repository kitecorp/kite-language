package cloud.kitelang.integration;

import cloud.kitelang.execution.values.DeferredResourceTemplate;
import cloud.kitelang.execution.values.DeferredValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for deferred resource handling when @count depends on @cloud properties.
 * When @count evaluates to a DeferredValue (because it references a cloud property),
 * a CloudPropertyObserver is registered for reactive re-evaluation during apply.
 *
 * <p>The reactive pattern:
 * <ol>
 *   <li>During plan: @count(vpc.subnetCount) encounters DeferredValue</li>
 *   <li>CloudPropertyObserver is registered with CloudObservable</li>
 *   <li>Resource is marked as cloudPending</li>
 *   <li>During apply: after vpc is created, observers are notified</li>
 *   <li>@count re-evaluates with actual value, creates resources</li>
 * </ol>
 */
@DisplayName("Deferred resource template tests (@count with @cloud)")
public class DeferredResourceTemplateTest extends BaseIntegrationTest {

    @Test
    @DisplayName("@count with cloud property registers cloud observer")
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

        // Verify a cloud observer was registered (new reactive pattern)
        assertTrue(interpreter.getCloudObservable().hasObservers(),
                "CloudObservable should have observers for deferred @count");
        assertTrue(interpreter.getCloudObservable().getPendingResources().contains("vpc"),
                "CloudObservable should be waiting on 'vpc'");
    }

    @Test
    @DisplayName("@count with length(cloudProperty) registers cloud observer")
    void countWithLengthOfCloudPropertyCreatesDeferredTemplateWithWrapperFunction() {
        eval("""
                schema Subnet {
                    string cidrBlock
                    @cloud string arn
                }
                schema Bucket {
                    string name
                }

                resource Subnet subnet {
                    cidrBlock = "10.0.0.0/24"
                }

                @count(length(subnet.arn))
                resource Bucket bucket {
                    name = "bucket-$count"
                }
                """);

        // The bucket should not be created yet (deferred)
        var bucket = interpreter.getInstance("bucket");
        assertNull(bucket);

        // Verify a cloud observer was registered (new reactive pattern)
        assertTrue(interpreter.getCloudObservable().hasObservers(),
                "CloudObservable should have observers for deferred @count");
        assertTrue(interpreter.getCloudObservable().getPendingResources().contains("subnet"),
                "CloudObservable should be waiting on 'subnet'");
    }

    @Test
    @DisplayName("@count with literal number does not register cloud observer")
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

        // No cloud observers registered
        assertFalse(interpreter.getCloudObservable().hasObservers(),
                "No cloud observers should be registered for literal count");
    }

    @Test
    @DisplayName("@count with variable containing number does not register cloud observer")
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

        // No cloud observers registered
        assertFalse(interpreter.getCloudObservable().hasObservers(),
                "No cloud observers should be registered for variable count");
    }

    @Test
    @DisplayName("CloudObservable captures correct dependency information")
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

        // Verify cloud observer is registered with correct dependency
        assertTrue(interpreter.getCloudObservable().hasObservers(),
                "CloudObservable should have observers");
        assertTrue(interpreter.getCloudObservable().getPendingResources().contains("myVpc"),
                "CloudObservable should be waiting on 'myVpc'");

        // The subnet should not be created yet
        var subnet = interpreter.getInstance("mySubnet");
        assertNull(subnet, "mySubnet should not be created - it's deferred");
    }

    @Test
    @DisplayName("Cloud observer registered for resource waiting on cloud property")
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

        // Verify cloud observer is registered
        assertTrue(interpreter.getCloudObservable().hasObservers(),
                "CloudObservable should have observers");
        assertTrue(interpreter.getCloudObservable().getPendingResources().contains("config"),
                "CloudObservable should be waiting on 'config'");
    }

    @Test
    @DisplayName("Multiple cloud observers can be registered")
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

        // Both decorators depend on vpc, so both should register observers
        assertTrue(interpreter.getCloudObservable().hasObservers(),
                "CloudObservable should have observers");
        assertTrue(interpreter.getCloudObservable().getPendingResources().contains("vpc"),
                "CloudObservable should be waiting on 'vpc'");

        // Neither resource should be created yet
        assertNull(interpreter.getInstance("subnet"), "subnet should not be created");
        assertNull(interpreter.getInstance("sg"), "sg should not be created");
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
                deferred,
                null
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
                deferred,
                null
        );

        String str = template.toString();
        assertTrue(str.contains("subnet"));
        assertTrue(str.contains("[?]"));
        assertTrue(str.contains("deferred"));
    }
}
