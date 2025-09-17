package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.AnyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Output")
public class AnnotationOutputTest extends CheckerTest {

    @Test
    void outputAnyInitNull() {
        var res = eval("""
                @sensitive
                output any something = null""");
        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void annotationUnkown() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitiveUnknown
                output any something = null"""));

    }


}
