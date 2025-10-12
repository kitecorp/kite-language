package io.kite.Runtime;

import io.kite.Runtime.Inputs.InputChainResolver;
import io.kite.Runtime.Inputs.CliResolver;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputCliTests extends InputTests {
    private InputStream sysInBackup = System.in;

    @AfterEach
    public void cleanup() {
        System.setIn(sysInBackup);
    }

    protected void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    @Override
    protected void setInput(Integer input) {
        setInput(input.toString());
    }

    @Override
    protected void setInput(Boolean input) {
        setInput(input.toString());
    }

    @Override
    protected void setInput(Double input) {
        setInput(input.toString());
    }

    @Override
    protected void setInput(Float input) {
        setInput(input.toString());
    }

    @Override
    protected @NotNull InputChainResolver getChainResolver() {
        return new InputChainResolver(List.of(new CliResolver()));
    }


    @Test
    void testInputStringDefault() {
        setInput("hello");
        var res = eval("input string region = 'bello' ");
        assertEquals("bello", res); // input not triggered if default is set.
    }

    @Test
    void testInputNumberDefault() {
        setInput(10);
        var res = eval("input number region = 20 ");
        assertEquals(20, res);
    }

    @Test
    void testInputDecimalDefault() {
        setInput(10.2);
        var res = eval("input number region = 20.2");
        assertEquals(20.2, res);
    }

    @Test
    void testInputDecimalHalfDefault() {
        setInput(0.2);
        var res = eval("input number region = 0.5 ");
        assertEquals(0.5, res);
    }

    @Test
    void testInputTrueDefault() {
        setInput(true);
        var res = eval("input boolean region = false");
        assertEquals(false, res);
    }

    @Test
    void testInputFalseDefault() {
        setInput(false);
        var res = eval("input boolean region = true");
        assertEquals(true, res);
    }

    @Test
    void testInputObjectDefault() {
        setInput("{ env : 'dev', region : 'us-east-1' }");
        var res = eval("input object region = { env : 'prod', region : 'us-west-1' } ");
        assertEquals(Map.of("env", "prod", "region", "us-west-1"), res);
    }

    @Test
    void testInputUnionDefault() {
        setInput("hello");
        var res = eval("""
                type custom = string | number
                input custom region = 'bello'
                """);
        assertEquals("bello", res);
    }

    @Test
    void testInputUnionNumberDefault() {
        setInput(10);
        var res = eval("""
                type custom = string | number
                input custom region = 20
                """);
        assertEquals(20, res);
    }

    @Test
    void testInputStringArrayDefault() {
        setInput("['hello','world']");
        var res = eval("input string[] region= ['hi','kite'] ");
        assertEquals(List.of("hi", "kite"), res);
    }

    @Test
    void testInputNumberArrayDefault() {
        setInput("[1,2,3]");
        var res = eval("input number[] region = [4, 5, 6]");
        assertEquals(List.of(4, 5, 6), res);
    }

    @Test
    void testInputNumberArrayNoParanthesisDefaults() {
        setInput("1,2,3");
        var res = eval("input number[] region = [4, 5, 6]");
        assertEquals(List.of(4, 5, 6), res);
    }


    @Test
    void testInputUnionArrayDefault() {
        setInput("['hello','world']");
        var res = eval("""
                type custom = string | number
                input custom[] region = ['hi', 'kite']
                """);

        assertEquals(List.of("hi", "kite"), res);
    }

    @Test
    void testInputUnionArrayNumbersDefault() {
        setInput("[1, 2, 3]");
        var res = eval("""
                type custom = string | number
                input custom[] region = [4,5,6]
                """);

        assertEquals(List.of(4, 5, 6), res);
    }

    @Test
    void testInputTrueArrayDefaults() {
        setInput("[true]");
        var res = eval("input boolean[] region = [false]");
        assertEquals(List.of(false), res);
    }

    @Test
    void testInputFalseArrayDefaults() {
        setInput("[false]");
        var res = eval("input boolean[] region = [true]");
        assertEquals(List.of(true), res);
    }


    @Test
    void testInputObjectArrayDefaults() {
        setInput("[{env:'dev'}]");
        var res = eval("input object[] region = [{env:'prod'}]");
        assertEquals(List.of(Map.of("env", "prod")), res);
    }
}
