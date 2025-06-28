package io.zmeu.Frontend.Lexical;

import io.zmeu.Frontend.Parser.errors.ParseError;
import io.zmeu.Base.RuntimeTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResolverTest extends RuntimeTest {

    @Test
    void varNameCollision() {
        Assertions.assertThrows(ParseError.class, () -> resolve("""
                {
                  var a = "first";
                  var a = "second";
                }
                """)
        );
    }
    @Test
    void returnTopFunctionShouldThrow() {
        Assertions.assertThrows(ParseError.class, () -> resolve("""
                return "second";
                """)
        );
    }
    @Test
    void returnInsideBlockShouldFail() {
        Assertions.assertThrows(ParseError.class, () -> resolve("""
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