package io.kite.typechecker.types;

import io.kite.frontend.parser.expressions.Expression;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public sealed abstract class Type extends Expression
        permits DecoratorType, FunType, ReferenceType, ValueType {
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

    public boolean hasValue() {
        return StringUtils.isNotBlank(value);
    }

    public String name() {
        return getValue();
    }

}
