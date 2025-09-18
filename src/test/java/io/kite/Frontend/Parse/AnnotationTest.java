package io.kite.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
@DisplayName("Parser Outputs")
public class AnnotationTest extends ParserTest {

    @Test
    void outputString() {
        var res = parse("@annotation output string something");
        log.info(res);
    }

    

}
