package io.zmeu.Runtime.Values;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class NullValue  {
    private static final NullValue value = new NullValue();

    public NullValue() {
    }

    @Nullable
    public Object getRuntimeValue() {
        return value;
    }

    public static NullValue of() {
        return value;
    }
}
