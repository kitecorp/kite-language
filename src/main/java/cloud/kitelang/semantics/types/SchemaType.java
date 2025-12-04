package cloud.kitelang.semantics.types;

import cloud.kitelang.semantics.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class SchemaType extends ReferenceType {
    public static final SchemaType INSTANCE = new SchemaType("empty");


    public SchemaType(String typeName, @Nullable TypeEnvironment env) {
        super(SystemType.SCHEMA, typeName, env);
    }

    public SchemaType(String typeName) {
        this(typeName, null);
    }

}