package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.syntax.ast.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class FunResourceTest extends RuntimeTest {

    @Test
    void funDeclaration() {
        assertThrows(ValidationException.class, () -> eval("""
                schema vm {
                   var string name
                }
                
                fun myFun(){
                    resource vm main {
                        name = 'prod'
                    }
                }
                """));
    }

}
