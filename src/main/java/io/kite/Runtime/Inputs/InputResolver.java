package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.Nullable;

public abstract class InputResolver {
    // todo remove this
    protected Ansi ansi = Ansi.ansi(50)
            .reset()
            .eraseScreen();

    public InputResolver() {
    }

    @Nullable
    abstract String resolve(InputDeclaration key, Object previousValue);

}
