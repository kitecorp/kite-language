package io.kite.Runtime.Decorators;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
@DisplayName("@allowed decorator")
public class AllowTests extends DecoratorTests {


    @Test
    void decoratorAllow() {
        eval("""
                @allowed(["hello"])
                input string something = "hello"
                """);
    }

    @Test
    void decoratorAllowNumber() {
        eval("""
                @allowed([10, 20])
                input number something = 20
                """);
    }

    @Test
    void decoratorAllowStringsArray() {
        eval("""
                @allowed(["hello", "world"])
                input string[] something = ["world", "hello"]
                """);
    }

    @Test
    void decoratorAllowNumberArray() {
        eval("""
                @allowed([10, 20])
                input number[] something = [10, 20]
                """);
    }

}
