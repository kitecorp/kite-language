package cloud.kitelang.integration;

import cloud.kitelang.execution.exceptions.InvalidInitException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("@cloud decorator integration tests")
public class CloudDecoratorTest extends BaseIntegrationTest {

    @Test
    @DisplayName("@cloud property cannot be initialized in schema")
    void cloudPropertyCannotBeInitializedInSchema() {
        var exception = assertThrows(InvalidInitException.class, () -> eval("""
                schema Bucket {
                    string name
                    @cloud string arn = "arn:aws:s3:::bucket"
                }
                """));

        assertTrue(exception.getMessage().contains("@cloud property 'arn' cannot have an initialization value"));
    }

    @Test
    @DisplayName("@cloud property cannot be set in resource")
    void cloudPropertyCannotBeSetInResource() {
        var exception = assertThrows(InvalidInitException.class, () -> eval("""
                schema Bucket {
                    string name
                    @cloud string arn
                }

                resource Bucket myBucket {
                    name = "my-bucket"
                    arn = "arn:aws:s3:::my-bucket"
                }
                """));

        assertTrue(exception.getMessage().contains("Cannot set"));
        assertTrue(exception.getMessage().contains("@cloud"));
        assertTrue(exception.getMessage().contains("arn"));
    }

    @Test
    @DisplayName("Regular properties can still be set in resource")
    void regularPropertiesCanBeSetInResource() {
        assertDoesNotThrow(() -> eval("""
                schema Bucket {
                    string name
                    string region
                    @cloud string arn
                }

                resource Bucket myBucket {
                    name = "my-bucket"
                    region = "us-east-1"
                }
                """));
    }

    @Test
    @DisplayName("Multiple @cloud properties are tracked correctly")
    void multipleCloudPropertiesTracked() {
        var exception = assertThrows(InvalidInitException.class, () -> eval("""
                schema Instance {
                    string name
                    @cloud string arn
                    @cloud(importable) string id
                    @cloud string publicIp
                }

                resource Instance server {
                    name = "web-server"
                    publicIp = "1.2.3.4"
                }
                """));

        assertTrue(exception.getMessage().contains("publicIp"));
    }

    @Test
    @DisplayName("@cloud(importable) property also cannot be set")
    void cloudImportablePropertyCannotBeSet() {
        var exception = assertThrows(InvalidInitException.class, () -> eval("""
                schema Instance {
                    string name
                    @cloud(importable) string id
                }

                resource Instance server {
                    name = "web-server"
                    id = "i-1234567890abcdef0"
                }
                """));

        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    @DisplayName("@cloud properties are initialized to null in schema")
    void cloudPropertiesInitializedToNull() {
        eval("""
                schema Bucket {
                    string name
                    @cloud string arn
                }

                resource Bucket myBucket {
                    name = "my-bucket"
                }
                """);

        var bucket = interpreter.getInstance("myBucket");
        assertNotNull(bucket);
        assertEquals("my-bucket", bucket.lookup("name"));
        assertNull(bucket.lookup("arn"));
    }

    @Test
    @DisplayName("Schema without @cloud properties works normally")
    void schemaWithoutCloudPropertiesWorksNormally() {
        eval("""
                schema Config {
                    string name
                    number port
                }

                resource Config app {
                    name = "my-app"
                    port = 8080
                }
                """);

        var config = interpreter.getInstance("app");
        assertNotNull(config);
        assertEquals("my-app", config.lookup("name"));
        assertEquals(8080, config.lookup("port"));
    }
}