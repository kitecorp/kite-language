package io.kite.TypeChecker.Types;

import io.kite.Frontend.Parser.Expressions.Expression;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

public sealed abstract class Type extends Expression
        permits FunType, ReferenceType, ValueType {
    @Getter
    @Setter
    private String value;
    @Getter
    private SystemType kind;

    public Type(String value) {
        this();
        this.value = value;
        this.kind = SystemType.ANY; // might be the wrong type, but we don't know yet. Correct kind will be set later in type checker
    }

    public Type(SystemType kind) {
        this.kind = kind;
        setValue(kind.name().toLowerCase());
    }

    public Type() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Type type)) return false;

        return new EqualsBuilder().appendSuper(super.equals(o)).append(getValue(), type.getValue()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getValue()).toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Type.class.getSimpleName() + "[", "]")
                .add("value='" + value + "'")
                .add("kind=" + this.getClass().getSimpleName())
                .toString();
    }

    public boolean hasValue() {
        return StringUtils.isNotBlank(value);
    }

}
