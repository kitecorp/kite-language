package io.zmeu.Runtime.Functions;

import io.zmeu.Runtime.BaseRuntimeTest;
import org.junit.jupiter.api.Test;

class PrintFunctionTest extends BaseRuntimeTest {

    private final PrintFunction function = new PrintFunction();

    @Test
    void printNumbers() {
        function.call(1, 2, 3);
    }

    @Test
    void printStringNums() {
        function.call("1", "2", "3");
    }

    @Test
    void printMix() {
        function.call("1", "2", 3);
    }

    @Test
    void printMixStrings() {
        function.call("hi", "!", "I'm", "Bob", 3);
    }

    @Test
    void printlnMixStrings() {
        interpreter.visit(parser.produceAST(tokenizer.tokenize("""
                print(1,2,3)
                print("hi!","my","name","is","Bob")
                print("hi! my name is Bob")
                """)));
    }

}