package io.zmeu.Runtime.Values;

import io.zmeu.Runtime.Interpreter;

public interface DeferredObserverValue {
    Object notify(Interpreter interpreter);

    boolean isEvaluated();

}
