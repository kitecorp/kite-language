package io.kite.runtime;

import io.kite.base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Log4j2
public class UnionTypeErrorsTest extends RuntimeTest {

    @Test
    @DisplayName("Should throw error if assigning the wrong value to a union type of numbers")
    void ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = 3
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a string to a union type of numbers")
    void shouldThrowString() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = 'hello'
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a false to a union type of numbers")
    void shouldThrowIfAssignFalseToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = false
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a true to a union type of numbers")
    void shouldThrowIfAssignTrueToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = true
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a object to a union type of numbers")
    void shouldThrowIfAssignObjectToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = { hello: 'world' }
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a object to a union type of numbers")
    void shouldThrowIfAssignEmptyObjectToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = { }
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a object to a union type of numbers")
    void shouldThrowIfAssignKeywordObjectToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = object()
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a object to a union type of numbers")
    void shouldThrowIfAssignEmptyKeywordObjectToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = object()
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a object to a union type of numbers")
    void shouldThrowIfAssignEmptyKeywordEmptyObjectToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = object({})
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a array of string to a union type of numbers")
    void shouldThrowIfAssignArrayToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = ['hello']
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if assigning a array of numbers to a union type of numbers")
    void shouldThrowIfAssignArrayOfValidNumbersToNumberUnionType() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers numbers = [1,2,5]
                    """);
        });
    }

    @Test
    @DisplayName("Should throw error if array contains incompatible types")
    void arrayOfTypeShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> {
            eval("""
                    type customNumbers = 1 | 2 | 5
                    var customNumbers[] numbers = [3]
                    """);
        });
    }

}
