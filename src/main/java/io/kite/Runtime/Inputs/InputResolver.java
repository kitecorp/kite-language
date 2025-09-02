package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Environment.Environment;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Nullable;

public abstract class InputResolver {
    // todo remove this
    private final Environment<Object> inputs;
    protected Ansi ansi = Ansi.ansi(50)
            .reset()
            .eraseScreen();

    public InputResolver(Environment<Object> inputs) {
        this.inputs = inputs;
    }

    @Nullable
    public abstract String resolve(InputDeclaration key);

    public Environment<Object> getInputs() {
        return inputs;
    }

}
