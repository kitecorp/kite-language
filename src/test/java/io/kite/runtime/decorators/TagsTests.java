package io.kite.runtime.decorators;

import io.kite.runtime.values.ResourceValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.kite.runtime.decorators.Tags.tags;

@Log4j2
@DisplayName("@tags")
public class TagsTests extends DecoratorTests {


    @Test
    void tagsValidStringArray() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @tags(["networking", "loadbalancer"])
                resource vm something {}""");
        Assertions.assertEquals(tags("networking", "loadbalancer"), res.getTags());
    }

    @Test
    void tagsDuplicateDedupeSilently() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @tags(["aws:core", "aws:extended"])
                resource vm something {}""");
        Assertions.assertEquals(tags("aws:core", "aws:extended"), res.getTags());
    }

    @Test
    void tagsValidString() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @tags("aws")
                resource vm something {}""");
        Assertions.assertEquals(tags("aws"), res.getTags());
    }

    @Test
    void tagsValidObjectArray() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @tags({ env: "prod", cloud: "azure" })
                resource vm something {}""");
        Assertions.assertEquals(tags(Map.of("env", "prod", "cloud", "azure")), res.getTags());
    }

    @Test
    void tagsValidObject() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @tags({
                    env: "prod", 
                    cloud: "azure"
                })
                resource vm something {}""");
        Assertions.assertEquals(tags(Map.of("env", "prod", "cloud", "azure")), res.getTags());
    }

}
