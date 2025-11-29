package cloud.kitelang.semantics.scope;

import cloud.kitelang.base.RuntimeTest;
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