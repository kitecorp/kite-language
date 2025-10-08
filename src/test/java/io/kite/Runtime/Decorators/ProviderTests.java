package io.kite.Runtime.Decorators;

import io.kite.Runtime.Values.ResourceValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

@Log4j2
public class ProviderTests extends DecoratorTests {

    @Test
    void providerValidString() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @provider("aws")
                resource vm something {}""");
        Assertions.assertEquals(Set.of("aws"), res.getProviders());
    }

    @Test
    void providerValidStringArray() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @provider(["aws", "azure"])
                resource vm something {}""");
        Assertions.assertEquals(Set.of("aws", "azure"), res.getProviders());
    }

    @Test
    void providerDuplicateDedupeSilently() {
        var res = (ResourceValue) eval("""
                schema vm {}
                @provider(["aws", "aws"])
                resource vm something {}""");
        Assertions.assertEquals(Set.of("aws"), res.getProviders());
    }

}
