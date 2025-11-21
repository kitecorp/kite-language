package io.kite.semantics;

import io.kite.base.CheckerTest;
import io.kite.semantics.types.ArrayType;
import io.kite.semantics.types.ObjectType;
import io.kite.semantics.types.ResourceType;
import io.kite.semantics.types.ValueType;
import org.junit.jupiter.api.Assertions;
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
                [for i in 0..10: if i>2 {i+=1}]
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
        assertEquals(res, checker.getEnv().lookup("x"));
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

    @Test
    void testIndexInListNumbers() {
        var res = eval("""
                var list = [1,2,3,4,5]
                var x = [for index in list: { name: 'item-$index'}]
                """);
        assertInstanceOf(ArrayType.class, res);

        var varType = (ArrayType) res;
        var objectType = new ObjectType(new TypeEnvironment(varType.getEnvironment().getParent(), Map.of("name", ValueType.String)));
        assertEquals(objectType, varType.getType());
    }

    @Test
    void testIndexInListStrings() {
        var res = eval("""
                var list = ['hello','world','!']
                var x = [for index in list: { name: 'item-$index'}]
                """);
        assertInstanceOf(ArrayType.class, res);

        var varType = (ArrayType) res;
        var objectType = new ObjectType(new TypeEnvironment(varType.getEnvironment().getParent(), Map.of("name", ValueType.String)));
        assertEquals(objectType, varType.getType());
    }

    @Test
    void arrayResourcesOverObjects() {
        var array = eval("""
                schema Bucket {
                   string name
                }
                var envs = [{client: 'amazon'},{client: 'bmw'}]
                [for index in envs]
                resource Bucket photos {
                  name     = 'name-${index.client}'
                }
                """);

        Assertions.assertInstanceOf(ArrayType.class, array);
        var arrayType = (ArrayType) array;
        var resourceType = (ResourceType) arrayType.getType();
        var res = new ResourceType("photos", resourceType.getSchema(), arrayType.getEnvironment());
        assertEquals(res, arrayType.getType());
    }

    @Test
    void arrayResourcesOverNumbers() {
        var array = eval("""
                schema Bucket {
                   string name
                }
                var envs = [1,2,3]
                [for index in envs]
                resource Bucket photos {
                  name     = 'name-${index}'
                }
                """);

        Assertions.assertInstanceOf(ArrayType.class, array);
        var arrayType = (ArrayType) array;
        var resourceType = (ResourceType) arrayType.getType();
        var res = new ResourceType("photos", resourceType.getSchema(), arrayType.getEnvironment());
        assertEquals(res, arrayType.getType());
    }

    @Test
    void arrayResourcesOverStrings() {
        var array = eval("""
                schema Bucket {
                   string name
                }
                var envs = ['hello', 'world']
                [for index in envs]
                resource Bucket photos {
                  name     = 'name-${index}'
                }
                """);

        Assertions.assertInstanceOf(ArrayType.class, array);
        var arrayType = (ArrayType) array;
        var resourceType = (ResourceType) arrayType.getType();
        var res = new ResourceType("photos", resourceType.getSchema(), arrayType.getEnvironment());
        assertEquals(res, arrayType.getType());
    }

}
