package io.kite.frontend.lexical;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

class ResourceTest extends RuntimeTest {

    @Test
    void varNameCollision() {
         resolve("""
                resource vm main {
                 name = "main"
                 }
                """);

    }


}