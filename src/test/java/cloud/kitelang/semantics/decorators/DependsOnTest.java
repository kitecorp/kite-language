package cloud.kitelang.semantics.decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker @dependsOn")
public class DependsOnTest extends CheckerTest {

    @Test
    void dependsOnResource() {
        var res = eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn(first)
                resource vm something {}
                
                """);

    }

    @Test
    void dependsOnResourceReverse() {
        var res = eval("""
                schema vm {}
                
                @dependsOn(something)
                resource vm first { }
                
                resource vm something {}
                
                """);

    }

    @Test
    void dependsOnResourceArray() {
        var res = eval("""
                schema vm {}
                
                resource vm first { }
                resource vm second { }
                
                @dependsOn([first, second])
                resource vm something {}
                
                """);

    }

    @Test
    void dependsOnResourceArrayReverse() {
        var res = eval("""
                schema vm {}
                
                @dependsOn([second, third])
                resource vm first { }
                resource vm second { }
                
                resource vm third {}
                
                """);
    }

    @Test
    void dependsOnResourceInvalidArray() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm first { }
                resource vm second { }
                
                @dependsOn([first, "second"])
                resource vm something {}
                
                """));
    }

    @Test
    void dependsOnString() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn("first")
                resource vm something {}
                
                """)
        );
    }

    @Test
    void dependsOnNumber() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn(10)
                resource vm something {}
                
                """)
        );
    }

    @Test
    void dependsOnObject() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn(true)
                resource vm something {}
                """)
        );
    }

    @Test
    void dependsOnResourceInComponent() {
        var res = eval("""
                schema vm {}
                
                component app {
                    resource vm first { }
                
                    @dependsOn(first)
                    resource vm something {}
                }
                """);
    }

    @Test
    void dependsOnResourceReverseInComponent() {
        var res = eval("""
                schema vm {}
                
                component app {
                    @dependsOn(something)
                    resource vm first { }
                
                    resource vm something {}
                }
                """);
    }

    @Test
    void dependsOnResourceArrayInComponent() {
        var res = eval("""
                schema vm {}
                
                component app {
                    resource vm first { }
                    resource vm second { }
                
                    @dependsOn([first, second])
                    resource vm something {}
                }
                """);
    }

    @Test
    void dependsOnResourceArrayReverseInComponent() {
        var res = eval("""
                schema vm {}
                
                component app {
                    @dependsOn([second, third])
                    resource vm first { }
                
                    resource vm second { }
                    resource vm third {}
                }
                """);
    }

    @Test
    void dependsOnResourceInvalidArrayInComponent() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component app {
                    resource vm first { }
                    resource vm second { }
                
                    @dependsOn([first, "second"])
                    resource vm something {}
                }
                """));
    }

    @Test
    void dependsOnStringInComponent() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component app {
                    resource vm first { }
                
                    @dependsOn("first")
                    resource vm something {}
                }
                """));
    }

    @Test
    void dependsOnNumberInComponent() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component app {
                    resource vm first { }
                
                    @dependsOn(10)
                    resource vm something {}
                }
                """));
    }

    @Test
    void dependsOnObjectInComponent() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component app {
                    resource vm first { }
                
                    @dependsOn(true)
                    resource vm something {}
                }
                """));
    }

    @Test
    void dependsOnComponentInstance() {
        var res = eval("""
                schema vm {}
                
                component networking {
                    resource vm vpc {}
                }
                
                component app {
                    resource vm server {}
                }
                
                component networking prodNet {}
                
                @dependsOn(prodNet)
                component app prodApp {}
                """);
    }

    @Test
    void dependsOnComponentInstanceArray() {
        var res = eval("""
                schema vm {}
                
                component networking {
                    resource vm vpc {}
                }
                
                component database {
                    resource vm db {}
                }
                
                component app {
                    resource vm server {}
                }
                
                component networking prodNet {}
                component database prodDb {}
                
                @dependsOn([prodNet, prodDb])
                component app prodApp {}
                """);
    }

    @Test
    void dependsOnComponentDefinitionShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                component networking {
                    resource vm vpc {}
                }
                
                @dependsOn(networking)
                component app {
                    resource vm server {}
                }
                """));
    }

    @Test
    void dependsOnComponentDefinitionFromInstanceShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component networking {
                    resource vm vpc {}
                }
                
                component app {
                    resource vm server {}
                }
                
                @dependsOn(networking)
                component app prodApp {}
                """));
    }

    @Test
    void componentInstanceDependsOnResource() {
        var res = eval("""
                schema vm {}
                
                resource vm database {}
                
                component app {
                    resource vm server {}
                }
                
                @dependsOn(database)
                component app prodApp {}
                """);
    }

    @Test
    void componentInstanceDependsOnMultipleResources() {
        var res = eval("""
                schema vm {}
                
                resource vm database {}
                resource vm cache {}
                
                component app {
                    resource vm server {}
                }
                
                @dependsOn([database, cache])
                component app prodApp {}
                """);
    }

    @Test
    void componentDefinitionDependsOnResourceShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm database {}
                
                @dependsOn(database)
                component app {
                    resource vm server {}
                }
                """));
    }

    @Test
    void resourceDependsOnComponentInstance() {
        var res = eval("""
                schema vm {}
                
                component app {
                    resource vm server {}
                }
                
                component app prodApp {}
                
                @dependsOn(prodApp)
                resource vm monitoring {}
                """);
    }

    @Test
    void resourceDependsOnMultipleComponentInstances() {
        var res = eval("""
                schema vm {}
                
                component app {
                    resource vm server {}
                }
                
                component database {
                    resource vm db {}
                }
                
                component app prodApp {}
                component database prodDb {}
                
                @dependsOn([prodApp, prodDb])
                resource vm monitoring {}
                """);
    }

    @Test
    void resourceDependsOnComponentDefinitionShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component app {
                    resource vm server {}
                }
                
                @dependsOn(app)
                resource vm monitoring {}
                """));
    }

}
