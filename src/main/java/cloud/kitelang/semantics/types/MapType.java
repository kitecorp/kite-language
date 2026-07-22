package cloud.kitelang.semantics.types;

import cloud.kitelang.semantics.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Semantic type for {@code map<KeyType, ValueType>} schema properties, variables, and parameters.
 * Mirrors {@link ArrayType} (also a container-of-another-type reference type) but carries two
 * nested types instead of one, since a map is keyed rather than merely ordered.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class MapType extends ReferenceType {
    @Getter
    @Setter
    @NotNull
    private Type keyType;
    @Getter
    @Setter
    @NotNull
    private Type valueType;

    public MapType(@Nullable TypeEnvironment environment) {
        super(SystemType.MAP, environment);
    }

    public MapType(@Nullable TypeEnvironment environment, Type keyType, Type valueType) {
        this(environment);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public static MapType mapType(Type keyType, Type valueType) {
        return new MapType(null, keyType, valueType);
    }
}
