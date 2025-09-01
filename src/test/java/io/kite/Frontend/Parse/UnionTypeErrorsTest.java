package io.kite.Frontend.Parse;

import io.kite.Frontend.Parser.ParserErrors;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
@DisplayName("Parser Type alias errors")
public class UnionTypeErrorsTest extends ParserTest {

    @Test
    void typeRepeatingIntError() {
        parse("type custom = 1 | 1");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingDecimalError() {
        parse("type custom = 1.2 | 1.2");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingStringError() {
        parse("type custom = 'hi' | \"hi\" ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingBooleanError() {
        parse("type custom = true | true ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingNullError() {
        parse("type custom = null | null ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingNumberError() {
        parse("type custom = number | number ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }


    @Test
    void typeRepeatingStringKeywordError() {
        parse("type custom = string | string ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingBooleanKeywordError() {
        parse("type custom = boolean | boolean ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingObjectKeywordError() {
        parse("type custom = object | object ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingEmptyObjectError() {
        parse("type custom = {} | {} ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingObjectError() {
        parse("type custom = { env: 123, color: 'red' } | { env: 123, color: 'red' } ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayIntsError() {
        parse("type custom = [1,2,3] | [1,2,3] ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayDecimalsError() {
        parse("type custom = [1.1, 2.2, 3.3] | [1.1, 2.2, 3.3] ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }


    @Test
    void typeRepeatingArrayBooleanError() {
        parse("type custom = [true] | [true] ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayStringsError() {
        parse("type custom = ['hello'] | ['hello'] ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayObjectEmptyError() {
        parse("type custom = [{}] | [{}] ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayObjectEmptyKeywordError() {
        parse("type custom = [{}] | [object] ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }

    @Test
    void typeRepeatingArrayObjectKeywordError() {
        parse("type custom = [object] | [object] ");
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
    }


}
