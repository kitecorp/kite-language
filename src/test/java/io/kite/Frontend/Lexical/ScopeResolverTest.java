package io.kite.Frontend.Lexical;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Parser.ValidationException;
import io.kite.Runtime.exceptions.DeclarationExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ScopeResolverTest extends RuntimeTest {

    @Test
    void varNameCollision() {
        Assertions.assertThrows(DeclarationExistsException.class, () -> resolve("""
                {
                  var a = "first";
                  var a = "second";
                }
                """)
        );
    }
    @Test
    void returnTopFunctionShouldThrow() {
        Assertions.assertThrows(ValidationException.class, () -> resolve("""
                return "second";
                """)
        );
    }

    @Test
    void returnInsideBlockShouldFail() {
        Assertions.assertThrows(ValidationException.class, () -> resolve("""
                if (x==2) {
                    return "second";
                }
                """)
        );
    }

    @Test
    void returnInsideFunctionShouldSucceed() {
        resolve("""
                fun x() {
                    return "second";
                }
                """);
    }

}