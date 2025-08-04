package io.kite.Runtime.Values;

import io.kite.Runtime.Interpreter;

public interface DeferredObserverValue {
    Object notify(Interpreter interpreter);

    boolean isEvaluated();

    boolean isEvaluating();

    void setEvaluated(boolean evaluated);

    void setEvaluating(boolean evaluating);

}
