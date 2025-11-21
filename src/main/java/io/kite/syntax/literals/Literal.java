package io.kite.syntax.literals;

import io.kite.syntax.ast.expressions.Expression;

public abstract non-sealed class Literal extends Expression {

    abstract public Object getVal();

}
