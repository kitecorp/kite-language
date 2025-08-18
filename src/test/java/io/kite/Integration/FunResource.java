package io.kite.Integration;

import io.kite.Base.RuntimeTest;
import io.kite.ParserErrors;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

@Log4j2
public class FunResource extends RuntimeTest {

    @Test
    void funDeclaration() {
        eval("""
                schema vm {
                   var string name
                }
                
                fun myFun(){
                    resource vm main {
                        name = 'prod'
                    }
                }
                """);
        Assumptions.assumeTrue(ParserErrors.hadErrors());
    }

}
