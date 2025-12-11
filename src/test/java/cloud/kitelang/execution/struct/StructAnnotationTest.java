package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct annotations.
 */
@DisplayName("Struct with Annotations")
public class StructAnnotationTest extends RuntimeTest {

    @Test
    @DisplayName("Struct property with @cloud annotation")
    void structWithCloudAnnotation() {
        var result = eval("""
                struct AWSResource {
                    string name
                    @cloud string arn
                }
                AWSResource
                """);

        assertInstanceOf(StructValue.class, result);
        var structValue = (StructValue) result;
        assertTrue(structValue.isCloudProperty("arn"));
        assertFalse(structValue.isCloudProperty("name"));
    }

    @Test
    @DisplayName("Cloud property cannot have initialization")
    void cloudPropertyCannotHaveInit() {
        assertThrows(Exception.class, () -> eval("""
                struct AWSResource {
                    string name
                    @cloud string arn = "arn:aws:..."
                }
                """));
    }
}
