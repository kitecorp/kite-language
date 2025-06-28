package io.zmeu.Frontend.Lexical;

import io.zmeu.Base.RuntimeTest;
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