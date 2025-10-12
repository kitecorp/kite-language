package io.kite.Visitors;

import io.kite.ContextStack;
import io.kite.Frontend.Parser.Expressions.Callstack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract non-sealed class StackVisitor<R> implements Visitor<R> {
    private final Deque<Callstack> callstack;
    /**
     * Used to track where are we in the execution of the program. Are we in an for statement? or in a Schema declaration? in a resource declaration?
     */
    private final Deque<ContextStack> contextStacks;

    public StackVisitor() {
        this.callstack = new ArrayDeque<>();
        this.contextStacks = new ArrayDeque<>();
    }

    protected boolean ExecutionContextIn(Class<?> forStatementClass) {
        for (Callstack next : callstack) {
            if (next.getClass().equals(forStatementClass)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    protected Callstack ExecutionContext(Class<?> forStatementClass) {
        for (Callstack next : callstack) {
            if (next.getClass().equals(forStatementClass)) {
                return next;
            }
        }
        return null;
    }

    protected void push(Callstack expression) {
        if (expression != null) {
            callstack.push(expression);
        }
    }
    protected void push(ContextStack expression) {
        if (expression != null) {
            contextStacks.push(expression);
        }
    }

    protected void pop(ContextStack expression) {
        if (expression != null) {
            contextStacks.pop();
        }
    }
    protected void pop(@Nullable Callstack expression) {
        if (expression != null) {
            callstack.pop();
        }
    }

    protected boolean peek(ContextStack contextStack) {
        return contextStacks.peek() == contextStack;
    }

    protected boolean contextStackContains(ContextStack contextStack) {
        return contextStacks.contains(contextStack);
    }
}
