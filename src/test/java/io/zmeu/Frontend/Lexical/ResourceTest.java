package io.zmeu.Frontend.Lexical;

import io.zmeu.Runtime.BaseRuntimeTest;
import org.junit.jupiter.api.Test;

class ResourceTest extends BaseRuntimeTest {

    @Test
    void varNameCollision() {
         resolve("""
                resource vm main {
                 name = "main"
                 }
                """);

    }


}