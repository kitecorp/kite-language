package io.zmeu.Runtime.Values;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class NullValue  {
    private static final NullValue value = new NullValue();

    public NullValue() {
    }

    @Nullable
    @JsonIgnore
    public Object getRuntimeValue() {
        return value;
    }

    public static NullValue of() {
        return value;
    }
}
