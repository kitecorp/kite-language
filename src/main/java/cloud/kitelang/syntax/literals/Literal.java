package cloud.kitelang.syntax.literals;

import cloud.kitelang.syntax.ast.expressions.Expression;

public abstract non-sealed class Literal extends Expression {

    abstract public Object getVal();

}
