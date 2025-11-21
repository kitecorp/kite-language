package io.kite.semantics.scope;

import io.kite.base.RuntimeTest;
import io.kite.execution.exceptions.DeclarationExistsException;
import io.kite.syntax.ast.ValidationException;
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