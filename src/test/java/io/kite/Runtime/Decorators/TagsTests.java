package io.kite.Runtime.Decorators;

import io.kite.Runtime.Values.ResourceValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

@Log4j2
public class TagsTests extends DecoratorTests {

    @Test
    void tagsValidString() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @tags("networking")
                resource vm something {}""");
        Assertions.assertEquals(new Tags(Set.of("networking")), res.getTags());
    }

    @Test
    void tagsValidStringArray() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @tags(["networking", "loadbalancer"])
                resource vm something {}""");
        Assertions.assertEquals(Set.of("networking", "loadbalancer"), res.getProviders());
    }

    @Test
    void tagsDuplicateDedupeSilently() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @provider(["aws:core", "aws:extended"])
                resource vm something {}""");
        Assertions.assertEquals(Set.of("aws"), res.getProviders());
    }

}
