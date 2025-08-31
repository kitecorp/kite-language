package io.kite.Runtime;

import io.kite.Runtime.Inputs.CliResolver;
import io.kite.Runtime.Inputs.InputResolver;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InputCliTest extends InputTest {

    @Override
    protected @NotNull List<InputResolver> getResolvers() {
        return List.of(new CliResolver(global));
    }

}
