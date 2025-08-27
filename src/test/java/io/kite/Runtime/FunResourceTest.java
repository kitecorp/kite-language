package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Parser.ParserErrors;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

@Log4j2
public class FunResourceTest extends RuntimeTest {

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
