package io.kite.Frontend.Parse.Literals;

import io.kite.Frontend.Parser.Expressions.Expression;

public abstract non-sealed class Literal extends Expression {

    abstract public Object getVal();

}
