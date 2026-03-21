package cloud.kitelang.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Resource string interpolation")
public class ResourceStringInterpolationTest extends BaseIntegrationTest {
    @Test
    void stringInterpolationMemberAccess() {
        eval("""
                        schema vm { string name; number size; }
                
                        resource vm server {
                            name = "main-property"
                            size = 1
                        }
                
                        fun main() {
                            var x = "Hello ${server.name}! Your number is ${server.size}"
                            println(x)
                        }
                        main()
                """);
    }

    @Test
    void interpolationAfterCount() {
        eval("""
                    schema vm { string name; number size; }
                
                    @count(2)
                    resource vm main {
                        name = "property-${count}"
                        size = 1
                    }
                
                    var x = main[0].name
                    var y = main[1].name
                """);
        Assertions.assertEquals("property-0", interpreter.getInstance("main[0]").getProperty("name"));
        Assertions.assertEquals("property-1", interpreter.getInstance("main[1]").getProperty("name"));
    }

    @Test
    void interpolationBeforeCount() {
        eval("""
                    schema vm { string name; number size; }
                
                    var x = main[0].name
                    var y = main[1].name
                
                    @count(2)
                    resource vm main {
                        name = "property-${count}"
                        size = 1
                    }
                
                
                """);
        Assertions.assertEquals("property-0", interpreter.getInstance("main[0]").getProperty("name"));
        Assertions.assertEquals("property-1", interpreter.getInstance("main[1]").getProperty("name"));
    }

    @Test
    @DisplayName("String interpolation with resource property reference should resolve to actual value")
    void shouldResolveResourcePropertyInStringInterpolation() {
        eval("""
                schema S3Bucket {
                    string bucket
                }

                resource S3Bucket main {
                    bucket = "kite-dev-bucket"
                }

                resource S3Bucket second {
                    bucket = "${main.bucket}-second"
                }
                """);

        var second = interpreter.getInstance("second");
        assertEquals("kite-dev-bucket-second", second.getProperty("bucket"));
    }

    @Test
    @DisplayName("String interpolation with multiple resource property references should resolve all values")
    void shouldResolveMultipleResourcePropertiesInStringInterpolation() {
        eval("""
                schema Server {
                    string name
                    string region
                    string tag
                }

                resource Server main {
                    name = "prod"
                    region = "us-east-1"
                    tag = "default"
                }

                resource Server second {
                    name = "backup"
                    region = "eu-west-1"
                    tag = "${main.name}-${main.region}"
                }
                """);

        var second = interpreter.getInstance("second");
        assertEquals("prod-us-east-1", second.getProperty("tag"));
    }
}
