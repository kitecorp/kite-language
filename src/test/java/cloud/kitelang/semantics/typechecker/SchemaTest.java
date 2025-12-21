package cloud.kitelang.semantics.typechecker;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeEnvironment;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.ObjectType;
import cloud.kitelang.semantics.types.SchemaType;
import cloud.kitelang.semantics.types.ValueType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("TypeChecker Schema")
public class SchemaTest extends CheckerTest {

    @Test
    void equals() {
        var typeEnv = new TypeEnvironment(Map.of("VERSION", ValueType.String));
        var base = new SchemaType("dog", typeEnv);
        var schema = new SchemaType("cat", typeEnv);
        var schema2 = new SchemaType("cat", typeEnv);

        assertEquals(schema, schema2);
    }

    @Test
    void empty() {
        var actual = checker.visit(parse("""
                schema Vm {
                
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());
        assertEquals(new SchemaType("Vm", checker.getEnv()), actual);
    }

    @Test
    void singleProperty() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number   x  
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        SchemaType vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        assertEquals(vm, actual);
    }

    @Test
    void singlePropertyObject() {
        var actual = checker.visit(parse("""
                schema Vm {
                   object   x  
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        SchemaType vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ObjectType.INSTANCE);
        assertEquals(vm, actual);
    }


    @Test
    @DisplayName("single property object with properties checks if type was correctly parsed")
    void singlePropertyObjectWrontAssignment() {
        Assertions.assertThrows(TypeError.class, () -> {
            checker.visit(parse("""
                    schema Vm {
                       object   x  = false
                    }
                    """));
        });
    }

    @Test
    void singlePropertyInit() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number    x = 1
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        assertEquals(vm, actual);
    }

    @Test
    void objectInit() {
        var actual = checker.visit(parse("""
                schema Vm {
                   object x = {
                     size: 1
                   }
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        var typeEnv = new TypeEnvironment(Map.of("size", ValueType.Number));
        vm.setProperty("x", new ObjectType(typeEnv));
        assertEquals(vm, actual);
    }

    @Test
    void singlePropertyInitThrows() {
        Assertions.assertThrows(TypeError.class, () -> checker.visit(parse("""
                schema Vm {
                   number  x   = true
                }
                """)));
    }

    @Test
    void multipleProperty() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number x  
                   string y  
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        vm.setProperty("y", ValueType.String);
        assertEquals(vm, actual);
    }

    @Test
    void multiplePropertyInit() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number x   = 2
                   string y   = "test"
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        vm.setProperty("y", ValueType.String);
        assertEquals(vm, actual);
    }

    @Test
    void schemaWithCloudAnnotation() {
        var actual = checker.visit(parse("""
                schema Bucket {
                   @cloud string arn
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var bucket = new SchemaType("Bucket", checker.getEnv());
        bucket.setProperty("arn", ValueType.String);
        assertEquals(bucket, actual);
    }

    @Test
    void schemaWithMixedProperties() {
        var actual = checker.visit(parse("""
                schema Bucket {
                   string name
                   string region
                   @cloud string arn
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var bucket = new SchemaType("Bucket", checker.getEnv());
        bucket.setProperty("name", ValueType.String);
        bucket.setProperty("region", ValueType.String);
        bucket.setProperty("arn", ValueType.String);
        assertEquals(bucket, actual);
    }

    @Test
    void schemaCloudPropertyWithComputedInit() {
        var actual = checker.visit(parse("""
                schema Bucket {
                   string name
                   @cloud string url = "https://" + name
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var bucket = new SchemaType("Bucket", checker.getEnv());
        bucket.setProperty("name", ValueType.String);
        bucket.setProperty("url", ValueType.String);
        assertEquals(bucket, actual);
    }

    @Test
    void schemaPropertyWithNestedSchema() {
        var actual = checker.visit(parse("""
                schema Address {
                    string street
                    string city
                }

                schema Person {
                    string name
                    Address address
                }
                """));

        // Person should have 'address' property of type Address (SchemaType)
        var person = (SchemaType) actual;
        var addressType = person.getEnvironment().lookup("address");
        assertEquals(SchemaType.class, addressType.getClass());
        assertEquals("Address", addressType.name());
    }

    @Test
    void resourceWithNestedSchemaPropertyRequiresSchemaType() {
        // Object literals do NOT structurally match schema types
        // This is intentional - schemas are nominal types, not structural
        Assertions.assertThrows(TypeError.class, () -> checker.visit(parse("""
                schema Address {
                    string street
                    string city
                }

                schema Person {
                    string name
                    Address address
                }

                resource Person john {
                    name = "John"
                    address = {
                        street: "123 Main St",
                        city: "NYC"
                    }
                }
                """)));
    }

    @Test
    void nestedSchemaWithResourceReference() {
        // Resource references match their schema type
        // e.g., resource Address home can be assigned to Address-typed property
        var actual = checker.visit(parse("""
                schema Address {
                    string street
                    string city
                }

                schema Person {
                    string name
                    Address address
                }

                resource Address home {
                    street = "123 Main St"
                    city = "NYC"
                }

                resource Person john {
                    name = "John"
                    address = home
                }
                """));

        Assertions.assertNotNull(actual);
    }

    @Test
    void nestedSchemaWithWrongResourceType() {
        // Wrong resource type should fail - Network != Address
        Assertions.assertThrows(TypeError.class, () -> checker.visit(parse("""
                schema Address {
                    string street
                    string city
                }

                schema Network {
                    string cidr
                }

                schema Person {
                    string name
                    Address address
                }

                resource Network vpc {
                    cidr = "10.0.0.0/16"
                }

                resource Person john {
                    name = "John"
                    address = vpc
                }
                """)));
    }

    @Test
    void cloudAnnotationOnlyOnSchemaProperty() {
        // @cloud is valid on schema properties
        var actual = checker.visit(parse("""
                schema Bucket {
                    string name
                    @cloud string arn
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());
    }

    @Test
    void cloudAnnotationInvalidOnResource() {
        // @cloud cannot be used on resources
        Assertions.assertThrows(TypeError.class, () -> checker.visit(parse("""
                schema Bucket {
                    string name
                }

                @cloud
                resource Bucket myBucket {
                    name = "my-bucket"
                }
                """)));
    }

    @Test
    void cloudAnnotationInvalidOnInput() {
        // @cloud cannot be used on inputs
        Assertions.assertThrows(TypeError.class, () -> checker.visit(parse("""
                component app {
                    @cloud
                    input string name
                }
                """)));
    }

    @Test
    void cloudAnnotationInvalidOnOutput() {
        // @cloud cannot be used on component outputs
        Assertions.assertThrows(TypeError.class, () -> checker.visit(parse("""
                component app {
                    @cloud
                    output string result = "test"
                }
                """)));
    }

    @Test
    void cloudAnnotationWithImportableShorthand() {
        // @cloud(importable) is valid shorthand for importable=true
        var actual = checker.visit(parse("""
                schema Bucket {
                    string name
                    @cloud(importable) string id
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());
    }

    @Test
    void cloudAnnotationWithImportableTrue() {
        // @cloud(importable=true) is valid
        var actual = checker.visit(parse("""
                schema Bucket {
                    string name
                    @cloud(importable=true) string id
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());
    }

    @Test
    void cloudAnnotationWithImportableFalse() {
        // @cloud(importable=false) is valid (same as plain @cloud)
        var actual = checker.visit(parse("""
                schema Bucket {
                    string name
                    @cloud(importable=false) string arn
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());
    }

    @Test
    void cloudAnnotationWithUnknownArgument() {
        // @cloud with unknown argument should fail (at parse or type-check time)
        Assertions.assertThrows(Exception.class, () -> checker.visit(parse("""
                schema Bucket {
                    @cloud(unknown)
                    string arn
                }
                """)));
    }

    @Test
    void cloudAnnotationWithStringArgument() {
        // @cloud("aws") should fail - string not allowed
        Assertions.assertThrows(Exception.class, () -> checker.visit(parse("""
                schema Bucket {
                    @cloud("aws")
                    string arn
                }
                """)));
    }

    @Test
    void cloudAnnotationWithUnknownNamedArgument() {
        // @cloud(provider="aws") should fail - unknown named arg (at parse or type-check time)
        Assertions.assertThrows(Exception.class, () -> checker.visit(parse("""
                schema Bucket {
                    @cloud(provider="aws")
                    string arn
                }
                """)));
    }

    @Test
    void cloudAnnotationImportableWithNonBoolean() {
        // @cloud(importable="yes") should fail - must be boolean (at parse or type-check time)
        Assertions.assertThrows(Exception.class, () -> checker.visit(parse("""
                schema Bucket {
                    @cloud(importable="yes")
                    string arn
                }
                """)));
    }

}
