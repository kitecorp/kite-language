package io.kite.Runtime.Values;

import io.kite.Runtime.Interpreter;

public interface DeferredObserverValue {
    Object notify(Interpreter interpreter);

    /**
     * Called when a specific dependency has been resolved.
     * Allows incremental re-evaluation based on which dependency was satisfied.
     */
    default Object notifyDependencyResolved(Interpreter interpreter, String resolvedResourceName) {
        return notify(interpreter); // Default: fall back to full re-evaluation
    }

    boolean isEvaluated();

    boolean isEvaluating();

    void setEvaluated(boolean evaluated);

    void setEvaluating(boolean evaluating);

}
