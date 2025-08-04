package io.kite.Frontend.Lexical;

import io.kite.Base.RuntimeTest;
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