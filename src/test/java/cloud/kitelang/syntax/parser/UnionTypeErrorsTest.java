package cloud.kitelang.syntax.parser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@DisplayName("Parser Type alias - syntax validation")
public class UnionTypeErrorsTest extends ParserTest {
    @Test
    void typeRepeatingInt() {
        // Valid syntax, duplicate checking is typechecker's job
        var res = parse("type custom = 1 | 1");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingDecimal() {
        var res = parse("type custom = 1.2 | 1.2");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingString() {
        var res = parse("type custom = 'hi' | \"hi\"");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingBoolean() {
        var res = parse("type custom = true | true");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingNull() {
        var res = parse("type custom = null | null");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingNumber() {
        var res = parse("type custom = number | number");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingStringKeyword() {
        var res = parse("type custom = string | string");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingBooleanKeyword() {
        var res = parse("type custom = boolean | boolean");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingObjectKeyword() {
        var res = parse("type custom = object | object");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingEmptyObject() {
        var res = parse("type custom = {} | {}");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingObject() {
        var res = parse("type custom = { env: 123, color: 'red' } | { env: 123, color: 'red' }");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingArrayInts() {
        var res = parse("type custom = [1,2,3] | [1,2,3]");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingArrayDecimals() {
        var res = parse("type custom = [1.1, 2.2, 3.3] | [1.1, 2.2, 3.3]");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingArrayBoolean() {
        var res = parse("type custom = [true] | [true]");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingArrayStrings() {
        var res = parse("type custom = ['hello'] | ['hello']");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingArrayObjectEmpty() {
        var res = parse("type custom = [{}] | [{}]");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingArrayObjectEmptyKeyword() {
        var res = parse("type custom = [{}] | [object]");
        assertNotNull(res);
    }

    @Test
    void typeRepeatingArrayObjectKeyword() {
        var res = parse("type custom = [object] | [object]");
        assertNotNull(res);
    }


}
