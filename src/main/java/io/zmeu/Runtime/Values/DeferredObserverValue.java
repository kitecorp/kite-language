package io.kite.Runtime.Values;

import io.kite.Runtime.Interpreter;

public interface DeferredObserverValue {
    Object notify(Interpreter interpreter);

    boolean isEvaluated();

}
