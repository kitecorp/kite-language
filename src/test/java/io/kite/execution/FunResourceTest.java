package io.kite.execution;

import io.kite.base.RuntimeTest;
import io.kite.syntax.ast.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
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
