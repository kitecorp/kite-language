package cloud.kitelang.execution.inputs;

import cloud.kitelang.syntax.ast.expressions.InputDeclaration;
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
