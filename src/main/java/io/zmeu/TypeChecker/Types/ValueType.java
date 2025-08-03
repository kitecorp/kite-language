package io.kite.TypeChecker.Types;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public non-sealed class ValueType extends Type {
    public static ValueType String = new ValueType(SystemType.STRING);
    public static ValueType Number = new ValueType(SystemType.NUMBER);
    public static ValueType Boolean = new ValueType(SystemType.BOOLEAN);
    public static ValueType Void = new ValueType(SystemType.VOID);
    public static ValueType Null = new ValueType(SystemType.NULL);

    public ValueType() {
        super();
    }

    public ValueType(String value) {
        super();
        setValue(value);
    }

    public ValueType(SystemType value) {
        super(value);
    }

    public static ValueType of(String value) {
        return switch (value) {
            case "boolean" -> Boolean;
            case "string" -> String;
            case "number" -> Number;
            case "void" -> Void;
            case "null" -> Null;
            default -> null;
        };
    }

    public static ValueType[] values() {
        return new ValueType[]{String, Number, Boolean, Void, Null};
    }

    @Override
    public java.lang.String toString() {
        return getValue();
    }

}
