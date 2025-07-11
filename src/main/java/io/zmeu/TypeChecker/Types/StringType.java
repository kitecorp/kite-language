package io.zmeu.TypeChecker.Types;

import lombok.Getter;

public class StringType extends ValueType {
    @Getter
    private String string;

    public StringType(String string) {
        super(String.toString());
        this.string = string;
    }

    public StringType(String string, boolean immutable) {
        this(string);
        setImmutable(immutable);
    }

}
