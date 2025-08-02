package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ArrayType;
import io.zmeu.TypeChecker.Types.ObjectType;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("TypeChecker Loops")
public class ForLoopTest extends CheckerTest {

    @Test
    void testBlock() {
        var actual = eval("""
                var x = 10
                while (x!=0) {
                   x--
                }
                x
                """);
        assertEquals(ValueType.Number, actual);
    }


    @Test
    void testForInRange() {
        var res = eval("""
                for i in 0..10 {
                    i+=1
                }
                """);
        assertEquals(ValueType.Number, res);
    }

    @Test
    void testFor() {
        var res = eval("""
                [for i in 0..10: i+=1]
                """);

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testForConditional() {
        var res = eval("""
                [for i in 0..10: if i>2 i+=1]
                """);
        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testForStringDoubleQuotes() {
        var res = eval("""
                [for i in 0..10: "item-$i"]
                """);

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void arrayAssignedToVar() {
        var res = eval("""
                var x = [for index in 1..5: 'item-$index']
                """);

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void arrayObjectsAssignedToVar() {
        var res = eval("""
                var x = [for index in 1..5: { name: 'item-$index'}]
                """);
        assertInstanceOf(ArrayType.class, res);

        var varType = (ArrayType) res;
        var objectType = new ObjectType(new TypeEnvironment(varType.getEnvironment().getParent(), Map.of("name", ValueType.String)));
        assertEquals(objectType, varType.getType());
    }

}
