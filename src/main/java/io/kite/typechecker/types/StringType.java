package io.kite.typechecker.types;

import lombok.Getter;

public class StringType extends ValueType {
    @Getter
    private final String string;

    public StringType(String string) {
        super(SystemType.STRING);
        this.string = string;
    }

}
