package io.zmeu.TypeChecker.Types;

import lombok.Getter;
import lombok.Setter;

public class StringType extends ValueType {
    @Getter
    private String string;
    @Getter
    @Setter
    private boolean immutable;

    public StringType(String string) {
        super(String.toString());
        this.string = string;
    }

    public StringType(String string, boolean immutable) {
        this(string);
        this.immutable = immutable;
    }

}
