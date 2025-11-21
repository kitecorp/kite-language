package io.kite.frontend.parse.literals;

import io.kite.frontend.parser.expressions.Expression;

public abstract non-sealed class Literal extends Expression {

    abstract public Object getVal();

}
