package io.kite.runtime.decorators;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
@DisplayName("@allowed decorator")
public class AllowedTests extends DecoratorTests {


    @Test
    void decoratorAllow() {
        eval("""
                @allowed(["hello"])
                input string something = "hello"
                """);
    }

    @Test
    void decoratorAllowThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed(["hello"])
                input string something = "bye"
                """));
    }

    @Test
    void decoratorAllowNumber() {
        eval("""
                @allowed([10, 20])
                input number something = 20
                """);
    }

    @Test
    void decoratorAllowNumberThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed([10, 20])
                input number something = 30
                """));
    }

    @Test
    void decoratorAllowStringsArray() {
        eval("""
                @allowed(["hello", "world"])
                input string[] something = ["world", "hello"]
                """);
    }

    @Test
    void decoratorAllowStringsArrayThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed(["hello", "world"])
                input string[] something = ["world", "hello","!"]
                """));
    }

    @Test
    void decoratorAllowNumberArray() {
        eval("""
                @allowed([10, 20])
                input number[] something = [10, 20]
                """);
    }

    @Test
    void decoratorAllowBooleansArray() {
        eval("""
                @allowed([true, false])
                input boolean[] something = [true, false]
                """);
    }

    @Test
    void decoratorAllowTrueArray() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed([true])
                input boolean[] something = [true, false]
                """));
    }

    @Test
    void decoratorAllowObject() {
        eval("""
                @allowed([{ env: 'dev' }])
                input object something = { env: 'dev' }
                """);
    }

    @Test
    void decoratorAllowObjectValueArray() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed([{ env: 'dev' }])
                input object something = { env: 'prod' }
                """));
    }

    @Test
    void decoratorAllowObjectComplexArray() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed([{ env: 'dev' }])
                input object something = { env: 'prod', region: 'us-east-1' }
                """));
    }

    @Test
    void decoratorAllowObjectKeyArray() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed([{ env: 'dev' }])
                input object something = { main: 'dev' }
                """));
    }

    @Test
    void decoratorAllowNumberArrayThrow() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @allowed([10, 20])
                input number[] something = [10, 20, 30]
                """));
    }

}
