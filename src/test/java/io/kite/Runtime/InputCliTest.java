package io.kite.Runtime;

import io.kite.Runtime.Inputs.CliResolver;
import io.kite.Runtime.Inputs.InputResolver;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class InputCliTest extends InputTest {
    private InputStream sysInBackup = System.in;

    @AfterEach
    public void cleanup() {
        System.setIn(sysInBackup);
    }

    protected void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    @Override
    protected @NotNull List<InputResolver> getResolvers() {
        return List.of(new CliResolver());
    }

}
