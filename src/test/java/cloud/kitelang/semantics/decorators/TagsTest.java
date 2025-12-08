package cloud.kitelang.semantics.decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("@tags")
public class TagsTest extends CheckerTest {


    @Test
    void tagsValidString() {
        eval("""
                schema vm {}
                @tags("aws")
                resource vm something {}""");
    }

    @Test
    void tagsValidStringArray() {
        eval("""
                schema vm {}
                @tags(["aws", "azure"])
                resource vm something {}""");
    }

    @Test
    void tagsValidObjectArray() {
        eval("""
                schema vm {}
                @tags({"env": "prod", "cloud": "azure"})
                resource vm something {}""");
    }

    @Test
    void tagsValidObject() {
        eval("""
                schema vm {}
                @tags({
                    env: "prod", 
                    cloud: "azure"
                })
                resource vm something {}""");
    }

    @Test
    void tags() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags
                resource vm something {}"""));
    }

    @Test
    void tagsEmpty() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags()
                resource vm something {}"""));
    }

    @Test
    void tagsEmptyList() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags([])
                resource vm something {}"""));
    }

    @Test
    void tagsEmptyString() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags("")
                resource vm something {}"""));
    }


    @Test
    void tagsEmptyStringArray() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags(["aws",10])
                resource vm something {}"""));
    }

    @Test
    void tagsNumber() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags(10)
                resource vm something {}"""
        ));
    }

    @Test
    void tagsInValidObject() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": 10 })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectBool() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": true })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectEmptyKey() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "": "prod" })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectNestedValue() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": { "season": "prod"} })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectEmptyValue() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": "" })
                resource vm something {}""")
        );
    }

    @Test
    void tagsOnComponent() {
        eval("""
                component app {}
                
                @tags("production")
                component app prodApp {}
                """);
    }

    @Test
    void tagsOnComponentStringArray() {
        eval("""
                component app {}
                
                @tags(["production", "critical"])
                component app prodApp {}
                """);
    }

    @Test
    void tagsOnComponentObject() {
        eval("""
                component app {}
                
                @tags({env: "prod", team: "backend"})
                component app prodApp {}
                """);
    }

    @Test
    void tagsOnResourceInComponent() {
        eval("""
                schema vm {}
                
                component app {
                    @tags("database")
                    resource vm db {}
                }
                """);
    }

    @Test
    void tagsOnResourceInComponentStringArray() {
        eval("""
                schema vm {}
                
                component app {
                    @tags(["database", "critical"])
                    resource vm db {}
                }
                """);
    }

    @Test
    void tagsOnResourceInComponentObject() {
        eval("""
                schema vm {}
                
                component app {
                    @tags({
                        env: "prod",
                        component: "database"
                    })
                    resource vm db {}
                }
                """);
    }

    @Test
    void tagsOnMultipleResourcesInComponent() {
        eval("""
                schema vm {}
                
                component app {
                    @tags({env: "prod"})
                    resource vm web {}
                
                    @tags({env: "prod", type: "database"})
                    resource vm db {}
                }
                """);
    }

    @Test
    void tagsOnComponentDefinitionShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                @tags("production")
                component app {}
                """));
    }

    @Test
    void tagsEmptyOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                component app {}
                
                @tags()
                component app prodApp {}
                """));
    }

    @Test
    void tagsEmptyListOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                component app {}
                
                @tags([])
                component app prodApp {}
                """));
    }

    @Test
    void tagsEmptyStringOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                component app {}
                
                @tags("")
                component app prodApp {}
                """));
    }

    @Test
    void tagsNumberOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                component app {}
                
                @tags(10)
                component app prodApp {}
                """));
    }

    @Test
    void tagsInvalidObjectOnComponent() {
        assertThrows(RuntimeException.class, () -> eval("""
                component app {}
                
                @tags({env: 10})
                component app prodApp {}
                """));
    }

    @Test
    void tagsInvalidObjectBoolOnComponent() {
        assertThrows(RuntimeException.class, () -> eval("""
                component app {}
                
                @tags({env: true})
                component app prodApp {}
                """));
    }

    @Test
    void tagsInvalidObjectEmptyKeyOnComponent() {
        assertThrows(RuntimeException.class, () -> eval("""
                component app {}
                
                @tags({"": "prod"})
                component app prodApp {}
                """));
    }

    @Test
    void tagsInvalidObjectEmptyValueOnComponent() {
        assertThrows(RuntimeException.class, () -> eval("""
                component app {}
                
                @tags({env: ""})
                component app prodApp {}
                """));
    }

    @Test
    void tagsInvalidObjectNestedValueOnComponent() {
        assertThrows(RuntimeException.class, () -> eval("""
                component app {}
                
                @tags({env: {season: "prod"}})
                component app prodApp {}
                """));
    }

    @Test
    void tagsEmptyOnResourceInComponent() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component app {
                    @tags()
                    resource vm server {}
                }
                """));
    }

    @Test
    void tagsNumberOnResourceInComponent() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                component app {
                    @tags(10)
                    resource vm server {}
                }
                """));
    }

    @Test
    void tagsInvalidObjectOnResourceInComponent() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                
                component app {
                    @tags({env: 10})
                    resource vm server {}
                }
                """));
    }

    @Test
    void tagsWithVariableInComponent() {
        eval("""
                schema vm {}
                
                component app {
                    input string environment = "prod"
                
                    @tags(environment)
                    resource vm server {}
                }
                """);
    }

    @Test
    void tagsOnComponentAndResource() {
        eval("""
                schema vm {}
                
                component app {
                    @tags({component: "app"})
                    resource vm server {}
                }
                
                @tags({environment: "production"})
                component app prodApp {}
                """);
    }

}
