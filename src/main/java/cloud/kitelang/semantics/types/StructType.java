package cloud.kitelang.semantics.types;

import cloud.kitelang.semantics.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

/**
 * Type representing a struct declaration.
 * Structs are nominal typed data containers.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class StructType extends ReferenceType {
    public static final StructType INSTANCE = new StructType("empty");

    public StructType(String typeName, @Nullable TypeEnvironment env) {
        super(SystemType.STRUCT, typeName, env);
    }

    public StructType(String typeName) {
        this(typeName, null);
    }
}
