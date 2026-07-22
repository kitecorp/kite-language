package cloud.kitelang.syntax.literals;

import cloud.kitelang.semantics.types.TypeFactory;
import lombok.Getter;
import lombok.EqualsAndHashCode;

/**
 * AST-level representation of a {@code map<KeyType, ValueType>} type annotation.
 * <p>
 * Unlike {@link ArrayTypeIdentifier} (which reuses its element type's own {@code type}/{@code path}
 * fields since it only ever wraps ONE nested type), a map has two independent nested type
 * references, so they are held here explicitly instead. Both are kept unresolved at parse time
 * (deferred, like array element types) so forward-referenced schema/resource types used as map
 * values - the exact case that motivated this syntax (Terraform bridge nested MAP attributes) -
 * resolve correctly once the TypeChecker's environment is fully populated.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public final class MapTypeIdentifier extends TypeIdentifier {
    private final TypeIdentifier keyType;
    private final TypeIdentifier valueType;

    public MapTypeIdentifier(TypeIdentifier keyType, TypeIdentifier valueType) {
        super(TypeFactory.from("map"), PathIdentifier.type("map"));
        this.keyType = keyType;
        this.valueType = valueType;
    }

    public static MapTypeIdentifier mapType(TypeIdentifier keyType, TypeIdentifier valueType) {
        return new MapTypeIdentifier(keyType, valueType);
    }

    @Override
    public String string() {
        return "map<" + keyType.string() + ", " + valueType.string() + ">";
    }
}
