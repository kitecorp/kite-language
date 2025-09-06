package io.kite.Runtime;

import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.Runtime.Inputs.CliResolver;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

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
    protected @NotNull ChainResolver getChainResolver() {
        return new ChainResolver(List.of(new CliResolver()));
    }

}
