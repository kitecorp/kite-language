package io.kite.runtime;

import java.util.List;

public interface Callable {
    Object call(Interpreter interpreter, List<Object> args);

    default Object call(List<Object> args) {
        return call(null, args);
    }

    default Object call(Interpreter interpreter, Object... args) {
        return call(interpreter, List.of(args));
    }

    default Object call(Object... args) {
        return call(null, List.of(args));
    }

    default int arity() {
        return 0;
    }
}
